package com.mycompany.onlinefruitshopping.service.coupon;

import com.mycompany.onlinefruitshopping.dao.coupon.CouponApplicationDAO;
import com.mycompany.onlinefruitshopping.model.CouponQuote;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CouponApplicationServiceTest {
    @Test void applyRejectsMalformedCodeBeforeTransactionAndPreservesCustomerScope() throws SQLException {
        RecordingApplicationDAO dao = new RecordingApplicationDAO();
        CouponApplicationService service = new CouponApplicationService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.apply(10, 3, "<bad>"));
        assertThrows(IllegalArgumentException.class, () -> service.apply(0, 3, "SAVE10"));
        assertEquals(0, dao.calls);
        service.apply(10, 3, " save10 ");
        assertEquals("SAVE10", dao.code);
        assertEquals(10, dao.customerId);
        assertEquals(3, dao.orderId);
    }

    @Test void sqlFailurePropagatesWithoutRetryingCouponApplication() {
        RecordingApplicationDAO dao = new RecordingApplicationDAO();
        dao.fail = true;
        CouponApplicationService service = new CouponApplicationService(dao);
        assertThrows(SQLException.class, () -> service.apply(10, 3, "SAVE10"));
        assertEquals(1, dao.calls);
    }

    private static class RecordingApplicationDAO extends CouponApplicationDAO {
        int calls, customerId, orderId;
        String code;
        boolean fail;
        @Override public CouponQuote apply(int customerId, int orderId, String code) throws SQLException {
            calls++;
            if (fail) throw new SQLException("Database error during transaction");
            this.customerId = customerId;
            this.orderId = orderId;
            this.code = code;
            return new CouponQuote();
        }
    }
}
