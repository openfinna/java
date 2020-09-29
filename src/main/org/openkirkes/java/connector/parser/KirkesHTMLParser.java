package org.openkirkes.java.connector.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openkirkes.java.connector.classes.models.User;
import org.openkirkes.java.connector.classes.models.user.KirkesPreferences;
import org.openkirkes.java.connector.classes.models.user.LibraryPreferences;

/**
 * This class parses different things from JSON/HTML
 */
public class KirkesHTMLParser {

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
}
