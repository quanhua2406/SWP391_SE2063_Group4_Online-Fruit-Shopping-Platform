package com.mycompany.onlinefruitshopping.controller.order;

import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Voucher;
import com.mycompany.onlinefruitshopping.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller Quá trình Thanh toán & Đặt hàng của Khách hàng (Customer Checkout Flow).
 * Feature 6: Tasks 1 - 6 (Select Address, Delivery Method, Payment Method, Review Order, Apply Voucher, Place Order).
 *
 * Tuân thủ mô hình 3 tầng:
 * - Servlet điều phối và gọi OrderService xử lý logic.
 */
@WebServlet(urlPatterns = {"/checkout", "/checkout/apply-voucher", "/checkout/place"})
public class CheckoutServlet extends HttpServlet {

    private final OrderService orderService;

    public CheckoutServlet() {
        this.orderService = new OrderService();
    }

    public CheckoutServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        try {
            List<CartItem> cartItems = orderService.getActiveCartItems(user.getUserID());
            if (cartItems == null || cartItems.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart?error=empty_cart");
                return;
            }

            List<Address> addresses = orderService.getUserAddresses(user.getUserID());
            Address defaultAddress = orderService.getDefaultAddress(user.getUserID());

            String deliveryMethod = request.getParameter("deliveryMethod");
            if (deliveryMethod == null || deliveryMethod.isBlank()) {
                deliveryMethod = "standard";
            }

            String voucherCode = request.getParameter("voucherCode");

            Map<String, Object> summary = orderService.calculateOrderSummary(cartItems, deliveryMethod, voucherCode);
            List<Voucher> activeVouchers = orderService.getActiveVouchers();

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("addresses", addresses);
            request.setAttribute("defaultAddress", defaultAddress);
            request.setAttribute("summary", summary);
            request.setAttribute("deliveryMethod", deliveryMethod);
            request.setAttribute("activeVouchers", activeVouchers);

            request.getRequestDispatcher("/checkout.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Database error in CheckoutServlet doGet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User user = getAuthenticatedUser(request, response);
        if (user == null) return;

        String action = request.getParameter("action");
        if (action == null) {
            String path = request.getServletPath();
            if (path.endsWith("apply-voucher")) action = "apply-voucher";
            else if (path.endsWith("place")) action = "place";
            else action = "place";
        }

        try {
            switch (action) {
                case "apply-voucher":
                    handleApplyVoucher(request, response, user.getUserID());
                    break;
                case "place":
                default:
                    handlePlaceOrder(request, response, user.getUserID());
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in CheckoutServlet doPost", e);
        }
    }

    private void handleApplyVoucher(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        String code = request.getParameter("voucherCode");
        String deliveryMethod = request.getParameter("deliveryMethod");

        List<CartItem> cartItems = orderService.getActiveCartItems(userId);
        Map<String, Object> summary = orderService.calculateOrderSummary(cartItems, deliveryMethod, code);

        Voucher v = (Voucher) summary.get("voucher");
        double discount = (double) summary.get("discount");
        double totalAmount = (double) summary.get("totalAmount");
        double shippingFee = (double) summary.get("shippingFee");
        String message = (String) summary.get("voucherMessage");

        PrintWriter out = response.getWriter();
        if (v != null) {
            out.print(String.format("{\"success\":true,\"discount\":%.0f,\"totalAmount\":%.0f,\"shippingFee\":%.0f,\"message\":\"%s\",\"code\":\"%s\"}",
                    discount, totalAmount, shippingFee, escapeJson(message), escapeJson(v.getCode())));
        } else {
            out.print(String.format("{\"success\":false,\"discount\":0,\"totalAmount\":%.0f,\"shippingFee\":%.0f,\"message\":\"%s\"}",
                    totalAmount, shippingFee, escapeJson(message != null ? message : "Mã không hợp lệ")));
        }
    }

    private void handlePlaceOrder(HttpServletRequest request, HttpServletResponse response, int userId)
            throws SQLException, ServletException, IOException {

        String addressIdStr = request.getParameter("addressId");
        int addressId = 0;
        if (addressIdStr != null && !addressIdStr.isBlank()) {
            try {
                addressId = Integer.parseInt(addressIdStr);
            } catch (NumberFormatException ignored) {}
        }

        // Nếu người dùng chọn "Nhập địa chỉ mới" trực tiếp trên trang Checkout
        if (addressId <= 0 || "new".equalsIgnoreCase(addressIdStr)) {
            String recName = request.getParameter("newRecipientName");
            String recPhone = request.getParameter("newRecipientPhone");
            String recLine = request.getParameter("newAddressLine");
            String recProv = request.getParameter("newProvince");
            String recDist = request.getParameter("newDistrict");
            String recWard = request.getParameter("newWard");

            if (recName == null || recName.isBlank() || recPhone == null || recPhone.isBlank() || recLine == null || recLine.isBlank()) {
                request.setAttribute("errorMessage", "Vui lòng chọn hoặc điền đầy đủ thông tin địa chỉ giao hàng.");
                doGet(request, response);
                return;
            }

            addressId = orderService.createQuickAddress(userId, recName.trim(), recPhone.trim(), recLine.trim(),
                    recProv != null ? recProv.trim() : "",
                    recDist != null ? recDist.trim() : "",
                    recWard != null ? recWard.trim() : "");
        }

        String deliveryMethod = request.getParameter("deliveryMethod");
        String paymentMethod = request.getParameter("paymentMethod");
        String voucherCode = request.getParameter("voucherCode");
        String note = request.getParameter("orderNote");

        try {
            int orderId = orderService.placeOrder(userId, addressId, deliveryMethod, paymentMethod, voucherCode, note);
            response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&success=placed");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            doGet(request, response);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") instanceof User user) {
            return user;
        }
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + request.getRequestURI());
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
