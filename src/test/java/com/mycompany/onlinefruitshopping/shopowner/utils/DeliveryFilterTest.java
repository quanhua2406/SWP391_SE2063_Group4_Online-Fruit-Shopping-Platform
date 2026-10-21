package com.mycompany.onlinefruitshopping.shopowner.utils;

import java.util.List;
import com.mycompany.onlinefruitshopping.shopowner.model.Delivery;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryFilterTest {
    private Delivery delivery(int id, int shopId, int shipperId, String status, String receiver) {
        Delivery d = new Delivery();
        d.setId(id);
        d.setOrderId(1000 + id);
        d.setShopId(shopId);
        d.setShipperId(shipperId);
        d.setShipperName("Nguyễn Văn Nam");
        d.setStatus(status);
        d.setReceiverName(receiver);
        d.setReceiverPhone("0900 123 456");
        d.setShippingAddress("12 Đường Hoa, Bình Thạnh");
        return d;
    }

    private List<Delivery> rows() {
        return List.of(delivery(24, 1, 7, "Delivering", "Đặng Ánh"),
                delivery(23, 1, 8, "Delivering", "Trần Bình"),
                delivery(22, 2, 7, "Delivered", "Lê Chi"));
    }

    @Test void emptyFilterKeepsAllRowsAndTheirOrder() {
        List<Delivery> data = rows();
        DeliveryList filter = new DeliveryList(null, " ", null, "");
        assertEquals(data, filter.apply(data));
        assertFalse(filter.isActive());
        assertEquals(3, data.size());
    }

    @Test void combinesCriteriaAndDistinguishesShippersWithTheSameName() {
        List<Delivery> data = rows();
        assertEquals(List.of(data.get(0)), new DeliveryList("", "Delivering", "1", "7").apply(data));
        assertEquals(List.of(data.get(1)), new DeliveryList("", "Delivering", "1", "8").apply(data));
        assertTrue(new DeliveryList("", "Delivered", "1", "7").apply(data).isEmpty());
    }

    @Test void acceptsVietnameseWithOrWithoutAccentsAndExtraSpaces() {
        List<Delivery> data = rows();
        assertEquals(List.of(data.get(0)), new DeliveryList("  DANG   ANH  ", "", "", "").apply(data));
        assertEquals(List.of(data.get(0)), new DeliveryList("đặng ánh", "", "", "").apply(data));
        assertEquals(data, new DeliveryList("binh thanh", "", "", "").apply(data));
    }

    @Test void searchesDeliveryIdOrderIdAndFormattedPhone() {
        List<Delivery> data = rows();
        assertEquals(List.of(data.get(0)), new DeliveryList("#24", "", "", "").apply(data));
        assertEquals(List.of(data.get(0)), new DeliveryList("1024", "", "", "").apply(data));
        assertEquals(data, new DeliveryList("0900123456", "", "", "").apply(data));
        assertEquals(data, new DeliveryList("0900-123-456", "", "", "").apply(data));
    }

    @Test void cannotReturnRowsOutsideTheProvidedAuthorizedList() {
        Delivery allowed = rows().get(0);
        assertTrue(new DeliveryList("", "", "2", "").apply(List.of(allowed)).isEmpty());
        assertTrue(new DeliveryList("", "", "", "8").apply(List.of(allowed)).isEmpty());
        assertTrue(new DeliveryList("", "", "", "").apply(List.of()).isEmpty());
    }

    @Test void handlesMissingFieldsAndTreatsSearchAsLiteralText() {
        Delivery missing = new Delivery();
        assertTrue(new DeliveryList("không tồn tại", "", "", "").apply(List.of(missing)).isEmpty());
        assertTrue(new DeliveryList("' OR 1=1 --", "", "", "").apply(rows()).isEmpty());
    }

    @Test void rejectsMalformedFilterParameters() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryList("", "Unknown", "", ""));
        for (String id : List.of("0", "-1", "abc", "2147483648")) {
            assertThrows(IllegalArgumentException.class, () -> new DeliveryList("", "", id, ""));
        }
        assertThrows(IllegalArgumentException.class, () -> new DeliveryList("x".repeat(101), "", "", ""));
    }
}
