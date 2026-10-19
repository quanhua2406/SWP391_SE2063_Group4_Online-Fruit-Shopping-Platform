package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.inventory.InventoryDAO;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.utils.Filters;
import com.mycompany.onlinefruitshopping.utils.Page;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin review of the promotional prices requested by shop owners.
 *
 * <p>A promotion submitted by a shop owner stays {@code pending}
 * until it is approved here; approving makes the promotional price
 * effective inside its start / end date range, refusing keeps the
 * variant on its normal price and shows the reason back to the
 * shop owner.</p>
 */
@WebServlet("/admin/promotion-approvals")
public class AdminPromotionApprovalServlet extends HttpServlet {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    // =====================================================
    // GET - THE REVIEW QUEUE
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String status = Filters.text(request.getParameter("status"));
        String q = Filters.text(request.getParameter("q"));

        try {
            List<Map<String, Object>> all =
                    inventoryDAO.getPromotionsForAdmin();

            // Counters describe every request, not only the rows the
            // current filter keeps.
            int pending = 0;
            int approved = 0;
            int rejected = 0;

            for (Map<String, Object> row : all) {
                String value = row.get("status") == null
                        ? ""
                        : row.get("status").toString();

                if ("approved".equals(value)) {
                    approved++;
                } else if ("rejected".equals(value)) {
                    rejected++;
                } else {
                    pending++;
                }
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (Map<String, Object> row : all) {
                if (Filters.same(status, row.get("status"))
                        && Filters.any(q,
                                row.get("product_name"),
                                row.get("sku"),
                                row.get("shop_name"))) {
                    rows.add(row);
                }
            }

            request.setAttribute("pendingCount", pending);
            request.setAttribute("approvedCount", approved);
            request.setAttribute("rejectedCount", rejected);
            request.setAttribute("totalCount", all.size());

            request.setAttribute("promotionsPage", Page.of(
                    rows, Page.parsePage(request.getParameter("page"))));

            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the promotion requests.", e);
        }

        request.getRequestDispatcher(
                "/jsp/admin/admin-promotion-approvals.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - APPROVE / REFUSE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User admin = currentAdmin(request, response);

        if (admin == null) {
            return;
        }

        String action = request.getParameter("action");
        String result;

        try {
            if ("reject".equals(action)) {
                result = rejectPromotion(request, admin.getUserID());
            } else {
                result = approvePromotion(request, admin.getUserID());
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the promotion decision.", e);
        }

        String url = QueryUrl.keep(request,
                request.getContextPath() + "/admin/promotion-approvals",
                "status", "q", "page") + "msg=" + result;

        response.sendRedirect(url);
    }

    private String approvePromotion(HttpServletRequest request, int adminId)
            throws SQLException {

        int promoId = parseInt(request.getParameter("promoId"), -1);

        if (promoId <= 0) {
            return "invalid";
        }

        Map<String, Object> promo =
                inventoryDAO.getPromotionForReview(promoId);

        if (promo == null) {
            return "invalid";
        }

        if (!"pending".equals(String.valueOf(promo.get("status")))) {
            return "not-pending";
        }

        // A promotion whose time window is already over cannot be
        // made effective, so it is refused instead of approved.
        Date endDate = (Date) promo.get("end_date");

        if (endDate != null
                && endDate.toLocalDate().isBefore(LocalDate.now())) {
            return "approve-expired";
        }

        // The offer must still be a discount: the shop owner may
        // have lowered the normal price while the request waited.
        BigDecimal promoPrice = (BigDecimal) promo.get("promo_price");
        BigDecimal price = (BigDecimal) promo.get("price");

        if (promoPrice == null || price == null
                || promoPrice.compareTo(price) >= 0) {
            return "approve-not-discount";
        }

        return inventoryDAO.reviewPromotion(
                promoId, "approved", null, adminId)
                ? "approved" : "not-pending";
    }

    private String rejectPromotion(HttpServletRequest request, int adminId)
            throws SQLException {

        int promoId = parseInt(request.getParameter("promoId"), -1);

        if (promoId <= 0) {
            return "invalid";
        }

        String note = request.getParameter("reviewNote");

        if (note != null && note.length() > 255) {
            note = note.substring(0, 255);
        }

        return inventoryDAO.reviewPromotion(
                promoId, "rejected", note, adminId)
                ? "rejected" : "not-pending";
    }

    // =====================================================
    // HELPERS
    // =====================================================

    /**
     * Returns the logged-in administrator, or sends anyone else to
     * the login page and returns {@code null}.
     */
    private User currentAdmin(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("user") instanceof User) {

            return (User) session.getAttribute("user");
        }

        response.sendRedirect(
                request.getContextPath() + "/login");
        return null;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
