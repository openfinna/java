package org.openfinna.java.connector.utils;

import java.util.Locale;

public class BuildingUtils {

    public static String optimizeName(String name, String codeName) {
        name = name.toLowerCase(Locale.ROOT);
        codeName = codeName.toLowerCase(Locale.ROOT);
        if (name.endsWith("- " + codeName))
            name = name.replace("- " + codeName, "");
        name = name.replace("-", "").replace(" ", "");
        return name;
    }
}
