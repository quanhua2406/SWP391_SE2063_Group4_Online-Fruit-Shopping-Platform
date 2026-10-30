package com.mycompany.onlinefruitshopping.controller.delivery;

import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/delivery/dashboard")
public class DeliveryStaffServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("currentRole", "DELIVERYSTAFF");

        WebUtil.createCsrfToken(request.getSession());

        request.getRequestDispatcher(
                "/jsp/delivery/delivery-dashboard.jsp"
        ).forward(request, response);
    }
}
