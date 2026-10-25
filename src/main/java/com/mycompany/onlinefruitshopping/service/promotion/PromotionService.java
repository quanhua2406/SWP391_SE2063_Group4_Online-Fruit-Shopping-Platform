package com.mycompany.onlinefruitshopping.service.promotion;

import com.mycompany.onlinefruitshopping.dao.promotion.PromotionDAO;
import com.mycompany.onlinefruitshopping.model.Promotion;
import com.mycompany.onlinefruitshopping.model.PromotionPrice;
import com.mycompany.onlinefruitshopping.model.PromotionProduct;
import com.mycompany.onlinefruitshopping.model.ShopOption;
import com.mycompany.onlinefruitshopping.service.common.ServiceChecks;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class PromotionService {
    private final PromotionDAO promotionDAO;

    public PromotionService() { this(new PromotionDAO()); }

    PromotionService(PromotionDAO promotionDAO) { this.promotionDAO = promotionDAO; }

    public Promotion createDraft() {
        Promotion promotion = new Promotion();
        promotion.setDiscountValue(java.math.BigDecimal.TEN);
        promotion.setStartsAt(PromotionRules.now().withSecond(0));
        promotion.setEndsAt(promotion.getStartsAt().plusDays(7));
        return promotion;
    }

    public List<Promotion> findAll(int ownerId) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        return promotionDAO.findAll(ownerId);
    }

    public Promotion findById(int ownerId, int id) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        return promotionDAO.findById(ownerId, id);
    }

    public List<ShopOption> findShops(int ownerId) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        return promotionDAO.findShops(ownerId);
    }

    public List<PromotionProduct> findProducts(int ownerId) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        return promotionDAO.findProducts(ownerId);
    }

    public List<PromotionPrice> previewPrices(int ownerId, int id) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        return promotionDAO.previewPrices(ownerId, id);
    }

    public void validateShopChange(Promotion promotion, int shopId) {
        ServiceChecks.positiveId(shopId);
        if (promotion != null && promotion.getId() > 0 && promotion.getShopId() != shopId) {
            throw new IllegalArgumentException("Khong duoc chuyen chuong trinh sang cua hang khac.");
        }
    }

    public int save(int ownerId, Promotion promotion) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        if (promotion == null || promotion.getId() < 0) {
            throw new IllegalArgumentException("Thong tin chuong trinh khong hop le.");
        }
        ServiceChecks.version(promotion.getVersion());
        PromotionRules.validate(promotion);
        if (promotion.getProductIds() != null) {
            promotion.setProductIds(new ArrayList<>(new LinkedHashSet<>(promotion.getProductIds())));
        }
        return promotionDAO.save(ownerId, promotion);
    }

    public void setActive(int ownerId, int id, int version, boolean active) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        ServiceChecks.version(version);
        promotionDAO.setActive(ownerId, id, version, active);
    }

    public void delete(int ownerId, int id, int version) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        ServiceChecks.version(version);
        promotionDAO.delete(ownerId, id, version);
    }
}