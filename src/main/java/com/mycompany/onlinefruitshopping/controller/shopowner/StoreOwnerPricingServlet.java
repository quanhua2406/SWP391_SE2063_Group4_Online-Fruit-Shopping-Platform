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
 * <p>Each part has its own page:</p>
 *
 * <ul>
 *   <li>{@code /store-owner/pricing/product} - price per kg of each product</li>
 *   <li>{@code /store-owner/pricing/variant} - grade and price of each variant</li>
 *   <li>{@code /store-owner/pricing/history} - recent price changes</li>
 * </ul>
 *
 * <p>Temporary offers are handled by
 * {@link StoreOwnerPromotionServlet} instead.</p>
 */
@WebServlet(urlPatterns = {
    "/store-owner/pricing",
    "/store-owner/pricing/*"
})
public class StoreOwnerPricingServlet extends HttpServlet {

    private final PricingService pricingService = new PricingService();

    // =====================================================
    // ROUTING
    // =====================================================

    private String viewOf(HttpServletRequest request) {

        String path = request.getPathInfo();

        if (path == null || path.isEmpty() || "/".equals(path) || "/product".equals(path)) {
            return "product";
        }

        if ("/variant".equals(path)) {
            return "variant";
        }

        if ("/history".equals(path)) {
            return "history";
        }

        return null;
    }

    private String backOf(String action) {

        if ("kg".equals(action)) {
            return "/product";
        }

        return "/variant";
    }

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

        String activePage = viewOf(request);

        if (activePage == null) {
            response.sendRedirect(
                    request.getContextPath() + "/store-owner/pricing/product");
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
                    request.getParameter("page"),
                    request.getParameter("ppage"),
                    request.getParameter("hpage"));

            request.setAttribute("variantsPage", view.getVariantsPage());
            request.setAttribute("weightOptions", view.getWeightOptions());
            request.setAttribute("qualityGrades", view.getQualityGrades());
            request.setAttribute("productsPage", view.getProductsPage());
            request.setAttribute("historyPage", view.getHistoryPage());
            request.setAttribute("message", request.getParameter("msg"));
            request.setAttribute("activePage", activePage);

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
                request.getContextPath() + "/store-owner/pricing" + backOf(action),
                "q", "weight", "page", "ppage") + "msg=" + result;

        response.sendRedirect(url);
    }
}
