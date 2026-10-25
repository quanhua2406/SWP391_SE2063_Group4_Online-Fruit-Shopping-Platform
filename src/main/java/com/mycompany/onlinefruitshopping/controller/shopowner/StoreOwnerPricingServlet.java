package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.pricing.PricingService;
import com.mycompany.onlinefruitshopping.utils.QueryUrl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Product &amp; variant pricing: set / edit the normal selling price of
 * each variant of the owner's shop.
 *
 * <p>Temporary offers are handled by
 * {@link StoreOwnerPromotionServlet} instead.</p>
 */
@WebServlet("/store-owner/pricing")
public class StoreOwnerPricingServlet extends HttpServlet {

    private final PricingService pricingService;
    public StoreOwnerPricingServlet() { this(new PricingService()); }
    StoreOwnerPricingServlet(PricingService service) { this.pricingService = service; }

    // =====================================================
    // GET - PRICE LIST
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
            if (!pricingService.hasShop(user.getUserID())) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/store-owner/create-shop"
                );
                return;
            }

            PricingService.PriceListView view = pricingService.loadPrices(
                    user.getUserID(),
                    request.getParameter("q"),
                    request.getParameter("weight"),
                    request.getParameter("page"));

            request.setAttribute("variantsPage", view.getVariantsPage());
            request.setAttribute("weightOptions", view.getWeightOptions());
            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to load your prices.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/pricing.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - UPDATE A PRICE
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

        String result;

        try {
            result = pricingService.savePrice(
                    user.getUserID(),
                    request.getParameter("variantId"),
                    request.getParameter("price"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Unable to save the price.", e);
        }

        // Keep the current search / filter / page while redirecting,
        // so saving one row does not send the owner back to page 1.
        String url = QueryUrl.keep(request,
                request.getContextPath() + "/store-owner/pricing",
                "q", "weight", "page") + "msg=" + result;

        response.sendRedirect(url);
    }
}
