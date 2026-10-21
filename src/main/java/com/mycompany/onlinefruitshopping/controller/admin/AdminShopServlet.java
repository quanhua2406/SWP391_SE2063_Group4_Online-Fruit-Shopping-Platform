package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.dao.shop.AdminShopDAO.ShopInfo;
import com.mycompany.onlinefruitshopping.service.shop.AdminShopService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Shop-owner management (feature 18.0): view the registered shops and
 * block / unblock a shop. All rules live in {@link AdminShopService}.
 */
@WebServlet("/admin/shops")
public class AdminShopServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AdminShopService shopService = new AdminShopService();

    private static final String LIST_JSP = "/jsp/admin/admin-shops.jsp";
    private static final String DETAIL_JSP = "/jsp/admin/admin-shop-detail.jsp";

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));

        try {
            if ("detail".equalsIgnoreCase(action)) {
                showDetail(request, response);

            } else if ("search".equalsIgnoreCase(action)
                    || request.getParameter("keyword") != null
                    || request.getParameter("status") != null) {
                searchShops(request, response);

            } else {
                loadShops(request, response);
            }

        } catch (SQLException e) {
            log("Failed to retrieve shop data.", e);
            throw new ServletException(
                    "Unable to retrieve shop data.", e
            );
        }
    }

    // =====================================================
    // SHOP DETAIL
    // =====================================================

    private void showDetail(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        Integer shopId = parseShopId(request, response);

        if (shopId == null) {
            return;
        }

        ShopInfo shop = shopService.getShop(shopId);

        if (shop == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Shop not found."
            );
            return;
        }

        request.setAttribute("shop", shop);
        request.setAttribute("message", request.getParameter("message"));

        request.getRequestDispatcher(DETAIL_JSP)
                .forward(request, response);
    }

    // =====================================================
    // LOAD SHOPS
    // =====================================================

    private void loadShops(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        List<ShopInfo> shops = shopService.searchShops(null, null);

        request.setAttribute("shops", shops);
        request.setAttribute("keyword", "");
        request.setAttribute("selectedStatus", "ALL");

        request.getRequestDispatcher(LIST_JSP)
                .forward(request, response);
    }

    // =====================================================
    // SEARCH / FILTER
    // =====================================================

    private void searchShops(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException, SQLException {

        String keyword = trim(request.getParameter("keyword"));
        String status = trim(request.getParameter("status"));

        List<ShopInfo> shops = shopService.searchShops(keyword, status);

        if (shops == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid status."
            );
            return;
        }

        request.setAttribute("shops", shops);
        request.setAttribute("keyword", keyword);
        request.setAttribute(
                "selectedStatus",
                status.isEmpty() ? "ALL" : status.toUpperCase(java.util.Locale.ROOT)
        );

        request.getRequestDispatcher(LIST_JSP)
                .forward(request, response);
    }

    // =====================================================
    // POST - BLOCK / UNBLOCK
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));

        try {
            String message;

            if ("block".equalsIgnoreCase(action)) {
                message = shopService.blockShop(request.getParameter("shopId"));

            } else if ("unblock".equalsIgnoreCase(action)) {
                message = shopService.unblockShop(request.getParameter("shopId"));

            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid action."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/admin/shops?action=detail&shopId="
                    + trim(request.getParameter("shopId"))
                    + "&message=" + message
            );

        } catch (SQLException e) {
            log("Failed to change shop status.", e);
            throw new ServletException(
                    "Unable to change shop status.", e
            );
        }
    }

    // =====================================================
    // PARSE SHOP ID
    // =====================================================

    private Integer parseShopId(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String value = request.getParameter("shopId");

        if (value == null || value.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing shop ID."
            );
            return null;
        }

        try {
            int shopId = Integer.parseInt(value);

            if (shopId <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid shop ID."
                );
                return null;
            }

            return shopId;

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid shop ID."
            );
            return null;
        }
    }

    // =====================================================
    // STRING HELPER
    // =====================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
