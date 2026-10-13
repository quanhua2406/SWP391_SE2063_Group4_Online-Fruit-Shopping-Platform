package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.CartItem;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Dữ liệu hoa quả mẫu hiển thị trong giỏ hàng (Feature 5: View Cart)
        List<CartItem> sampleItems = new ArrayList<>();
        sampleItems.add(new CartItem(1, 1, "Táo Envy New Zealand", "Túi 1kg", "kg", 220000, 2, 
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500", 50));
        sampleItems.add(new CartItem(2, 3, "Nho Mẫu Đơn Shine Muscat", "Hộp 500g", "box", 450000, 1, 
                "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=500", 35));
        sampleItems.add(new CartItem(3, 2, "Xoài Cát Hòa Lộc", "Hộp 1kg", "kg", 95000, 3, 
                "https://images.unsplash.com/photo-1553279768-865429fa0078?w=500", 80));

        double subtotal = 0;
        for (CartItem item : sampleItems) {
            subtotal += item.getItemTotal();
        }

        double shippingFee = 25000;
        double grandTotal = subtotal + shippingFee;

        request.setAttribute("cartItems", sampleItems);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("shippingFee", shippingFee);
        request.setAttribute("grandTotal", grandTotal);

        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }
}
