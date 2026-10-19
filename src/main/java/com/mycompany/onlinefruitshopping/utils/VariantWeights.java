package com.mycompany.onlinefruitshopping.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helpers for the weight of a product variant.
 *
 * <p>A variant is one selling package, so what tells two variants of
 * the same fruit apart is the weight of the package: half a kilo, one
 * kilo, two kilos ... Weight is therefore measured in kilograms, and
 * {@code packaging_type} is only the label of the bag or carton.</p>
 */
public final class VariantWeights {

    private VariantWeights() {
    }

    /** The unit every variant weight is measured in. */
    public static final String UNIT = "kg";

    /**
     * Canonical, comparison friendly form of a weight, e.g.
     * {@code "0.5"}, {@code "1"}, {@code "2"}. Never null, and empty
     * when the variant has no weight.
     */
    public static String key(Object weight) {
        BigDecimal value = decimal(weight);

        return value == null
                ? ""
                : value.stripTrailingZeros().toPlainString();
    }

    /**
     * Ready to display package size, e.g. {@code "0.5 kg"}, or an
     * empty string when the variant has no weight.
     */
    public static String label(Object weight, Object unit) {
        String value = key(weight);

        if (value.isEmpty()) {
            return "";
        }

        String suffix = unit == null || unit.toString().isBlank()
                ? UNIT
                : unit.toString().trim();

        return value + " " + suffix;
    }

    /**
     * The distinct weights of a variant list, ascending, each one in
     * the canonical {@link #key} form so it can feed a drop-down
     * filter.
     */
    public static List<String> options(List<Map<String, Object>> variants) {
        List<String> keys = new ArrayList<>();

        for (Map<String, Object> variant : variants) {
            String value = key(variant.get("weight_value"));

            if (!value.isEmpty() && !keys.contains(value)) {
                keys.add(value);
            }
        }

        keys.sort((left, right) -> new BigDecimal(left)
                .compareTo(new BigDecimal(right)));

        return keys;
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
