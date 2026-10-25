package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VariantPromotionServiceTest {
    @Test void ownershipGatesSaveAndRemovalAndValidRequestKeepsActorAndDates() throws Exception {
        RecordingDAO dao = new RecordingDAO(); VariantPromotionService service = new VariantPromotionService(dao);
        String from = LocalDate.now().toString(), to = LocalDate.now().plusDays(3).toString();
        assertEquals("forbidden", service.savePromotion(10, "7", "80", from, to));
        assertEquals("forbidden", service.removePromotion(10, "7")); assertEquals(0, dao.writes);
        dao.owns = true;
        assertEquals("promo-not-discount", service.savePromotion(10, "7", "100", from, to));
        assertEquals(0, dao.writes);
        assertEquals("promo-set", service.savePromotion(10, "7", "80", from, to));
        assertEquals(Date.valueOf(to), dao.end);
        assertEquals("promo-removed", service.removePromotion(10, "7")); assertEquals(2, dao.writes);
    }
    @Test void malformedAndExpiredRequestsDoNotWrite() throws Exception {
        RecordingDAO dao = new RecordingDAO(); VariantPromotionService service = new VariantPromotionService(dao);
        assertEquals("promo-invalid", service.savePromotion(10, "bad", "80", "bad", "bad"));
        String yesterday = LocalDate.now().minusDays(1).toString();
        assertEquals("promo-expired", service.savePromotion(10, "7", "80", yesterday, yesterday));
        assertEquals("invalid", service.removePromotion(10, "0")); assertEquals(0, dao.writes);
    }
    private static class RecordingDAO extends InventoryDAO {
        boolean owns; int writes; Date end;
        @Override public boolean variantBelongsToOwner(int variant, int owner) { assertEquals(7,variant); assertEquals(10,owner); return owns; }
        @Override public Double getVariantPrice(int id) { return 100d; }
        @Override public boolean setPromotion(int variant, BigDecimal price, Date from, Date to, int owner) {
            assertEquals(7,variant); assertEquals(10,owner); assertEquals(new BigDecimal("80"),price); writes++; end = to; return true;
        }
        @Override public boolean removePromotion(int variant, int owner) { assertEquals(7,variant); assertEquals(10,owner); writes++; return true; }
    }
}
