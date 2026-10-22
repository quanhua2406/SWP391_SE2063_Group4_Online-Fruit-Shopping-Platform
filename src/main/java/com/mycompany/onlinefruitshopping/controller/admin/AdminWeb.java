package com.mycompany.onlinefruitshopping.controller.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Small helpers shared by the admin order / delivery screens
 * (feature 19.0): request-parameter trimming and pager URL
 * building.
 */
final class AdminWeb {

    private AdminWeb() {
    }

    /**
     * Trim a request parameter, turning {@code null} into an empty
     * string.
     */
    static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Parse a positive integer parameter, falling back to the
     * given default when the value is missing or malformed.
     */
    static int intParam(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(trim(value));
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * Build a base URL that keeps the given filter parameters and
     * ends with {@code ?} or {@code &} so the shared
     * {@code Pager} can append {@code page=N}. Empty parameters
     * are skipped.
     */
    static String pagerBase(HttpServletRequest request, String path, String... names)
            throws UnsupportedEncodingException {

        StringBuilder url = new StringBuilder(request.getContextPath())
                .append(path)
                .append("?");

        for (String name : names) {
            String value = trim(request.getParameter(name));

            if (request.getAttribute(name) != null) {
                value = request.getAttribute(name).toString();
            }

            if (!value.isEmpty()) {
                url.append(name).append("=")
                    .append(URLEncoder.encode(value, "UTF-8"))
                    .append("&");
            }
        }

        return url.toString();
    }

    /**
     * HTML-escape a value for safe output inside the JSP pages.
     */
    static String esc(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
