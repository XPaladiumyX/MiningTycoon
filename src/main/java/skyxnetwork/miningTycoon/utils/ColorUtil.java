package skyxnetwork.miningTycoon.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling color codes in Minecraft
 * Supports both § and & color code formats
 */
public class ColorUtil {

    /**
     * Translates color codes from & to § format
     * Supports all Minecraft color codes (0-9, a-f, k-o, r)
     *
     * @param text The text to translate
     * @return The translated text with § color codes
     */
    public static String translate(String text) {
        if (text == null) {
            return null;
        }

        // Replace & with § for all color codes
        return text.replace('&', '§');
    }

    /**
     * Translates a list of strings with color codes
     *
     * @param list The list of strings to translate
     * @return The translated list
     */
    public static List<String> translate(List<String> list) {
        if (list == null) {
            return null;
        }

        List<String> translated = new ArrayList<>();
        for (String line : list) {
            translated.add(translate(line));
        }
        return translated;
    }

    /**
     * Strips all color codes from a string (both § and &)
     *
     * @param text The text to strip
     * @return The text without color codes
     */
    public static String stripColor(String text) {
        if (text == null) {
            return null;
        }

        // Remove all § color codes
        text = text.replaceAll("§[0-9a-fk-or]", "");
        // Remove all & color codes
        text = text.replaceAll("&[0-9a-fk-or]", "");

        return text;
    }

    /**
     * Checks if a string contains color codes
     *
     * @param text The text to check
     * @return true if the text contains color codes
     */
    public static boolean hasColorCodes(String text) {
        if (text == null) {
            return false;
        }

        return text.matches(".*[§&][0-9a-fk-or].*");
    }
}