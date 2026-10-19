package com.mycompany.onlinefruitshopping.service.inventory;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.DBConnection;
import com.mycompany.onlinefruitshopping.utils.EmailService;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Low stock email alerts for shop owners.
 *
 * <p>The alert lists every variant of the shop that is at or below
 * the shop's safety threshold ({@code shops.low_stock_threshold}).
 * It is sent</p>
 *
 * <ul>
 *   <li>on request from the inventory screen ("Email Alert"
 *       button), and</li>
 *   <li>automatically (best effort, in the background) after an
 *       order deduction pushed stock below the threshold.</li>
 * </ul>
 */
public final class LowStockNotifier {

    /** Mail subject; kept fixed so it can be filtered server-side. */
    public static final String SUBJECT =
            "FreshFruit - Low Stock Alert";

    private LowStockNotifier() {
    }

    /**
     * Sends the low stock alert for one shop to the given address.
     *
     * @param ownerId        the shop owner (user id)
     * @param recipientEmail address the alert is sent to
     * @param shopName       shop shown in the mail body
     * @return number of low stock lines reported; {@code 0} means
     *         nothing is low, so no mail was sent
     * @throws SQLException      when the stock cannot be read
     * @throws MessagingException when the mail cannot be sent
     */
    public static int sendAlert(int ownerId,
                                String recipientEmail,
                                String shopName)
            throws SQLException, MessagingException {

        InventoryDAO inventoryDAO = new InventoryDAO();

        double threshold = inventoryDAO.getLowStockThreshold(ownerId);

        List<Map<String, Object>> rows =
                inventoryDAO.getLowStockForOwner(ownerId, threshold);

        if (rows.isEmpty()) {
            return 0;
        }

        EmailService.sendTextEmail(
                recipientEmail,
                SUBJECT,
                buildBody(rows, shopName, threshold));

        return rows.size();
    }

    /**
     * Fire-and-forget variant for background use (for example after
     * an order deduction): the recipient and shop name are read
     * from the database and every failure is logged and swallowed,
     * so the calling business transaction can never fail because
     * of an email problem.
     */
    public static void sendAlertAsync(int ownerId) {

        Thread thread = new Thread(() -> {

            try {
                String[] contact = findOwnerContact(ownerId);

                if (contact == null || contact[0] == null) {
                    return;
                }

                int reported = sendAlert(
                        ownerId, contact[0], contact[1]);

                if (reported > 0) {
                    System.out.println(
                            "LOW STOCK ALERT SENT (" + reported
                                    + " lines, owner " + ownerId + ")");
                }

            } catch (Exception e) {
                System.out.println(
                        "LOW STOCK ALERT FAILED (owner " + ownerId
                                + "): " + e.getMessage());
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private static String buildBody(List<Map<String, Object>> rows,
                                    String shopName,
                                    double threshold) {

        StringBuilder body = new StringBuilder();

        body.append("Hello,\n\n");

        body.append("The following products of ")
            .append(shopName == null || shopName.isBlank()
                    ? "your shop" : shopName)
            .append(" are at or below the safety threshold of ")
            .append(number(threshold))
            .append(" units:\n\n");

        for (Map<String, Object> row : rows) {
            body.append(" - ")
                .append(row.get("product_name"))
                .append(" [")
                .append(VariantWeights.label(
                        row.get("weight_value"), row.get("unit")))
                .append("] - SKU ")
                .append(row.get("sku"))
                .append(" - stock: ")
                .append(number(row.get("stock")))
                .append('\n');
        }

        body.append("\nPlease restock soon to keep selling.\n\n");
        body.append("FreshFruit Team");

        return body.toString();
    }

    /**
     * Reads the owner's email address and shop name in one go.
     *
     * @return {@code {email, shopName}} or {@code null}
     */
    private static String[] findOwnerContact(int ownerId)
            throws SQLException {

        String sql =
                "SELECT u.email, s.shop_name "
                + "FROM users u "
                + "LEFT JOIN shops s ON s.owner_id = u.user_id "
                + "WHERE u.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            rs.getString("email"),
                            rs.getString("shop_name")
                    };
                }
            }
        }

        return null;
    }

    private static String number(Object value) {

        if (value == null) {
            return "0";
        }

        BigDecimal number = value instanceof BigDecimal
                ? (BigDecimal) value
                : new BigDecimal(value.toString());

        return number.setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
