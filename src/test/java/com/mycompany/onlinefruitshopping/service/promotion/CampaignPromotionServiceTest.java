package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.promotion.PromotionDAO;
import com.mycompany.onlinefruitshopping.model.Promotion;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CampaignPromotionServiceTest {
    private Promotion promotion(CampaignPromotionService service) {
        Promotion promotion = service.createDraft();
        promotion.setName("Giảm giá cuối tuần");
        promotion.setShopId(2);
        promotion.setProductIds(List.of(5, 7));
        return promotion;
    }

    @Test void promotionRejectsInvalidDiscountBeforeSaveAndDeduplicatesProducts() throws SQLException {
        RecordingPromotionDAO dao = new RecordingPromotionDAO();
        CampaignPromotionService service = new CampaignPromotionService(dao);
        Promotion promotion = promotion(service);
        promotion.setDiscountValue(new BigDecimal("101"));
        assertThrows(IllegalArgumentException.class, () -> service.save(10, promotion));
        assertEquals(0, dao.calls);
        promotion.setDiscountValue(new BigDecimal("10"));
        promotion.setProductIds(List.of(5, 7, 5));
        assertEquals(8, service.save(10, promotion));
        assertEquals(List.of(5, 7), dao.saved.getProductIds());
        assertEquals(10, dao.ownerId);
    }

    private static class RecordingPromotionDAO extends PromotionDAO {
        int calls, ownerId;
        Promotion saved;
        @Override public int save(int ownerId, Promotion promotion) {
            calls++;
            this.ownerId = ownerId;
            this.saved = promotion;
            return 8;
        }
    }

    @Test void existingPromotionCannotChangeShopDuringFormMapping() {
        CampaignPromotionService service = new CampaignPromotionService(new RecordingPromotionDAO());
        Promotion promotion = promotion(service);
        promotion.setId(8);
        assertThrows(IllegalArgumentException.class, () -> service.validateShopChange(promotion, 99));
        assertEquals(2, promotion.getShopId());
    }
}
