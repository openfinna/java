package org.openkirkes.java.connector.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
}
