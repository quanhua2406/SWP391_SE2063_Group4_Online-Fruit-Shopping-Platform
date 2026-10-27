package com.mycompany.onlinefruitshopping.controller.cart;

import com.mycompany.onlinefruitshopping.dao.CartDAO.CartResult;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.CartService;
import com.mycompany.onlinefruitshopping.service.CartService.CartSummary;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller for Shopping Cart Management.
 * Feature 5: Shopping Cart Management - In Charge: Le Duc Cuong (cuongld)
 *
 * Tuân thủ mô hình 3 tầng (3-Layer Architecture):
 * Servlet chỉ đóng vai trò Controller: tiếp nhận HTTP Request, đọc tham số và gọi Service.
 * Mọi nghiệp vụ tính toán, kiểm tra tồn kho, điều chỉnh số lượng và áp dụng chính sách ship
 * đều được đóng gói trong CartService để dễ dàng tái sử dụng.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove", "/cart/clear"})
public class CartServlet extends HttpServlet {

    private final CartService cartService;

    public CartServlet() {
        this.cartService = new CartService();
    }

    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");
        List<CartItem> sessionCart = getSessionCart(session);

        try {
            // Service xử lý lấy toàn bộ dữ liệu, kiểm tra tồn kho, cảnh báo và tính toán chi phí
            CartSummary summary = cartService.getCartSummary(currentUser, sessionCart);

            // Cập nhật huy hiệu số lượng trên navbar
            session.setAttribute("cartCount", summary.getTotalCount());

            // Truyền dữ liệu sang View JSP
            request.setAttribute("cartItems", summary.getItems());
            request.setAttribute("subtotal", summary.getSubtotal());
            request.setAttribute("shippingFee", summary.getShippingFee());
            request.setAttribute("grandTotal", summary.getGrandTotal());
            request.setAttribute("freeShippingThreshold", summary.getFreeShippingThreshold());
            request.setAttribute("freeShippingRemaining", summary.getFreeShippingRemaining());
            request.setAttribute("cartWarnings", summary.getWarnings());

            request.getRequestDispatcher("/cart.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Không thể tải dữ liệu giỏ hàng: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            String servletPath = request.getServletPath();
            if (servletPath.contains("/add")) action = "add";
            else if (servletPath.contains("/update")) action = "update";
            else if (servletPath.contains("/remove")) action = "remove";
            else if (servletPath.contains("/clear")) action = "clear";
            else action = "view";
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        switch (action) {
            case "add":
                handleAdd(request, response, session, currentUser, isAjax);
                break;
            case "update":
                handleUpdate(request, response, session, currentUser, isAjax);
                break;
            case "remove":
                handleRemove(request, response, session, currentUser, isAjax);
                break;
            case "clear":
                handleClear(request, response, session, currentUser, isAjax);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/cart");
                break;
        }
    }

    /**
     * Xử lý thêm sản phẩm vào giỏ hàng thông qua Service.
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response,
                           HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);
        int quantity = parseInt(request.getParameter("quantity"), 1);
        boolean buyNow = "true".equalsIgnoreCase(request.getParameter("buyNow"));

        List<CartItem> sessionCart = getSessionCart(session);

        try {
            // Service xử lý kiểm tra sản phẩm, tồn kho và thêm vào giỏ
            CartResult result = cartService.addToCart(currentUser, sessionCart, variantId, quantity);
            session.setAttribute("cartCount", result.getCartItemCount());

            if (buyNow && result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            sendResponse(response, isAjax, result.isSuccess(), result.getMessage(), result.getCartItemCount(),
                    request.getContextPath() + "/cart");

        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Xử lý cập nhật số lượng sản phẩm thông qua Service.
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);
        int newQuantity = parseInt(request.getParameter("quantity"), 1);
        List<CartItem> sessionCart = getSessionCart(session);

        try {
            // Service xử lý cập nhật số lượng, kiểm tra giới hạn tồn kho
            CartResult result = cartService.updateQuantity(currentUser, sessionCart, variantId, newQuantity);
            session.setAttribute("cartCount", result.getCartItemCount());

            if (isAjax) {
                CartSummary summary = cartService.getCartSummary(currentUser, sessionCart);
                sendAjaxSummaryResponse(response, result, summary);
            } else {
                session.setAttribute("cartToast", result.getMessage());
                response.sendRedirect(request.getContextPath() + "/cart");
            }

        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi cập nhật giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Xử lý xóa một mặt hàng khỏi giỏ thông qua Service.
     */
    private void handleRemove(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);
        List<CartItem> sessionCart = getSessionCart(session);

        try {
            int newCount = cartService.removeItem(currentUser, sessionCart, variantId);
            session.setAttribute("cartCount", newCount);
            sendResponse(response, isAjax, true, "Đã xóa sản phẩm khỏi giỏ hàng.", newCount, request.getContextPath() + "/cart");
        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi xóa sản phẩm: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Xử lý làm trống toàn bộ giỏ hàng thông qua Service.
     */
    private void handleClear(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        List<CartItem> sessionCart = getSessionCart(session);

        try {
            cartService.clearCart(currentUser, sessionCart);
            session.removeAttribute("sessionCart");
            session.setAttribute("cartCount", 0);
            sendResponse(response, isAjax, true, "Đã dọn sạch toàn bộ giỏ hàng.", 0, request.getContextPath() + "/cart");
        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi làm trống giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getSessionCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessionCart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("sessionCart", cart);
        }
        return cart;
    }

    private void sendResponse(HttpServletResponse response, boolean isAjax, boolean success,
                              String message, int cartCount, String redirectUrl) throws IOException {
        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(String.format("{\"success\":%b,\"message\":\"%s\",\"cartCount\":%d}",
                    success, escapeJson(message), cartCount));
            out.flush();
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    private void sendAjaxSummaryResponse(HttpServletResponse response, CartResult result, CartSummary summary)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(String.format(Locale.US,
                "{\"success\":%b,\"message\":\"%s\",\"adjustedQuantity\":%d,\"cartCount\":%d,\"subtotal\":%.0f,\"shippingFee\":%.0f,\"grandTotal\":%.0f}",
                result.isSuccess(), escapeJson(result.getMessage()), result.getAdjustedQuantity(),
                summary.getTotalCount(), summary.getSubtotal(), summary.getShippingFee(), summary.getGrandTotal()));
        out.flush();
    }

    private int parseInt(String str, int defaultValue) {
        if (str == null || str.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
