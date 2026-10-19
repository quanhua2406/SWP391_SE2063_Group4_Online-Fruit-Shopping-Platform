package com.mycompany.onlinefruitshopping.utils;

import java.util.Collections;
import java.util.List;

/**
 * A slice of a list, used to render one page of a table.
 *
 * <p>This is a small, dependency-free helper shared by the shop
 * owner and admin screens. Each list is sliced in memory after the
 * DAO has loaded it, so pagination can be added to any list without
 * touching its query.</p>
 *
 * @param <T> type of the rows held by the page
 */
public final class Page<T> {

    /**
     * Default number of rows shown per page.
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * Largest page size accepted from the request.
     */
    private static final int MAX_SIZE = 200;

    private final List<T> items;
    private final int page;
    private final int size;
    private final int total;

    private Page(List<T> items, int page, int size, int total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
    }

    // =====================================================
    // FACTORY
    // =====================================================

    /**
     * Slices {@code all} into the requested page. A missing list is
     * treated as empty; the page number is clamped so an out-of-range
     * link still shows something.
     */
    public static <T> Page<T> of(List<T> all, int requestedPage, int requestedSize) {

        List<T> source = all == null ? Collections.emptyList() : all;

        int size = requestedSize <= 0
                ? DEFAULT_SIZE
                : Math.min(requestedSize, MAX_SIZE);

        int total = source.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
        int page = Math.min(Math.max(1, requestedPage), totalPages);

        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);

        return new Page<>(source.subList(from, to), page, size, total);
    }

    /**
     * Slices with the default page size.
     */
    public static <T> Page<T> of(List<T> all, int requestedPage) {
        return of(all, requestedPage, DEFAULT_SIZE);
    }

    /**
     * Parses a {@code page} request parameter. Anything that is not a
     * positive integer falls back to page 1.
     */
    public static int parsePage(String value) {
        if (value == null) {
            return 1;
        }

        try {
            int page = Integer.parseInt(value.trim());
            return page > 0 ? page : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // =====================================================
    // ACCESSORS
    // =====================================================

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil(total / (double) size));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < getTotalPages();
    }

    /**
     * 1-based index of the first row on this page.
     */
    public int getFromRow() {
        return total == 0 ? 0 : (page - 1) * size + 1;
    }

    /**
     * 1-based index of the last row on this page.
     */
    public int getToRow() {
        return Math.min(page * size, total);
    }
}
