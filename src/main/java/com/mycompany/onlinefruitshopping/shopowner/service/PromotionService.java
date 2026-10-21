package com.mycompany.onlinefruitshopping.shopowner.service;

import com.mycompany.onlinefruitshopping.shopowner.dao.PromotionDAO;
import com.mycompany.onlinefruitshopping.shopowner.model.Promotion;
import com.mycompany.onlinefruitshopping.shopowner.model.PromotionPrice;
import com.mycompany.onlinefruitshopping.shopowner.model.PromotionProduct;
import com.mycompany.onlinefruitshopping.shopowner.model.ShopOption;
import com.mycompany.onlinefruitshopping.shopowner.utils.PromotionRules;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Chuẩn bị và kiểm tra chương trình khuyến mãi trước khi lưu. */
public class PromotionService {
    private final PromotionDAO promotionDAO;

    public PromotionService() { this(new PromotionDAO()); }

    PromotionService(PromotionDAO promotionDAO) { this.promotionDAO = promotionDAO; }

    public Promotion createDraft() {
        Promotion promotion = new Promotion();
        promotion.setDiscountValue(new BigDecimal("10"));
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

    public List<PromotionProduct> findProducts() throws SQLException {
        return promotionDAO.findProducts();
    }

    public List<PromotionPrice> previewPrices(int ownerId, int id) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        ServiceChecks.positiveId(id);
        return promotionDAO.previewPrices(ownerId, id);
    }

    public void validateShopChange(Promotion promotion, int shopId) {
        ServiceChecks.positiveId(shopId);
        if (promotion.getId() > 0 && promotion.getShopId() != shopId) {
            throw new IllegalArgumentException("Không được chuyển chương trình sang cửa hàng khác.");
        }
    }

    public int save(int ownerId, Promotion promotion) throws SQLException {
        ServiceChecks.positiveId(ownerId);
        if (promotion == null || promotion.getId() < 0) {
            throw new IllegalArgumentException("Thông tin chương trình không hợp lệ.");
        }
        ServiceChecks.version(promotion.getVersion());
        PromotionRules.validate(promotion);
        // Một sản phẩm được chọn nhiều lần chỉ lưu một lần.
        promotion.setProductIds(new ArrayList<>(new LinkedHashSet<>(promotion.getProductIds())));
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
