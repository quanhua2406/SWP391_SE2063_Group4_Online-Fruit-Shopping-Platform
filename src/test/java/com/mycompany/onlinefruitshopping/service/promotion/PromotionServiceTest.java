package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.promotion.PromotionDAO;
import com.mycompany.onlinefruitshopping.model.Promotion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromotionServiceTest {
    private Promotion promotion(PromotionService service) {
        Promotion p = service.createDraft(); p.setName("Giảm giá cuối tuần");
        p.setShopId(2); p.setProductIds(List.of(5, 7)); return p;
    }
    @Test void promotionRejectsInvalidDiscountBeforeSaveAndDeduplicatesProducts() throws Exception {
        RecordingDAO dao = new RecordingDAO();
        PromotionService service = new PromotionService(dao);
        Promotion p = promotion(service); p.setDiscountValue(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class, () -> service.save(10, p));
        assertEquals(0, dao.calls);
        p.setDiscountValue(BigDecimal.TEN); p.setProductIds(List.of(5, 7, 5));
        assertEquals(8, service.save(10, p));
        assertEquals(List.of(5, 7), dao.saved.getProductIds());
        assertEquals(10, dao.ownerId);
    }
    @Test void existingPromotionCannotChangeShopDuringFormMapping() {
        PromotionService service = new PromotionService(new RecordingDAO());
        Promotion p = promotion(service); p.setId(8);
        assertThrows(IllegalArgumentException.class, () -> service.validateShopChange(p, 99));
        assertEquals(2, p.getShopId());
    }
    private static class RecordingDAO extends PromotionDAO {
        int calls, ownerId; Promotion saved;
        @Override public int save(int ownerId, Promotion promotion) {
            calls++; this.ownerId = ownerId; saved = promotion; return 8;
        }
    }
}
