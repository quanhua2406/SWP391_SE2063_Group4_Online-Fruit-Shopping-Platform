package com.mycompany.onlinefruitshopping.utils;

/**
 * Case-insensitive helpers used by the list screens to apply a search
 * box and drop-down filters on top of the data already loaded by the
 * DAOs.
 *
 * <p>Keeping the matching here means every screen filters the same
 * way and no DAO has to grow a "search" variant.</p>
 */
public final class Filters {

    private Filters() {
    }

    /**
     * Normalises search text: trimmed, lower-cased and never null.
     */
    public static String text(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    /**
     * True when the needle appears in any of the given values.
     * A blank needle matches everything, so an empty search box keeps
     * the whole list.
     */
    public static boolean any(String needle, Object... values) {

        if (needle == null || needle.isEmpty()) {
            return true;
        }

        for (Object value : values) {
            if (value != null
                    && value.toString().toLowerCase().contains(needle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * True when the filter is blank (meaning "all") or matches the
     * value exactly, ignoring case.
     */
    public static boolean same(String filter, Object value) {

        if (filter == null || filter.isEmpty()) {
            return true;
        }

        return value != null && filter.equalsIgnoreCase(value.toString());
    }
}
