package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.inventory.InventoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Shop owner inventory, split into small pages:
 *
 * <ul>
 *   <li>{@code /store-owner/inventory} - view stock quantity</li>
 *   <li>{@code /store-owner/inventory/update} - update a batch's
 *       stock quantity</li>
 *   <li>{@code /store-owner/inventory/restock} - record a new
 *       restock batch</li>
 *   <li>{@code /store-owner/inventory/low-stock} - threshold and
 *       low stock monitor</li>
 *   <li>{@code /store-owner/inventory/history} - inventory change
 *       log</li>
 * </ul>
 *
 * <p>The controller only reads the request and renders the response:
 * every rule and every query goes through
 * {@link InventoryService}.</p>
 */
@WebServlet(urlPatterns = {
    "/store-owner/inventory",
    "/store-owner/inventory/*"
})
public class StoreOwnerInventoryServlet extends HttpServlet {

    private final InventoryService inventoryService = new InventoryService();

    // =====================================================
    // ROUTING
    // =====================================================

    /**
     * Which small page is requested, or {@code null} for an unknown
     * sub-path.
     */
    private String viewOf(HttpServletRequest request) {

        String path = request.getPathInfo();

        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "stock";
        }

        if ("/update".equals(path)) {
            return "update";
        }

        if ("/restock".equals(path)) {
            return "restock";
        }

        if ("/low-stock".equals(path)) {
            return "low-stock";
        }

        if ("/history".equals(path)) {
            return "history";
        }

        return null;
    }

    private String jspOf(String view) {

        if ("update".equals(view)) {
            return "/jsp/shopowner/inventory-update.jsp";
        }

        if ("restock".equals(view)) {
            return "/jsp/shopowner/inventory-restock.jsp";
        }

        if ("low-stock".equals(view)) {
            return "/jsp/shopowner/inventory-low-stock.jsp";
        }

        if ("history".equals(view)) {
            return "/jsp/shopowner/inventory-history.jsp";
        }

        return "/jsp/shopowner/inventory.jsp";
    }

    /**
     * The page that owns each action, so a POST message lands where
     * the user was working.
     */
    private String backOf(String action) {

        if ("restock".equals(action)) {
            return "/restock";
        }

        if ("setThreshold".equals(action) || "notifyLowStock".equals(action)) {
            return "/low-stock";
        }

        return "/update";
    }

    // =====================================================
    // GET - ONE SMALL PAGE PER FUNCTION
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String view = viewOf(request);

        if (view == null) {
            response.sendRedirect(
                    request.getContextPath() + "/store-owner/inventory");
            return;
        }

        try {
            if (!inventoryService.hasShop(user.getUserID())) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            int userId = user.getUserID();

            request.setAttribute("shopName",
                    inventoryService.shopName(userId));

            if ("update".equals(view)) {
                loadUpdateView(request, userId);

            } else if ("restock".equals(view)) {
                loadRestockView(request, userId);

            } else if ("low-stock".equals(view)) {
                loadLowStockView(request, userId);

            } else if ("history".equals(view)) {
                loadHistoryView(request, userId);

            } else {
                loadStockView(request, userId);
            }

            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your inventory.", e);
        }

        request.getRequestDispatcher(jspOf(view))
               .forward(request, response);
    }

    private void loadStockView(HttpServletRequest request, int userId)
            throws SQLException {

        InventoryService.StockView view = inventoryService.loadStock(
                userId,
                request.getParameter("vq"),
                request.getParameter("vweight"),
                request.getParameter("vpage"));

        request.setAttribute("threshold", view.getThreshold());
        request.setAttribute("weightOptions", view.getWeightOptions());
        request.setAttribute("variantsPage", view.getVariantsPage());
    }

    private void loadUpdateView(HttpServletRequest request, int userId)
            throws SQLException {

        InventoryService.BatchesView view = inventoryService.loadBatches(
                userId,
                request.getParameter("bq"),
                request.getParameter("bstatus"),
                request.getParameter("bpage"));

        request.setAttribute("batchesPage", view.getBatchesPage());
    }

    private void loadRestockView(HttpServletRequest request, int userId)
            throws SQLException {

        request.setAttribute("variants",
                inventoryService.loadRestockVariants(userId));
    }

    private void loadLowStockView(HttpServletRequest request, int userId)
            throws SQLException {

        InventoryService.LowStockView view = inventoryService.loadLowStock(
                userId,
                request.getParameter("lq"),
                request.getParameter("lpage"));

        request.setAttribute("threshold", view.getThreshold());
        request.setAttribute("lowStockPage", view.getLowStockPage());
    }

    private void loadHistoryView(HttpServletRequest request, int userId)
            throws SQLException {

        InventoryService.AdjustmentsView view = inventoryService.loadHistory(
                userId,
                request.getParameter("hq"),
                request.getParameter("htype"),
                request.getParameter("hpage"));

        request.setAttribute("adjustmentsPage", view.getAdjustmentsPage());
    }

    // =====================================================
    // POST - UPDATE, RESTOCK, THRESHOLD, ALERT
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String action = trim(request.getParameter("action"));
        int userId = user.getUserID();
        String result;

        try {
            if ("restock".equals(action)) {
                result = inventoryService.restock(
                        userId,
                        request.getParameter("variantId"),
                        request.getParameter("quantity"),
                        request.getParameter("batchCode"),
                        request.getParameter("expiryDate"),
                        request.getParameter("reason"));

            } else if ("setThreshold".equals(action)) {
                result = inventoryService.setThreshold(
                        userId, request.getParameter("threshold"));

            } else if ("notifyLowStock".equals(action)) {
                result = inventoryService.notifyLowStock(user);

            } else {
                result = inventoryService.updateBatch(
                        userId,
                        request.getParameter("batchId"),
                        request.getParameter("quantity"),
                        request.getParameter("reason"));
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to update your inventory.", e);
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/store-owner/inventory"
                        + backOf(action)
                        + "?msg=" + result
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
