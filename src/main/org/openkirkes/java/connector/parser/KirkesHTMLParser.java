package org.openkirkes.java.connector.parser;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openkirkes.java.connector.classes.models.Resource;
import org.openkirkes.java.connector.classes.models.User;
import org.openkirkes.java.connector.classes.models.loans.Loan;
import org.openkirkes.java.connector.classes.models.user.KirkesPreferences;
import org.openkirkes.java.connector.classes.models.user.LibraryPreferences;

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
    private static final Pattern renewCountPattern = Pattern.compile(renewCountRegex);
    private static final Pattern dueDatePattern = Pattern.compile(dueDateRegex);

    public static String parseCSRF(String html) {
        Document document = Jsoup.parse(html);
        Element csrfElement = getOneOrNull(document.getElementsByAttributeValue("name", "csrf"));
        if (csrfElement != null)
            if (csrfElement.hasAttr("value"))
                return csrfElement.attr("value");
        return null;
    }

    private static Element getOneOrNull(Elements elements) {
        if (elements.size() > 0)
            return elements.get(0);
        else
            return null;
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
        Element fullName = getOneOrNull(document.getElementsByClass("username login-text"));
        if (fullName != null) {
            String name = fullName.text().trim();
            user.setName(name);
            libraryPreferences.setFullName(name);
        }

        Element libraryForm = document.getElementById("profile_library_form");
        Element kirkesForm = getOneOrNull(document.getElementsByAttributeValue("name", "my_profile_form"));

        if (libraryForm != null) {
            Elements libValues = libraryForm.getElementsByClass("profile-text-value");
            if (libValues.size() > 4) {
                libraryPreferences.setFirstName(libValues.get(0).text());
                libraryPreferences.setSurname(libValues.get(1).text());
                libraryPreferences.setAddress(libValues.get(2).text());
                libraryPreferences.setZipcode(libValues.get(3).text());
                libraryPreferences.setCity(libValues.get(4).text());
            }

            Element libPhone = getOneOrNull(document.getElementsByClass("profile_tel"));
            if (libPhone != null && libPhone.hasAttr("value"))
                libraryPreferences.setPhoneNumber(libPhone.attr("value"));

            Element libEmail = getOneOrNull(document.getElementsByClass("profile_email"));
            if (libEmail != null && libEmail.hasAttr("value"))
                libraryPreferences.setEmail(libEmail.attr("value"));
        }

        if (kirkesForm != null) {
            System.out.println("Kirkes FOUND!");
            Element kirkesEmail = getOneOrNull(document.getElementsByAttributeValue("name", "email"));
            Element kirkesNick = getOneOrNull(document.getElementsByAttributeValue("name", "finna_nickname"));

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

        Element table = getOneOrNull(document.getElementsByClass("myresearch-table"));
        if (table != null) {
            Elements loansHtml = table.getElementsByClass("myresearch-row");
            for (Element loanHtml : loansHtml) {
                String recordId = loanHtml.attributes().get("id").replace("record", "");
                String title = null, type = null, author = null, image = null, renewId = null;
                int renewsUsed = 0, renewsTotal = 0;
                Date dueDate = null;

                // Init necessary elements
                Element inputOne = getOneOrNull(loanHtml.getElementsByAttributeValue("name", "renewAllIDS[]"));
                Element inputTwo = getOneOrNull(loanHtml.getElementsByAttributeValue("name", "selectAllIDS[]"));
                Element titleElem = getOneOrNull(loanHtml.getElementsByClass("record-title"));
                Element metadataElem = getOneOrNull(loanHtml.getElementsByClass("record-core-metadata"));
                Element typeElem = getOneOrNull(loanHtml.getElementsByClass("label-info"));
                Element imageElem = getOneOrNull(loanHtml.getElementsByClass("recordcover"));
                Elements textElements = loanHtml.getElementsByTag("strong");

                if (titleElem != null)
                    title = titleElem.text();

                if (typeElem != null)
                    type = typeElem.text();

                if (imageElem != null)
                    image = StringEscapeUtils.unescapeJava(imageElem.attributes().get("src"));

                if (metadataElem != null) {
                    Element authorUrl = getOneOrNull(metadataElem.getElementsByTag("a"));
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
}
