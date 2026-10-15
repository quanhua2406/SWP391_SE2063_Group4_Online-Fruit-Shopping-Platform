package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ShopOwnerRequestDAO;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerRequestDAO.AdminRequestInfo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class AdminReviewRequestServlet extends HttpServlet {

    private final ShopOwnerRequestDAO requestDAO =
            new ShopOwnerRequestDAO();

    // Check whether the current user is an administrator.
    private boolean isAdmin(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }

        Object role = session.getAttribute("roleName");

        return role != null
                && "Admin".equalsIgnoreCase(role.toString());
    }

    // Display all shop owner requests.
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this page."
            );
            return;
        }

        try {
            List<AdminRequestInfo> requests =
                    requestDAO.getAllRequests();

            request.setAttribute("requests", requests);

            request.getRequestDispatcher(
                    "/admin-review-requests.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load shop owner requests.", e
            );
        }
    }

    // Process the administrator's approval or rejection.
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to perform this action."
            );
            return;
        }

        String requestIdValue = request.getParameter("requestId");
        String action = request.getParameter("action");
        String adminNote = request.getParameter("adminNote");

        int requestId;

        try {
            requestId = Integer.parseInt(requestIdValue);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request ID."
            );
            return;
        }

        if (requestId <= 0 || action == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request information."
            );
            return;
        }

        String newStatus;

        if ("APPROVE".equalsIgnoreCase(action)) {
            newStatus = "APPROVED";
        } else if ("REJECT".equalsIgnoreCase(action)) {
            newStatus = "REJECTED";
        } else {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid review action."
            );
            return;
        }

        try {
            boolean updated = requestDAO.reviewRequest(
                    requestId,
                    newStatus,
                    adminNote
            );

            HttpSession session = request.getSession();

            if (updated) {
                session.setAttribute(
                        "adminReviewMessage",
                        "Request #" + requestId
                                + " has been " + newStatus + "."
                );
            } else {
                session.setAttribute(
                        "adminReviewMessage",
                        "The request was not updated. "
                                + "It may no longer be pending."
                );
            }

            response.sendRedirect(
                    request.getContextPath()
                            + "/admin/review-requests"
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to review the shop owner request.", e
            );
        }
    }
}