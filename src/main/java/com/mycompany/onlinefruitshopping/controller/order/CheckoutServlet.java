package com.mycompany.onlinefruitshopping.controller.order;

import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.model.Coupon;
import com.mycompany.onlinefruitshopping.service.OrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
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
@WebServlet(urlPatterns = {"/checkout", "/checkout/apply-coupon", "/checkout/apply-voucher", "/checkout/place"})
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

            String couponCode = request.getParameter("couponCode");
            Map<String,Object> summary;
            try {
                summary=orderService.calculateOrderSummary(user.getUserID(),cartItems,deliveryMethod,couponCode);
            } catch(IllegalArgumentException invalid) {
                request.setAttribute("errorMessage",invalid.getMessage());
                try {
                    summary=orderService.calculateOrderSummary(user.getUserID(),cartItems,deliveryMethod,null);
                } catch(IllegalArgumentException mixedShops) {
                    // Do not show a misleading total for carts spanning multiple shops.
                    response.sendRedirect(request.getContextPath()+"/cart?error=multiple_shops");
                    return;
                }
            }
            List<Coupon> activeCoupons;
            try {
                activeCoupons=orderService.getCheckoutCoupons(user.getUserID(),cartItems);
            } catch(IllegalArgumentException invalid) {
                activeCoupons=List.of();
                request.setAttribute("errorMessage",invalid.getMessage());
            }

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("addresses", addresses);
            request.setAttribute("defaultAddress", defaultAddress);
            request.setAttribute("summary", summary);
            request.setAttribute("deliveryMethod", deliveryMethod);
            request.setAttribute("activeCoupons", activeCoupons);

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
            if (path.endsWith("apply-voucher") || path.endsWith("apply-coupon")) action = "apply-voucher";
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
        String code=request.getParameter("couponCode");
        String deliveryMethod=request.getParameter("deliveryMethod");
        List<CartItem> cartItems=orderService.getActiveCartItems(userId);
        Map<String,Object> summary;
        boolean success=code!=null && !code.isBlank();
        String message;
        try {
            summary=orderService.calculateOrderSummary(userId,cartItems,deliveryMethod,code);
            message=(String)summary.get("couponMessage");
        } catch(IllegalArgumentException invalid) {
            success=false;
            message=invalid.getMessage();
            summary=orderService.calculateOrderSummary(userId,cartItems,deliveryMethod,null);
        }
        double discount=(double)summary.get("discount");
        double totalAmount=(double)summary.get("totalAmount");
        double shippingFee=(double)summary.get("shippingFee");
        String json=String.format(java.util.Locale.ROOT,
            "{\"success\":%s,\"discount\":%.0f,\"totalAmount\":%.0f,\"shippingFee\":%.0f,\"message\":\"%s\"}",
            success,discount,totalAmount,shippingFee,escapeJson(message));
        response.getWriter().print(json);
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
        String voucherCode = request.getParameter("couponCode");
        String note = request.getParameter("orderNote");

        try {
            int orderId = orderService.placeOrder(userId, addressId, deliveryMethod, paymentMethod, voucherCode, note);
            if ("vnpay".equalsIgnoreCase(paymentMethod) || "online".equalsIgnoreCase(paymentMethod) || "banking".equalsIgnoreCase(paymentMethod)) {
                response.sendRedirect(request.getContextPath() + "/payment/process?orderId=" + orderId);
            } else {
                response.sendRedirect(request.getContextPath() + "/orders/detail?id=" + orderId + "&success=placed");
            }
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
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
