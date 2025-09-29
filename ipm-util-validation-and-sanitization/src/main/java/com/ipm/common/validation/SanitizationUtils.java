package main.java.com.ipm.common.validation;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.encoder.Encode;

/**
 * Utility for sanitizing user input to prevent XSS, HTML injection, etc.
 */
public class SanitizationUtils {

    /**
     * Removes all HTML tags except basic safe ones.
     */
    public static String cleanBasicHtml(String input) {
        return input == null ? null : Jsoup.clean(input, Safelist.basic());
    }

    /**
     * Strips all HTML tags (plain text only).
     */
    public static String stripAllHtml(String input) {
        return input == null ? null : Jsoup.clean(input, Safelist.none());
    }

    /**
     * Encodes for safe HTML output (prevents XSS when rendering).
     */
    public static String encodeForHtml(String input) {
        return input == null ? null : Encode.forHtml(input);
    }
}