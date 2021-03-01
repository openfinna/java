package org.openfinna.java.connector.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.fines.Fine;
import org.openfinna.java.connector.classes.models.fines.Fines;
import org.openfinna.java.connector.classes.models.holds.*;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.classes.models.user.KirkesPreferences;
import org.openfinna.java.connector.classes.models.user.LibraryPreferences;
import org.openfinna.java.connector.exceptions.KirkesClientException;
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

    private static final String renewCountDelimiter = "/";
    private static final String renewCountRegex = "([0-9]+\\" + renewCountDelimiter + "[0-9]+)";
    private static final String dueDateRegex = "((?:[0-9]{1}.)|(?:[0-9]{2}.)){2}[0-9]+";
    private static final String expirationDateRegex = "(((?:[0-9]{1}.)|(?:[0-9]{2}.)){2}[0-9]+)";
    private static final String orderNoRegex = "([0-9]+)";
    private static final String hashKeyRegex = "^(.*)hashKey=([^#]+)";
    private static final String cardIdRegex = "^(.*)id=([^#]+)";
    private static final String priceRegex = "^^[^\\d]*(\\d+|\\d+((,|\\.)\\d{1,2}))(\\s|[a-zA-Z)]|€|$).*";
    private static final Pattern renewCountPattern = Pattern.compile(renewCountRegex);
    private static final Pattern expirationDatePattern = Pattern.compile(expirationDateRegex);
    private static final Pattern dueDatePattern = Pattern.compile(dueDateRegex);
    private static final Pattern orderNoPattern = Pattern.compile(orderNoRegex);
    private static final Pattern hashKeyPattern = Pattern.compile(hashKeyRegex);
    private static final Pattern cardIdPattern = Pattern.compile(cardIdRegex);
    private static final Pattern pricePattern = Pattern.compile(priceRegex);
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

    public static User parseUserDetails(String html, Building building) {
        User user = parseUserDetails(html);
        user.setBuilding(building);
        return user;
    }

    public static List<Building> getBuildings(String html) {
        Document document = Jsoup.parse(html);
        List<Building> buildings = new ArrayList<>();
        Elements orgLists = document.getElementsByClass("organisations list-group");
        if (orgLists.size() > 1) {
            Element orgList = orgLists.get(1);
            Elements orgs = orgList.getElementsByAttributeValue("data-link", "0");
            for (Element org : orgs) {
                if (org.hasAttr("data-organisation-name") && org.hasAttr("data-organisation")) {
                    String id = "0/" + org.attr("data-organisation") + "/";
                    String name = org.attr("data-organisation-name");
                    buildings.add(new Building(id, name));
                }
            }
        }
        return buildings;
    }

    public static String checkRenewResult(String html, Loan loan) throws KirkesClientException {
        Document document = Jsoup.parse(html);
        Element table = document.getElementsByClass("myresearch-table").first();
        if (table != null) {
            Elements loansHtml = table.getElementsByClass("myresearch-row");
            for (Element loanHtml : loansHtml) {
                String recordId = loanHtml.attributes().get("id").replace("record", "");
                if (recordId.equals(loan.getId())) {
                    // Alert banner element
                    Element resultElement = loanHtml.getElementsByClass("alert").first();
                    // If not null, proceed parsing
                    if (resultElement != null) {
                        if (resultElement.hasClass("alert-success")) {
                            // Success
                            return resultElement.wholeText();
                        } else {
                            // Failure
                            throw new KirkesClientException(resultElement.wholeText());
                        }
                    } else {
                        // Proceed to parse another error banner if possible
                        Element headerMsg = document.getElementsByClass("flash-message alert").first();
                        if (headerMsg != null)
                            throw new KirkesClientException(headerMsg.wholeText());
                    }
                }
            }
            // How did we get here? That must mean that renew failed
            throw new KirkesClientException("Renew failed");
        }
        throw new KirkesClientException("Something unexpected happened");
    }

    public static String extractHashKey(String html) {
        Document document = Jsoup.parse(html);
        Element hashKeyLink = document.getElementsByClass("placehold btn btn-primary hidden-print").first();
        if (hashKeyLink != null && hashKeyLink.hasAttr("href")) {
            String hrefLink = hashKeyLink.attr("href");
            Matcher hashKeyMatcher = hashKeyPattern.matcher(hrefLink);
            if (hashKeyMatcher.find()) {
                return hashKeyMatcher.group(2);
            }
        }
        return null;
    }

    public static HoldingDetails extractHoldingDetails(String html) {
        String info = null;
        List<HoldingDetails.HoldingType> types = new ArrayList<>();
        Document document = Jsoup.parse(html);
        // Elements
        Element groupIdSelect = document.getElementById("requestGroupId");
        Element infoTextElem = document.getElementsByClass("helptext").first();
        if (infoTextElem != null)
            info = infoTextElem.text();
        if (groupIdSelect != null) {
            Elements options = groupIdSelect.getElementsByTag("option");
            for (Element element : options) {
                String name = element.text().trim();
                String codeName = null;
                if (options.hasAttr("value"))
                    codeName = options.attr("value");
                types.add(new HoldingDetails.HoldingType(codeName, name));
            }
        }
        return new HoldingDetails(types, info);
    }

    /**
     * Get Home library / default
     *
     * @param html
     * @return PickupLocation or null
     */
    public static PickupLocation getHomeLibrary(String html) {
        Document document = Jsoup.parse(html);
        // Elements
        Element homeLibElement = document.getElementById("home_library");
        if (homeLibElement != null) {
            Element selectedLib = homeLibElement.getElementsByAttributeValue("selected", "selected").first();
            PickupLocation selected = new PickupLocation(null, null);
            if (selectedLib != null) {
                selected.setName(selectedLib.text());
                if (selectedLib.hasAttr("value"))
                    selected.setId(selectedLib.attr("value"));
            }
            return selected;
        }
        return null;
    }

    public static boolean getHomeLibraryResult(String html) {
        Document document = Jsoup.parse(html);
        return document.getElementsByClass("flash-message alert alert-success").size() > 0;
    }

    public static Fines extractFines(String html) {
        Document document = Jsoup.parse(html);
        Fines fines = new Fines();
        fines.setCurrency("€");
        Element opdElement = document.getElementsByClass("text-right online-payment-data").first();
        if (opdElement != null) {
            Element amount = opdElement.getElementsByClass("amount").first();
            if (amount != null) {
                Matcher amountMatcher = pricePattern.matcher(amount.text());
                if (amountMatcher.find()) {
                    String priceText = amountMatcher.group(1).replace(",", ".");
                    double priceDouble = Double.parseDouble(priceText);
                    fines.setPayableDue(priceDouble);
                }
            }
        }

        Element finesTable = document.getElementsByClass("table table-striped useraccount-table online-payment").first();
        Element totalElement = document.getElementsByClass("total-balance").first();
        if (totalElement != null) {
            Element amount = totalElement.getElementsByClass("amount").first();
            if (amount != null) {
                Matcher amountMatcher = pricePattern.matcher(amount.text());
                if (amountMatcher.find()) {
                    String priceText = amountMatcher.group(1).replace(",", ".");
                    double priceDouble = Double.parseDouble(priceText);
                    fines.setTotalDue(priceDouble);
                }
            }
        }

        if (finesTable != null) {
            Elements fineItems = finesTable.getElementsByTag("tr");
            for (Element fineItem : fineItems) {
                if (!fineItem.hasAttr("class") && !fineItem.hasClass("headers")) {
                    Fine fine = new Fine();
                    Element balanceElem = fineItem.getElementsByClass("balance").first();
                    Element dateElem = fineItem.getElementsByClass("hidden-xs").first();
                    if (balanceElem != null) {
                        Matcher priceSearch = pricePattern.matcher(balanceElem.text());
                        if (priceSearch.find()) {
                            String priceText = priceSearch.group(1).replace(",", ".");
                            double priceDouble = Double.parseDouble(priceText);
                            fine.setPrice(priceDouble);
                        }
                    }
                    if (dateElem != null) {
                        Matcher dateRegex = expirationDatePattern.matcher(dateElem.text());
                        if (dateRegex.find()) {
                            String dateTxt = dateRegex.group(1);
                            try {
                                Date date = dateFormat.parse(dateTxt);
                                fine.setRegistrationDate(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Elements elements = fineItem.getElementsByTag("td");
                    if (elements.size() > 2) {
                        Element descElement = elements.get(3);
                        fine.setDescription(descElement.text());
                    }
                    if (fine.getPrice() != -1) {
                        fines.getFines().add(fine);
                    }
                }
            }
        }
        return fines;
    }

    public static List<PickupLocation> getHomeLibraries(String html) {
        Document document = Jsoup.parse(html);
        // Elements
        Element homeLibElement = document.getElementById("home_library");
        Elements libs = homeLibElement.getElementsByTag("option");
        List<PickupLocation> items = new ArrayList<>();
        for (Element item : libs) {
            PickupLocation location = new PickupLocation(null, null);
            if (item != null) {
                location.setName(item.text());
                if (item.hasAttr("value"))
                    location.setId(item.attr("value"));
            }
            items.add(location);
        }
        return items;
    }

    /**
     * Get current account's card id from user info page
     *
     * @param html HTML content
     * @return Card ID or null
     */
    public static String getCurrentCardId(String html) {
        Document document = Jsoup.parse(html);
        Element passwordChangeButton = document.getElementsByClass("change-password-link").first();
        if (passwordChangeButton != null) {
            Element link = passwordChangeButton.getElementsByTag("a").first();
            if (link != null && link.hasAttr("href")) {
                Matcher cardIdMatcher = cardIdPattern.matcher(link.attr("href"));
                if (cardIdMatcher.find()) {
                    return cardIdMatcher.group(2);
                }
            }
        }
        return null;
    }

    public static UserType getActiveChain(String html) {
        Document document = Jsoup.parse(html);
        Element libraryList = document.getElementById("login_target");
        Elements items = libraryList.getElementsByTag("option");
        for (Element item : items) {
            if (item.hasAttr("selected") && item.attr("selected").equals("selected")) {
                // HAH! GOT EM!
                return new UserType(item.attr("value"), item.text());
            }
        }
        return null;
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
                    image = WebClient.optimizeURL(WebClient.kirkesBaseURL) + StringEscapeUtils.unescapeJava(imageElem.attributes().get("src"));

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
                    Matcher renewCountMatcher = renewCountPattern.matcher(text.text().replace(" ", ""));
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
