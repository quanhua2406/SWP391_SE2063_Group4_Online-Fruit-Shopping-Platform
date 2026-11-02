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
 * Product &amp; variant pricing: the price per kg of each product, the
 * quality grade of each variant and the selling price of variants that
 * are priced by hand.
 *
 * <p>Temporary offers are handled by
 * {@link StoreOwnerPromotionServlet} instead.</p>
 */
@WebServlet("/store-owner/pricing")
public class StoreOwnerPricingServlet extends HttpServlet {

    private final PricingService pricingService = new PricingService();

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
            request.setAttribute("qualityGrades", view.getQualityGrades());
            request.setAttribute("products", view.getProducts());
            request.setAttribute("history", view.getHistory());
            request.setAttribute("message", request.getParameter("msg"));

        } catch (SQLException e) {
            throw new ServletException(
                    "Không tải được danh sách giá của bạn.", e);
        }

        request.getRequestDispatcher(
                "/jsp/shopowner/pricing.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // POST - UPDATE PRICES
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
            if ("grade".equals(action)) {
                result = pricingService.saveGrade(
                        user.getUserID(),
                        request.getParameter("variantId"),
                        request.getParameter("grade"));

            } else if ("kg".equals(action)) {
                result = pricingService.savePricePerKg(
                        user.getUserID(),
                        request.getParameter("productId"),
                        request.getParameter("pricePerKg"));

            } else {
                result = pricingService.savePrice(
                        user.getUserID(),
                        request.getParameter("variantId"),
                        request.getParameter("price"));
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Không lưu được giá.", e);
        }

        // Keep the current search / filter / page while redirecting,
        // so saving one row does not send the owner back to page 1.
        String url = QueryUrl.keep(request,
                request.getContextPath() + "/store-owner/pricing",
                "q", "weight", "page") + "msg=" + result;

        response.sendRedirect(url);
    }
}
