package com.mycompany.onlinefruitshopping.dao.delivery;

import com.mycompany.onlinefruitshopping.support.ModuleDatabase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShipperShopAddressTest {
    @Test void ownerCanSaveAddressAndShipperCanReadDirectory() throws Exception {
        ModuleDatabase db=new ModuleDatabase();
        ShipperTeamDAO dao=new ShipperTeamDAO(db::open);
        assertNull(dao.findActiveShops().get(0).getPickupAddress());
        assertThrows(IllegalArgumentException.class,()->dao.updatePickupAddress(11,2,"no"));
        dao.updatePickupAddress(10,1,"123 Duong A, Ha Noi");
        assertEquals("123 Duong A, Ha Noi",dao.findOwnerShop(10).getPickupAddress());
        assertTrue(dao.findActiveShops().stream().anyMatch(s->s.getId()==1 && "123 Duong A, Ha Noi".equals(s.getPickupAddress())));
        assertThrows(IllegalArgumentException.class,()->dao.updatePickupAddress(999,1,"123 Duong A, Ha Noi"));
        assertThrows(IllegalArgumentException.class,()->dao.updatePickupAddress(11,1,"123 Duong A, Ha Noi"));
    }
}
