package com.mycompany.onlinefruitshopping.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the HTML for one {@link Page}: a "showing X-Y of Z" note
 * plus Previous / page numbers / Next links.
 *
 * <p>The caller supplies a base URL that already contains every
 * filter it wants to keep (e.g. a search keyword) and ends with
 * {@code ?} or {@code &}; the pager simply appends
 * {@code page=N}.</p>
 */
public final class Pager {

    /**
     * Maximum number of numbered links shown around the current page.
     */
    private static final int WINDOW = 5;

    private Pager() {
    }

    /**
     * Builds the pager markup. Returns an empty string when there is
     * only a single page, so callers can render it unconditionally.
     *
     * @param page     the slice being displayed
     * @param baseUrl  URL that already ends with {@code ?} or {@code &}
     *                 and carries every filter to keep
     * @param pageParam name of the page parameter (e.g. {@code page})
     */
    public static String render(Page<?> page, String baseUrl, String pageParam) {

        if (page == null || page.getTotalPages() <= 1) {
            return "";
        }

        String url = baseUrl == null ? "?" : baseUrl;
        String param = pageParam == null || pageParam.isEmpty()
                ? "page"
                : pageParam;

        StringBuilder html = new StringBuilder();

        html.append("<div class=\"pager\">");

        html.append("<span class=\"page-info\">Showing ")
            .append(page.getFromRow())
            .append("&ndash;")
            .append(page.getToRow())
            .append(" of ")
            .append(page.getTotal())
            .append("</span>");

        html.append("<span class=\"pager-links\">");

        html.append(page.hasPrevious()
                ? link(url, param, page.getPage() - 1, "&laquo; Prev")
                : "<span class=\"pager-disabled\">&laquo; Prev</span>");

        for (int number : window(page.getPage(), page.getTotalPages())) {
            html.append(number == page.getPage()
                    ? "<span class=\"pager-current\">" + number + "</span>"
                    : link(url, param, number, String.valueOf(number)));
        }

        html.append(page.hasNext()
                ? link(url, param, page.getPage() + 1, "Next &raquo;")
                : "<span class=\"pager-disabled\">Next &raquo;</span>");

        html.append("</span>");

        html.append("</div>");

        return html.toString();
    }

    /**
     * Convenience overload using the default {@code page} parameter.
     */
    public static String render(Page<?> page, String baseUrl) {
        return render(page, baseUrl, "page");
    }

    /**
     * The page numbers to display: a sliding window of at most
     * {@link #WINDOW} entries, always containing the first and last
     * page when they are close enough.
     */
    private static List<Integer> window(int current, int totalPages) {

        List<Integer> numbers = new ArrayList<>();

        int half = WINDOW / 2;
        int start = Math.max(1, current - half);
        int end = Math.min(totalPages, start + WINDOW - 1);

        start = Math.max(1, end - WINDOW + 1);

        for (int number = start; number <= end; number++) {
            numbers.add(number);
        }

        return numbers;
    }

    private static String link(String baseUrl, String param, int page, String label) {
        return "<a href=\"" + baseUrl + param + "=" + page + "\">" + label + "</a>";
    }
}
