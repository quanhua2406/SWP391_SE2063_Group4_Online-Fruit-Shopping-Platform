package com.mycompany.onlinefruitshopping.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryRulesTest {
    @Test
    void happyPathAndOrderStatuses() {
        String status = "Assigned";
        status = DeliveryRules.nextStatus(status, "accept");
        assertEquals("Confirmed", DeliveryRules.orderStatus(status));
        status = DeliveryRules.nextStatus(status, "pickup");
        assertEquals("Shipping", DeliveryRules.orderStatus(status));
        status = DeliveryRules.nextStatus(status, "start");
        status = DeliveryRules.nextStatus(status, "delivered");
        assertEquals("Delivered", status);
        assertEquals("Completed", DeliveryRules.orderStatus(status));
    }

    @Test
    void cannotSkipAcceptOrPickupOrStart() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Assigned", "pickup"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Accepted", "start"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("PickedUp", "delivered"));
    }

    @Test
    void cannotSubmitSameActionTwiceOrReopenFinishedAttempt() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Accepted", "accept"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Delivered", "failed"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Failed", "start"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Rejected", "accept"));
    }

    @Test
    void rejectionAndFailureNeedReason() {
        assertEquals("Rejected", DeliveryRules.nextStatus("Assigned", "reject"));
        assertEquals("Failed", DeliveryRules.nextStatus("Delivering", "failed"));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validateReason("Rejected", "  "));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validateReason("Failed", null));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validateReason("Failed", "x".repeat(256)));
        assertEquals("Không liên lạc được", DeliveryRules.validateReason("Failed", " Không liên lạc được "));
    }

    @Test
    void onlinePaymentCannotBeMarkedPaidByShipper() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validatePayment("VNPay", "Unpaid", true));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validatePayment("Momo", "Unpaid", false));
        assertDoesNotThrow(() -> DeliveryRules.validatePayment("VNPay", "Paid", false));
    }

    @Test
    void unpaidCodRequiresConfirmation() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.validatePayment("COD", "Unpaid", false));
        assertDoesNotThrow(() -> DeliveryRules.validatePayment("COD", "Unpaid", true));
        assertDoesNotThrow(() -> DeliveryRules.validatePayment("COD", "Paid", false));
    }

    @Test
    void missingOrUnknownActionDoesNotChangeState() {
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Assigned", null));
        assertThrows(IllegalArgumentException.class, () -> DeliveryRules.nextStatus("Assigned", "delete"));
    }

    @Test
    void invalidIdentifiersAreRejected() {
        assertEquals(17, WebUtil.positiveId("17"));
        for (String value : new String[] {null, "", "abc", "0", "-1", "1 OR 1=1", "9999999999999"}) {
            assertThrows(IllegalArgumentException.class, () -> WebUtil.positiveId(value));
        }
    }
}
