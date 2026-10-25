package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.promotion.VariantPromotionService;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Promotion management: set / edit / remove the promotional price of a
 * product variant for a limited period of time.
 *
 * <p>Every promotion is only a request: it is stored as
 * {@code pending} and becomes effective once an administrator
 * approves it in {@code /admin/promotion-approvals}.</p>
 */
@WebServlet("/store-owner/promotions")
public class StoreOwnerPromotionServlet extends HttpServlet {

    private final VariantPromotionService promotionService;

    public StoreOwnerPromotionServlet() { this(new VariantPromotionService()); }
    StoreOwnerPromotionServlet(VariantPromotionService service) { this.promotionService = service; }

    // =====================================================
    // GET - PROMOTION LIST
    // =====================================================

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        try {
            if (!promotionService.hasShop(user.getUserID())) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            VariantPromotionService.PromotionListView view =
                    promotionService.loadPromotions(
                            user.getUserID(),
                            request.getParameter("q"),
                            request.getParameter("weight"),
                            request.getParameter("approval"),
                            request.getParameter("page"));

            request.setAttribute("variantCount", view.getVariantCount());
            request.setAttribute("pendingCount", view.getPendingCount());
            request.setAttribute("approvedCount", view.getApprovedCount());
            request.setAttribute("rejectedCount", view.getRejectedCount());
            request.setAttribute("weightOptions", view.getWeightOptions());
            request.setAttribute("variantsPage", view.getVariantsPage());
            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your promotions.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/promotions.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - SET OR REMOVE A PROMOTION
    // =====================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User user = ShopOwnerSupport.currentUser(request, response);

        if (user == null) {
            return;
        }

        String action = request.getParameter("action");
        String result;

        try {
            if ("removePromo".equals(action)) {
                result = promotionService.removePromotion(
                        user.getUserID(),
                        request.getParameter("variantId"));
            } else {
                result = promotionService.savePromotion(
                        user.getUserID(),
                        request.getParameter("variantId"),
                        request.getParameter("promoPrice"),
                        request.getParameter("startDate"),
                        request.getParameter("endDate"));
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the promotion.", e);
        }

        // Keep the current search / filter / page while redirecting,
        // so saving one row does not send the owner back to page 1.
        String url = QueryUrl.keep(request,
                request.getContextPath() + "/store-owner/promotions",
                "q", "weight", "approval", "page") + "msg=" + result;

        response.sendRedirect(url);
    }
}
