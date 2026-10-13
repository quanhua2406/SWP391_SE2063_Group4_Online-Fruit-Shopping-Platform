package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private List<CartItem> getSampleFruitItems() {
        List<CartItem> sampleItems = new ArrayList<>();
        sampleItems.add(new CartItem(1, 1, "Táo Envy New Zealand", "Túi 1kg", "kg", 220000, 2, 
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500", 50));
        sampleItems.add(new CartItem(2, 3, "Nho Mẫu Đơn Shine Muscat", "Hộp 500g", "box", 450000, 1, 
                "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=500", 35));
        sampleItems.add(new CartItem(3, 2, "Xoài Cát Hòa Lộc", "Hộp 1kg", "kg", 95000, 3, 
                "https://images.unsplash.com/photo-1553279768-865429fa0078?w=500", 80));
        return sampleItems;
    }

    private void ensureUserSession(HttpServletRequest request) {
        if (request.getSession().getAttribute("user") == null) {
            User mockUser = new User();
            mockUser.setUserID(1);
            mockUser.setFullName("Nguyễn Văn An");
            mockUser.setEmail("customer@freshfruit.com");
            mockUser.setPhone("0988888888");
            mockUser.setRoleName("CUSTOMER");
            mockUser.setStatus("ACTIVE");
            request.getSession().setAttribute("user", mockUser);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ensureUserSession(request);

        // Lấy danh sách biến thể sản phẩm được chọn từ giỏ hàng
        String[] selectedVariants = request.getParameterValues("selectedVariants");
        List<CartItem> allItems = getSampleFruitItems();
        List<CartItem> checkoutItems = new ArrayList<>();

        if (selectedVariants != null && selectedVariants.length > 0) {
            List<String> selectedList = Arrays.asList(selectedVariants);
            for (CartItem item : allItems) {
                if (selectedList.contains(String.valueOf(item.getVariantId()))) {
                    checkoutItems.add(item);
                }
            }
        } else {
            // Mặc định chọn tất cả nếu không truyền
            checkoutItems.addAll(allItems);
        }

        double subtotal = 0;
        for (CartItem item : checkoutItems) {
            subtotal += item.getItemTotal();
        }

        double shippingFee = subtotal > 0 ? 25000 : 0;
        double grandTotal = subtotal + shippingFee;

        request.setAttribute("checkoutItems", checkoutItems);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("grandTotal", grandTotal);

        request.getRequestDispatcher("/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        ensureUserSession(request);

        String recipientName = request.getParameter("recipientName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");
        String totalAmountStr = request.getParameter("totalAmount");

        // Sinh mã đơn hàng ngẫu nhiên chuyên nghiệp
        long timestamp = System.currentTimeMillis() % 100000;
        String orderId = "FF-2026-" + timestamp;

        request.setAttribute("orderId", orderId);
        request.setAttribute("recipientName", recipientName);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);
        request.setAttribute("note", note);
        request.setAttribute("paymentMethod", paymentMethod != null ? paymentMethod : "Thanh toán khi nhận hàng (COD)");
        request.setAttribute("totalAmount", totalAmountStr);

        request.getRequestDispatcher("/order-success.jsp").forward(request, response);
    }
}
