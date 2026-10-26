package com.mycompany.onlinefruitshopping.dao.report;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The report filters must speak the same vocabulary as the real
 * {@code orders.order_status} column (see DeliveryRules), otherwise
 * the SQL silently matches no rows.
 */
class CommissionReportNormalizationTest {

    @Test
    void statusVocabularyMatchesTheOrderLifecycle() {
        assertEquals(List.of("Pending", "Confirmed", "Shipping",
                        "Completed", "Cancelled"),
                CommissionReportDAO.STATUSES);
        assertEquals("Completed", CommissionReportDAO.DELIVERED_STATUS);
        assertTrue(CommissionReportDAO.STATUSES
                .contains(CommissionReportDAO.DELIVERED_STATUS));
        assertTrue(CommissionReportDAO.REFUND_STATUSES.contains("Cancelled"));
    }

    @Test
    void statusFilterIsCaseInsensitiveAndReturnsCanonicalValue() {
        assertEquals("Pending", CommissionReportDAO.normalizeStatus("pending"));
        assertEquals("Confirmed", CommissionReportDAO.normalizeStatus("CONFIRMED"));
        assertEquals("Shipping", CommissionReportDAO.normalizeStatus(" shipping "));
        assertEquals("Completed", CommissionReportDAO.normalizeStatus("completed"));
        assertEquals("Cancelled", CommissionReportDAO.normalizeStatus("Cancelled"));
    }

    @Test
    void oldLowercaseVocabularyIsNoLongerAccepted() {
        for (String stale : new String[]{
                "delivered", "preparing", "pending_confirmation", "failed"}) {
            assertEquals("", CommissionReportDAO.normalizeStatus(stale),
                    "stale status must not reach orders.order_status: " + stale);
        }
    }

    @Test
    void unknownOrMaliciousStatusMeansNoSlice() {
        for (String value : new String[]{
                null, "", "   ", "' OR 1=1", "Completed; DROP TABLE orders"}) {
            assertEquals("", CommissionReportDAO.normalizeStatus(value));
        }
    }

    @Test
    void acceptedStatusesAreAlwaysMembersOfTheVocabulary() {
        for (String candidate : new String[]{
                "Pending", "pending", "Completed", "cancelled", "nope"}) {
            String normalized = CommissionReportDAO.normalizeStatus(candidate);
            if (!normalized.isEmpty()) {
                assertTrue(CommissionReportDAO.STATUSES.contains(normalized));
                assertFalse(normalized.equals(normalized.toLowerCase()));
            }
        }
    }

    @Test
    void dateFilterOnlyAcceptsIsoDates() {
        assertEquals("2026-10-26", CommissionReportDAO.normalizeDate("2026-10-26"));
        assertEquals("2026-10-26", CommissionReportDAO.normalizeDate(" 2026-10-26 "));
        assertEquals("", CommissionReportDAO.normalizeDate("2026-13-01"));
        assertEquals("", CommissionReportDAO.normalizeDate("26/10/2026"));
        assertEquals("", CommissionReportDAO.normalizeDate("2026-10-26' OR 1=1"));
        assertEquals("", CommissionReportDAO.normalizeDate(null));
        assertEquals("", CommissionReportDAO.normalizeDate(""));
    }
}
