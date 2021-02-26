package org.openfinna.java.connector.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldPickupData;
import org.openfinna.java.connector.classes.models.holds.HoldStatus;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.classes.models.user.KirkesPreferences;
import org.openfinna.java.connector.classes.models.user.LibraryPreferences;
import org.openfinna.java.connector.http.WebClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses different things from JSON/HTML
 */
public class KirkesHTMLParser {

    private static final int maxRenewDefault = 3;

    private static final String renewCountDelimiter = "/";
    private static final String renewCountRegex = "([0-9]+" + renewCountDelimiter + "[0-9]+)";
    private static final String dueDateRegex = "((?:[0-9]{1}.)|(?:[0-9]{2}.)){2}[0-9]+";
    private static final String expirationDateRegex = "(((?:[0-9]{1}.)|(?:[0-9]{2}.)){2}[0-9]+)";
    private static final String orderNoRegex = "([0-9]+)";
    private static final Pattern renewCountPattern = Pattern.compile(renewCountRegex);
    private static final Pattern expirationDatePattern = Pattern.compile(expirationDateRegex);
    private static final Pattern dueDatePattern = Pattern.compile(dueDateRegex);
    private static final Pattern orderNoPattern = Pattern.compile(orderNoRegex);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String parseCSRF(String html) {
        Document document = Jsoup.parse(html);
        Element csrfElement = document.getElementsByAttributeValue("name", "csrf").first();
        if (csrfElement != null)
            if (csrfElement.hasAttr("value"))
                return csrfElement.attr("value");
        return null;
    }

    public static List<UserType> parseUserTypes(String html) {
        Document document = Jsoup.parse(html);
        List<UserType> types = new ArrayList<>();
        Element targetSelector = document.getElementsByAttributeValue("name", "target").first();
        if (targetSelector != null) {
            Elements options = targetSelector.getElementsByTag("option");
            for (Element option : options) {
                types.add(new UserType(option.attr("value"), option.text()));
            }
        }
        return types;
    }

    /**
     * Parse User Details from HTML code
     *
     * @param html HTML Code
     * @return User
     */
    public static User parseUserDetails(String html) {
        Document document = Jsoup.parse(html);
        User user = new User();
        LibraryPreferences libraryPreferences = new LibraryPreferences();
        KirkesPreferences kirkesPreferences = new KirkesPreferences();

        // Parse name
        Element fullName = document.getElementsByClass("username login-text").first();
        if (fullName != null) {
            String name = fullName.text().trim();
            user.setName(name);
            libraryPreferences.setFullName(name);
        }

        Element libraryForm = document.getElementById("profile_library_form");
        Element kirkesForm = document.getElementsByAttributeValue("name", "my_profile_form").first();

        if (libraryForm != null) {
            Elements libValues = libraryForm.getElementsByClass("profile-text-value");
            if (libValues.size() > 4) {
                libraryPreferences.setFirstName(libValues.get(0).text());
                libraryPreferences.setSurname(libValues.get(1).text());
                libraryPreferences.setAddress(libValues.get(2).text());
                libraryPreferences.setZipcode(libValues.get(3).text());
                libraryPreferences.setCity(libValues.get(4).text());
            }

            Element libPhone = document.getElementsByClass("profile_tel").first();
            if (libPhone != null && libPhone.hasAttr("value"))
                libraryPreferences.setPhoneNumber(libPhone.attr("value"));

            Element libEmail = document.getElementsByClass("profile_email").first();
            if (libEmail != null && libEmail.hasAttr("value"))
                libraryPreferences.setEmail(libEmail.attr("value"));
        }

        if (kirkesForm != null) {
            System.out.println("Kirkes FOUND!");
            Element kirkesEmail = document.getElementsByAttributeValue("name", "email").first();
            Element kirkesNick = document.getElementsByAttributeValue("name", "finna_nickname").first();

            if (kirkesEmail != null && kirkesEmail.hasAttr("value"))
                kirkesPreferences.setEmail(kirkesEmail.attr("value"));

            if (kirkesNick != null && kirkesNick.hasAttr("value"))
                kirkesPreferences.setNickname(kirkesNick.attr("value"));
        }

        user.setLibraryPreferences(libraryPreferences);
        user.setKirkesPreferences(kirkesPreferences);
        return user;
    }

    public static List<Loan> parseLoans(String html) {
        List<Loan> loans = new ArrayList<>();
        Document document = Jsoup.parse(html);

        Element table = document.getElementsByClass("myresearch-table").first();
        if (table != null) {
            Elements loansHtml = table.getElementsByClass("myresearch-row");
            for (Element loanHtml : loansHtml) {
                String recordId = loanHtml.attributes().get("id").replace("record", "");
                String title = null, type = null, author = null, image = null, renewId = null;
                int renewsUsed = 0, renewsTotal = 0;
                Date dueDate = null;

                // Init necessary elements
                Element inputOne = loanHtml.getElementsByAttributeValue("name", "renewAllIDS[]").first();
                Element inputTwo = loanHtml.getElementsByAttributeValue("name", "selectAllIDS[]").first();
                Element titleElem = loanHtml.getElementsByClass("record-title").first();
                Element metadataElem = loanHtml.getElementsByClass("record-core-metadata").first();
                Element typeElem = loanHtml.getElementsByClass("label-info").first();
                Element imageElem = loanHtml.getElementsByClass("recordcover").first();
                Elements textElements = loanHtml.getElementsByTag("strong");

                if (titleElem != null)
                    title = titleElem.text();

                if (typeElem != null)
                    type = typeElem.text();

                if (imageElem != null)
                    image = StringEscapeUtils.unescapeJava(imageElem.attributes().get("src"));

                if (metadataElem != null) {
                    Element authorUrl = metadataElem.getElementsByTag("a").first();
                    if (authorUrl != null)
                        author = authorUrl.text();
                }

                if (inputOne != null)
                    if (inputOne.hasAttr("value")) {
                        renewId = inputOne.attr("value");
                    } else if (inputTwo != null)
                        if (inputTwo.hasAttr("value"))
                            renewId = inputTwo.attr("value");

                for (Element text : textElements) {
                    Matcher renewCountMatcher = renewCountPattern.matcher(text.text());
                    Matcher dueDateMatcher = dueDatePattern.matcher(text.text());
                    if (renewCountMatcher.find()) {
                        String[] renewCountNumbers = renewCountMatcher.group(1).replace(renewCountDelimiter, ",").split(",");
                        renewsUsed = Integer.parseInt(renewCountNumbers[0]);
                        renewsTotal = Integer.parseInt(renewCountNumbers[1]);
                    } else if (dueDateMatcher.find()) {
                        String date = dueDateMatcher.group(0);
                        try {
                            dueDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                loans.add(new Loan(recordId, renewId, new Resource(recordId, title, author, type, image), renewsTotal, renewsUsed, dueDate));
            }
        }
        return loans;
    }

    public static List<Hold> parseHolds(String html) throws ParseException {
        List<Hold> holds = new ArrayList<>();
        Document document = Jsoup.parse(html);

        Element table = document.getElementsByClass("myresearch-table").first();
        if (table != null) {
            Elements loansHtml = table.getElementsByClass("myresearch-row");
            for (Element loanHtml : loansHtml) {
                String recordId = null;
                int queue = 0;
                HoldStatus status = HoldStatus.WAITING;
                // Init all elements
                Element inputOne = loanHtml.getElementsByAttributeValue("name", "cancelSelectedIDS[]").first();
                Element inputTwo = loanHtml.getElementsByAttributeValue("name", "cancelAllIDS[]").first();
                Element titleElement = loanHtml.getElementsByClass("record-title").first();
                Element plElem = loanHtml.getElementsByClass("pickupLocationSelected").first();
                Element plRoot = loanHtml.getElementsByClass("pickup-location-container").first();
                Element metadataElem = loanHtml.getElementsByClass("record-core-metadata").first();
                Element transmitElem = loanHtml.getElementsByClass("text-success").first();
                Element availElem = loanHtml.getElementsByClass("alert alert-success").first();
                Element statusBox = loanHtml.getElementsByClass("holds-status-information").first();
                Element queueElem = statusBox.getElementsByTag("p").first();
                Element typeElem = loanHtml.getElementsByClass("label-info").first();
                Element imageElem = loanHtml.getElementsByClass("recordcover").first();

                if (queueElem != null) {
                    String[] queueSplit = queueElem.text().split(":");
                    if (queueSplit.length > 0) {
                        queue = Integer.parseInt(queueSplit[1].trim());
                    }
                }

                Date expirationDate = null, holdDate = null;
                Matcher dateMatcher = expirationDatePattern.matcher(statusBox.text());
                List<String> values = new ArrayList<>();
                while (dateMatcher.find()) {
                    values.add(dateMatcher.group());
                }
                if (values.size() > 1) {
                    holdDate = dateFormat.parse(values.get(0));
                    expirationDate = dateFormat.parse(values.get(1));
                }

                String type = null, title = null, author = null, image = null, currentPickupLocation = null, actionId = null;
                boolean cancelPossible = false;
                int reservationNumber = -1;

                if (metadataElem != null) {
                    Element authorUrl = metadataElem.getElementsByTag("a").first();
                    if (authorUrl != null) {
                        author = authorUrl.text();
                    }
                }

                if (plElem != null) {
                    currentPickupLocation = plElem.text();
                } else if (plRoot != null) {
                    String[] lParts = plRoot.text().split(":");
                    if (lParts.length > 1) {
                        currentPickupLocation = lParts[1].replace("  ", "").replace("\n", "");
                    }
                }
                if (titleElement != null) {
                    title = titleElement.text();
                    recordId = titleElement.attr("href").replace("/Record/", "");
                }
                if (typeElem != null) {
                    type = typeElem.text();
                }
                if (imageElem != null) {
                    image = WebClient.optimizeURL(WebClient.kirkesBaseURL) + imageElem.attr("src");
                }

                if (inputOne != null) {
                    cancelPossible = !inputOne.hasAttr("disabled");
                    if (inputOne.hasAttr("value"))
                        actionId = inputOne.attr("value");
                } else if (inputTwo != null) {
                    cancelPossible = !inputTwo.hasAttr("disabled");
                    if (inputTwo.hasAttr("value"))
                        actionId = inputTwo.attr("value");
                }

                if (transmitElem != null && !cancelPossible)
                    status = HoldStatus.IN_TRANSIT;
                else if (availElem != null && !cancelPossible) {
                    status = HoldStatus.AVAILABLE;
                    Matcher orderNoMatcher = orderNoPattern.matcher(availElem.text());
                    StringBuilder numString = new StringBuilder();
                    while (orderNoMatcher.find())
                        numString.append(orderNoMatcher.group());
                    if (numString.length() > 0)
                        reservationNumber = Integer.parseInt(numString.toString());
                }

                holds.add(new Hold(recordId, actionId, status, cancelPossible, new HoldPickupData(currentPickupLocation, reservationNumber), queue, expirationDate, holdDate, new Resource(recordId, title, author, type, image)));
            }
        }
        return holds;
    }
}
