package com.mycompany.onlinefruitshopping.dao.promotion;
import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import com.mycompany.onlinefruitshopping.model.Promotion;
import com.mycompany.onlinefruitshopping.utils.PromotionRules;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
class PromotionDatabaseTest {
 ModuleDatabase db; PromotionDAO dao;
 @BeforeEach void setup() throws Exception {db=new ModuleDatabase();dao=new PromotionDAO(db::open);}
 Promotion campaign(int product){Promotion p=new Promotion();p.setShopId(1);p.setName("Campaign");p.setDiscountValue(BigDecimal.TEN);p.setStartsAt(PromotionRules.now().minusHours(1));p.setEndsAt(PromotionRules.now().plusDays(1));p.setActive(true);p.setProductIds(List.of(product));return p;}
 @Test void createReadPreviewUpdateToggleDeleteUseCurrentTables() throws Exception {
  int id=dao.save(10,campaign(1));
  assertEquals(1,dao.findAll(10).size());assertEquals(1,dao.findShops(10).size());
  assertEquals(0,new BigDecimal("90000").compareTo(dao.previewPrices(10,id).get(0).getFinalPrice()));
  Promotion p=dao.findById(10,id);p.setName("Changed");dao.save(10,p);
  assertEquals(1,dao.findById(10,id).getVersion());
  dao.setActive(10,id,1,false);dao.delete(10,id,2);assertNull(dao.findById(10,id));
 }
 @Test void otherShopHiddenProductAndOverlappingCampaignCannotBeSaved() throws Exception {
  assertThrows(IllegalArgumentException.class,()->dao.save(10,campaign(2)));
  assertThrows(IllegalArgumentException.class,()->dao.save(10,campaign(3)));
  assertEquals(0,db.count("SELECT COUNT(*) FROM promotions"));
  assertFalse(dao.findProducts(10).stream().filter(p->p.getId()==3).findFirst().orElseThrow().isActive());
  int id=dao.save(10,campaign(1));
  assertThrows(IllegalArgumentException.class,()->dao.save(10,campaign(1)));
  assertNull(dao.findById(11,id));
  assertThrows(IllegalArgumentException.class,()->dao.previewPrices(11,id));
 }
 @Test void staleVersionAndPartialProductWriteRollBack() throws Exception {
  int id=dao.save(10,campaign(1));dao.setActive(10,id,0,false);
  assertThrows(IllegalArgumentException.class,()->dao.delete(10,id,0));
  db.exec("DELETE FROM promotion_products", "ALTER TABLE promotion_products ADD CONSTRAINT fail_product CHECK(product_id<>1)");
  assertThrows(java.sql.SQLException.class,()->dao.save(10,campaign(1)));
  assertEquals(1,db.count("SELECT COUNT(*) FROM promotions"));
 }
}
