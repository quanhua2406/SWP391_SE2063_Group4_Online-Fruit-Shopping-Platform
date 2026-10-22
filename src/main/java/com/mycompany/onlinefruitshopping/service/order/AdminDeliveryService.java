package com.mycompany.onlinefruitshopping.service.order;

import com.mycompany.onlinefruitshopping.dao.OrderDAO;
import com.mycompany.onlinefruitshopping.dao.order.AdminOrderDAO;
import com.mycompany.onlinefruitshopping.dao.user.AdminUserDAO;

import java.sql.SQLException;

/**
 * Business logic behind the administrator's delivery administration
 * (feature 19.0): reassigning a delivery, handling a problem order
 * and locking / unlocking a delivery-staff account.
 *
 * <p>The controller only reads the request and renders the response.
 * Every rule (identifiers, note length, shipper must really be a
 * delivery-staff account, order must exist ...) and the choice of
 * the message code live here, on top of {@link AdminOrderDAO},
 * {@link OrderDAO} and {@link AdminUserDAO}.</p>
 */
public class AdminDeliveryService {

    /**
     * Delivery notes are stored in {@code deliveries.note}, which is
     * an NVARCHAR(255) column. Reject longer text here so the insert
     * can never overflow the column.
     */
    private static final int MAX_NOTE_LENGTH = 255;

    /**
     * History notes are folded into {@code order_status_history.status},
     * an NVARCHAR(50) column, together with the new status.
     */
    private static final int MAX_HISTORY_NOTE_LENGTH = 40;

    private static final String DEFAULT_ASSIGN_NOTE = "Assigned by admin";
    private static final String DEFAULT_REASSIGN_NOTE = "Reassigned by admin";
    private static final String DEFAULT_ISSUE_NOTE = "Handled by admin";

    private static final String STATUS_ASSIGNED = "assigned";

    private final AdminOrderDAO orderDAO = new AdminOrderDAO();
    private final OrderDAO orderShopDAO = new OrderDAO();
    private final AdminUserDAO userDAO = new AdminUserDAO();

    // =========================================================
    // DELIVERY ASSIGNMENT (reassign / unassign)
    // =========================================================

    /**
     * Applies one assignment action chosen on the reassignment
     * screen.
     *
     * @param action     {@code assign}, {@code reassign} or
     *                   {@code unassign}
     * @param orderId    order the assignment belongs to
     * @param deliveryId existing assignment (for reassign / unassign)
     * @param staffId    shipper to assign to
     * @param noteRaw    free-text note (optional)
     * @return a message code understood by the delivery screens
     */
    public String applyAssignment(String action,
                                  int orderId,
                                  int deliveryId,
                                  int staffId,
                                  String noteRaw) throws SQLException {

        String normalizedAction = trim(action);
        String note = trim(noteRaw);

        // A note longer than the column would make the write fail.
        if (note.length() > MAX_NOTE_LENGTH) {
            return "invalid-note";
        }

        if ("unassign".equals(normalizedAction)) {

            if (deliveryId <= 0) {
                return "invalid";
            }

            return orderDAO.unassignDelivery(deliveryId) ? "unassigned" : "failed";
        }

        // Both "assign" and "reassign" need a real, existing order and
        // a shipper who actually holds the DeliveryStaff role.
        if (orderId <= 0 || !isDeliveryStaff(staffId)) {
            return "invalid";
        }

        if ("assign".equals(normalizedAction)) {

            String finalNote = note.isEmpty() ? DEFAULT_ASSIGN_NOTE : note;

            return orderDAO.assignDelivery(orderId, staffId, finalNote)
                    ? "assigned" : "conflict";
        }

        if ("reassign".equals(normalizedAction)) {

            // A reassignment needs an existing assignment to move.
            if (deliveryId <= 0) {
                return "invalid";
            }

            String finalNote = note.isEmpty() ? DEFAULT_REASSIGN_NOTE : note;

            return orderDAO.reassignDelivery(deliveryId, staffId, finalNote)
                    ? "reassigned" : "failed";
        }

        return "invalid";
    }

    // =========================================================
    // ORDER ISSUES (reopen / cancel)
    // =========================================================

    /**
     * Reopens or cancels a problem order.
     *
     * @param resolution {@code reopen} or {@code cancel}
     * @return a message code understood by the issue screen
     */
    public String resolveIssue(int orderId,
                               String resolution,
                               String noteRaw,
                               int adminId) throws SQLException {

        String normalizedResolution = trim(resolution);
        String note = trim(noteRaw);

        String newStatus;

        if ("cancel".equals(normalizedResolution)) {
            newStatus = "cancelled";
        } else if ("reopen".equals(normalizedResolution)) {
            newStatus = "preparing";
        } else {
            return "invalid";
        }

        if (orderId <= 0) {
            return "invalid";
        }

        // The history note shares the 50-char status column, so keep
        // it short enough that the status prefix still fits.
        if (note.length() > MAX_HISTORY_NOTE_LENGTH) {
            note = note.substring(0, MAX_HISTORY_NOTE_LENGTH);
        }

        String finalNote = note.isEmpty() ? DEFAULT_ISSUE_NOTE : note;

        try {

            boolean updated = orderShopDAO.updateOrderStatus(
                    orderId, 0, newStatus, adminId, finalNote);

            if (!updated) {
                return "failed";
            }

            return "cancel".equals(normalizedResolution)
                    ? "cancelled" : "reopened";

        } catch (IllegalStateException | IllegalArgumentException e) {

            // The order is already finished (delivered / cancelled).
            return "terminal";
        }
    }

    // =========================================================
    // DELIVERY STAFF (block / unblock)
    // =========================================================

    /**
     * Locks or unlocks a delivery-staff account.
     *
     * @param action {@code block} or {@code unblock}
     * @return a message code understood by the staff screen
     */
    public String changeStaffStatus(int staffId, String action)
            throws SQLException {

        String normalizedAction = trim(action);

        String targetStatus;

        if ("block".equals(normalizedAction)) {
            targetStatus = "locked";
        } else if ("unblock".equals(normalizedAction)) {
            targetStatus = "active";
        } else {
            return "invalid";
        }

        // Only an account that really is a delivery-staff account can
        // be managed here; this blocks tampering with arbitrary users.
        if (staffId <= 0 || !isDeliveryStaff(staffId)) {
            return "invalid";
        }

        boolean updated = userDAO.updateStatus(staffId, targetStatus);

        if (!updated) {
            return "failed";
        }

        return "block".equals(normalizedAction) ? "blocked" : "unblocked";
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /**
     * True only when the id is positive and the account currently
     * holds the DeliveryStaff role.
     */
    private boolean isDeliveryStaff(int staffId) throws SQLException {

        return staffId > 0 && orderDAO.isDeliveryStaff(staffId);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
