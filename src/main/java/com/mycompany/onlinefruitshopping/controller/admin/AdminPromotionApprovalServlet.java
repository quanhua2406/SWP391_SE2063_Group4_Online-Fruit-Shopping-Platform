package com.mycompany.onlinefruitshopping.controller.admin;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.promotion.PromotionReviewService;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Admin review of the promotional prices requested by shop owners.
 *
 * <p>A promotion submitted by a shop owner stays {@code pending}
 * until it is approved here; approving makes the promotional price
 * effective inside its start / end date range, refusing keeps the
 * variant on its normal price and shows the reason back to the
 * shop owner.</p>
 */
@WebServlet("/admin/promotion-approvals")
public class AdminPromotionApprovalServlet extends HttpServlet {

    private final PromotionReviewService reviewService =
            new PromotionReviewService();

    // =====================================================
    // GET - THE REVIEW QUEUE
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            PromotionReviewService.ReviewQueue queue =
                    reviewService.loadQueue(
                            request.getParameter("status"),
                            request.getParameter("q"),
                            request.getParameter("page"));

            request.setAttribute("pendingCount", queue.getPendingCount());
            request.setAttribute("approvedCount", queue.getApprovedCount());
            request.setAttribute("rejectedCount", queue.getRejectedCount());
            request.setAttribute("totalCount", queue.getTotalCount());
            request.setAttribute("promotionsPage", queue.getPromotionsPage());
            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load the promotion requests.", e);
        }

        request.getRequestDispatcher(
                "/jsp/admin/admin-promotion-approvals.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - APPROVE / REFUSE
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User admin = currentAdmin(request, response);

        if (admin == null) {
            return;
        }

        String action = request.getParameter("action");
        String result;

        try {
            if ("reject".equals(action)) {
                result = reviewService.reject(
                        admin.getUserID(),
                        request.getParameter("promoId"),
                        request.getParameter("reviewNote"));
            } else {
                result = reviewService.approve(
                        admin.getUserID(),
                        request.getParameter("promoId"));
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the promotion decision.", e);
        }

        String url = QueryUrl.keep(request,
                request.getContextPath() + "/admin/promotion-approvals",
                "status", "q", "page") + "msg=" + result;

        response.sendRedirect(url);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    /**
     * Returns the logged-in administrator, or sends anyone else to
     * the login page and returns {@code null}.
     */
    private User currentAdmin(HttpServletRequest request,
                              HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute("user") instanceof User) {

            return (User) session.getAttribute("user");
        }

        response.sendRedirect(
                request.getContextPath() + "/login");
        return null;
    }
}
