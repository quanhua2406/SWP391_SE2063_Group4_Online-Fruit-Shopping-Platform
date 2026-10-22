package com.mycompany.onlinefruitshopping.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Builds the URL handed to {@link Pager}: the current path plus the
 * filter parameters that must survive a page change.
 *
 * <p>The result always ends with {@code ?} or {@code &} so the pager
 * can append its own page parameter.</p>
 */
public final class QueryUrl {

    private QueryUrl() {
    }

    /**
     * @param request current request
     * @param path    e.g. {@code contextPath + "/store-owner/pricing"}
     * @param names   request parameters to keep, in order
     * @return e.g. {@code /app/store-owner/pricing?q=apple&unit=kg&}
     */
    public static String keep(HttpServletRequest request,
                              String path,
                              String... names) {

        StringBuilder url = new StringBuilder(
                path == null ? "" : path);

        // The caller may pass a path that already ends with "?" or "&"
        // (for example ".../admin/categories?"); make sure we add
        // exactly one separator.
        if (url.length() == 0
                || (url.charAt(url.length() - 1) != '?'
                    && url.charAt(url.length() - 1) != '&')) {
            url.append('?');
        }

        for (String name : names) {
            String value = request.getParameter(name);

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            url.append(name).append('=')
               .append(encode(value.trim()))
               .append('&');
        }

        return url.toString();
    }

    /**
     * URL-encodes a single value with UTF-8.
     */
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
