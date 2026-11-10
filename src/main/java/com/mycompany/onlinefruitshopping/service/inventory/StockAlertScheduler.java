package com.mycompany.onlinefruitshopping.service.inventory;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.utils.EmailService;
import com.mycompany.onlinefruitshopping.utils.VariantWeights;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Daily stock check for every active shop, run at 08:00.
 *
 * <ul>
 *   <li>Low stock: the existing {@link LowStockNotifier} sends the mail
 *       only when some variant is at or below the shop's threshold.</li>
 *   <li>Quality / expiry: batches that have expired or expire within
 *       {@value #EXPIRY_WINDOW_DAYS} days are listed in a mail.</li>
 * </ul>
 */
@WebListener
public final class StockAlertScheduler implements ServletContextListener {

    static final String EXPIRY_SUBJECT =
            "FreshFruit - Cảnh báo lô sắp hết hạn";

    static final int EXPIRY_WINDOW_DAYS = 7;

    private static final int RUN_HOUR = 8;

    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent event) {

        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "stock-alert");
            thread.setDaemon(true);
            return thread;
        });

        executor.scheduleAtFixedRate(
                StockAlertScheduler::runOnce,
                minutesUntilNextRun(),
                24 * 60,
                TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    static long minutesUntilNextRun() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().atTime(RUN_HOUR, 0);

        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }

        return Duration.between(now, next).toMinutes() + 1;
    }

    static void runOnce() {

        InventoryDAO inventoryDAO = new InventoryDAO();

        List<Integer> owners;
        try {
            owners = inventoryDAO.getActiveShopOwnerIds();
        } catch (Exception e) {
            System.out.println("STOCK ALERT: cannot list shops: " + e.getMessage());
            return;
        }

        for (int ownerId : owners) {
            try {
                LowStockNotifier.sendAlertAsync(ownerId);
                sendExpiryAlert(inventoryDAO, ownerId);
            } catch (Exception e) {
                System.out.println("STOCK ALERT FAILED (owner " + ownerId
                        + "): " + e.getMessage());
            }
        }
    }

    private static void sendExpiryAlert(InventoryDAO inventoryDAO, int ownerId)
            throws Exception {

        List<Map<String, Object>> rows =
                inventoryDAO.getExpiringBatchesForOwner(ownerId, EXPIRY_WINDOW_DAYS);

        if (rows.isEmpty()) {
            return;
        }

        String[] contact = LowStockNotifier.findOwnerContact(ownerId);

        if (contact == null || contact[0] == null) {
            return;
        }

        EmailService.sendTextEmail(
                contact[0], EXPIRY_SUBJECT, buildExpiryBody(rows, contact[1]));

        System.out.println("EXPIRY ALERT SENT (" + rows.size()
                + " batches, owner " + ownerId + ")");
    }

    private static String buildExpiryBody(List<Map<String, Object>> rows,
                                          String shopName) {

        StringBuilder body = new StringBuilder();

        body.append("Xin chào,\n\n");
        body.append("Các lô sau của ")
            .append(shopName == null || shopName.isBlank()
                    ? "cửa hàng của bạn" : shopName)
            .append(" đã hết hạn hoặc sắp hết hạn trong ")
            .append(EXPIRY_WINDOW_DAYS)
            .append(" ngày tới:\n\n");

        LocalDate today = LocalDate.now();

        for (Map<String, Object> row : rows) {

            LocalDate expiry = row.get("expiry_date") == null
                    ? null
                    : ((java.sql.Date) row.get("expiry_date")).toLocalDate();

            String state = expiry == null ? ""
                    : expiry.isBefore(today)
                        ? "đã hết hạn"
                        : "còn " + Duration.between(
                                today.atStartOfDay(), expiry.atStartOfDay()).toDays() + " ngày";

            body.append(" - ")
                .append(row.get("product_name"))
                .append(" [")
                .append(VariantWeights.label(row.get("weight_value"), row.get("unit")))
                .append("] - SKU ")
                .append(row.get("sku"))
                .append(" - lô ")
                .append(row.get("batch_code"))
                .append(" - SL ")
                .append(row.get("quantity"))
                .append(" - hạn ")
                .append(expiry == null ? "" : expiry)
                .append(" (")
                .append(state)
                .append(")\n");
        }

        body.append("\nVui lòng vào mục \"Không đảm bảo chất lượng\" trong quản lý kho để xử lý.\n\n");
        body.append("Đội ngũ FreshFruit");

        return body.toString();
    }
}
