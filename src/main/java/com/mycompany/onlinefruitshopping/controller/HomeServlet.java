package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.model.SubscriptionPlan;
import com.mycompany.onlinefruitshopping.service.SubscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HomeServlet.class.getName());
    private final SubscriptionService subscriptionService = new SubscriptionService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Nạp các gói combo đang active để render động trên banner & grid trang chủ
            List<SubscriptionPlan> featuredPlans = subscriptionService.getActivePlansForCustomer(null, null, null, 1, 4);
            request.setAttribute("featuredPlans", featuredPlans);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Không thể tải danh sách gói combo cho trang chủ", e);
        }

        request.getRequestDispatcher("/home.jsp")
                .forward(request, response);
    }
}