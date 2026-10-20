package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.CartDAO;
import com.mycompany.onlinefruitshopping.dao.CartDAO.CartResult;
import com.mycompany.onlinefruitshopping.model.CartItem;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Controller for Shopping Cart Management.
 * Feature 5: Shopping Cart Management - In Charge: Le Duc Cuong (cuongld)
 * Handles View Cart, Add to Cart, Update Quantity, Remove Item, Clear Cart,
 * with full validation for product status, packaging variants, dynamic pricing, and live inventory.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove", "/cart/clear"})
public class CartServlet extends HttpServlet {

    private final CartDAO cartDAO = new CartDAO();
    private static final double FREE_SHIPPING_THRESHOLD = 500000.0;
    private static final double STANDARD_SHIPPING_FEE = 25000.0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        List<CartItem> cartItems;
        List<String> warnings = new ArrayList<>();

        try {
            if (currentUser != null) {
                // Logged in: Load from database with live pricing & stock
                cartItems = cartDAO.getCartItemsByUserId(currentUser.getUserID());
            } else {
                // Guest: Load from session and re-validate against database
                cartItems = getSessionCart(session);
                validateAndRefreshSessionCart(cartItems, warnings);
            }

            // Calculate totals and validate business rules
            double subtotal = 0;
            int totalCount = 0;
            boolean hasUnavailableItem = false;

            for (CartItem item : cartItems) {
                if (!item.isActiveProduct()) {
                    hasUnavailableItem = true;
                }
                if (item.getQuantity() > item.getStockQuantity()) {
                    warnings.add("Sản phẩm '" + item.getProductName() + " - " + item.getPackagingType() +
                            "' chỉ còn " + (int) item.getStockQuantity() + " sản phẩm trong kho.");
                }
                subtotal += item.getItemTotal();
                totalCount += item.getQuantity();
            }

            if (hasUnavailableItem) {
                warnings.add("Một số sản phẩm trong giỏ hàng hiện đã tạm ngừng kinh doanh.");
            }

            double shippingFee = (subtotal >= FREE_SHIPPING_THRESHOLD || cartItems.isEmpty()) ? 0 : STANDARD_SHIPPING_FEE;
            double grandTotal = subtotal + shippingFee;
            double freeShippingRemaining = Math.max(0, FREE_SHIPPING_THRESHOLD - subtotal);

            // Update session cart count for header badge
            session.setAttribute("cartCount", totalCount);

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("subtotal", subtotal);
            request.setAttribute("shippingFee", shippingFee);
            request.setAttribute("grandTotal", grandTotal);
            request.setAttribute("freeShippingThreshold", FREE_SHIPPING_THRESHOLD);
            request.setAttribute("freeShippingRemaining", freeShippingRemaining);
            request.setAttribute("cartWarnings", warnings);

            request.getRequestDispatcher("/cart.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Không thể tải dữ liệu giỏ hàng: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            String servletPath = request.getServletPath();
            if (servletPath.contains("/add")) action = "add";
            else if (servletPath.contains("/update")) action = "update";
            else if (servletPath.contains("/remove")) action = "remove";
            else if (servletPath.contains("/clear")) action = "clear";
            else action = "view";
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        switch (action) {
            case "add":
                handleAdd(request, response, session, currentUser, isAjax);
                break;
            case "update":
                handleUpdate(request, response, session, currentUser, isAjax);
                break;
            case "remove":
                handleRemove(request, response, session, currentUser, isAjax);
                break;
            case "clear":
                handleClear(request, response, session, currentUser, isAjax);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/cart");
                break;
        }
    }

    /**
     * Handles adding a product packaging variant to the cart.
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response,
                           HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);
        int quantity = parseInt(request.getParameter("quantity"), 1);
        boolean buyNow = "true".equalsIgnoreCase(request.getParameter("buyNow"));

        if (quantity <= 0) {
            quantity = 1;
        }

        if (variantId <= 0) {
            sendResponse(response, isAjax, false, "Quy cách đóng gói không hợp lệ.", 0, request.getContextPath() + "/products");
            return;
        }

        try {
            if (currentUser != null) {
                // Logged in: Process via CartDAO (Database)
                CartResult result = cartDAO.addToCart(currentUser.getUserID(), variantId, quantity);
                session.setAttribute("cartCount", result.getCartItemCount());

                if (buyNow) {
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }

                sendResponse(response, isAjax, result.isSuccess(), result.getMessage(), result.getCartItemCount(),
                        request.getContextPath() + "/cart");

            } else {
                // Guest: Process via Session Cart
                CartItem liveVariant = cartDAO.getVariantDetailForCart(variantId);
                if (liveVariant == null) {
                    sendResponse(response, isAjax, false, "Quy cách sản phẩm không tồn tại.", 0, request.getContextPath() + "/products");
                    return;
                }
                if (!liveVariant.isActiveProduct()) {
                    sendResponse(response, isAjax, false, "Sản phẩm tạm thời ngừng kinh doanh.", 0, request.getContextPath() + "/products");
                    return;
                }

                int availableStock = (int) liveVariant.getStockQuantity();
                if (availableStock <= 0) {
                    sendResponse(response, isAjax, false, "Sản phẩm hiện đã hết hàng trong kho.", 0, request.getContextPath() + "/products");
                    return;
                }

                List<CartItem> sessionCart = getSessionCart(session);
                CartItem existingItem = null;
                for (CartItem item : sessionCart) {
                    if (item.getVariantId() == variantId) {
                        existingItem = item;
                        break;
                    }
                }

                int targetQty = quantity;
                boolean wasCapped = false;

                if (existingItem != null) {
                    targetQty = existingItem.getQuantity() + quantity;
                    if (targetQty > availableStock) {
                        targetQty = availableStock;
                        wasCapped = true;
                    }
                    existingItem.setQuantity(targetQty);
                    existingItem.setUnitPrice(liveVariant.getUnitPrice());
                    existingItem.setStockQuantity(availableStock);
                } else {
                    if (targetQty > availableStock) {
                        targetQty = availableStock;
                        wasCapped = true;
                    }
                    liveVariant.setQuantity(targetQty);
                    sessionCart.add(liveVariant);
                }

                int totalCount = calculateSessionCartCount(sessionCart);
                session.setAttribute("cartCount", totalCount);

                String msg = wasCapped
                        ? "Đã thêm vào giỏ. Do tồn kho chỉ còn " + availableStock + " sản phẩm nên số lượng đã điều chỉnh về " + targetQty + "."
                        : "Đã thêm thành công " + quantity + " " + liveVariant.getPackagingType() + " (" + liveVariant.getProductName() + ") vào giỏ hàng!";

                if (buyNow) {
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }

                sendResponse(response, isAjax, true, msg, totalCount, request.getContextPath() + "/cart");
            }

        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Handles updating quantity of a variant item.
     */
    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);
        int newQuantity = parseInt(request.getParameter("quantity"), 1);

        try {
            if (currentUser != null) {
                CartResult result = cartDAO.updateQuantity(currentUser.getUserID(), variantId, newQuantity);
                session.setAttribute("cartCount", result.getCartItemCount());

                if (isAjax) {
                    sendAjaxCartSummary(response, currentUser.getUserID(), result);
                } else {
                    session.setAttribute("cartToast", result.getMessage());
                    response.sendRedirect(request.getContextPath() + "/cart");
                }
            } else {
                List<CartItem> sessionCart = getSessionCart(session);
                CartItem targetItem = null;
                for (CartItem item : sessionCart) {
                    if (item.getVariantId() == variantId) {
                        targetItem = item;
                        break;
                    }
                }

                if (targetItem == null) {
                    sendResponse(response, isAjax, false, "Không tìm thấy sản phẩm trong giỏ hàng.", calculateSessionCartCount(sessionCart), request.getContextPath() + "/cart");
                    return;
                }

                if (newQuantity <= 0) {
                    sessionCart.remove(targetItem);
                } else {
                    CartItem live = cartDAO.getVariantDetailForCart(variantId);
                    int stock = (live != null) ? (int) live.getStockQuantity() : 0;
                    if (newQuantity > stock) {
                        newQuantity = stock;
                    }
                    targetItem.setQuantity(newQuantity);
                    if (live != null) {
                        targetItem.setUnitPrice(live.getUnitPrice());
                        targetItem.setStockQuantity(stock);
                    }
                }

                int totalCount = calculateSessionCartCount(sessionCart);
                session.setAttribute("cartCount", totalCount);

                if (isAjax) {
                    sendAjaxSessionCartSummary(response, sessionCart);
                } else {
                    response.sendRedirect(request.getContextPath() + "/cart");
                }
            }
        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi cập nhật giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Handles removing an item from the cart.
     */
    private void handleRemove(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        int variantId = parseInt(request.getParameter("variantId"), 0);

        try {
            if (currentUser != null) {
                cartDAO.removeItem(currentUser.getUserID(), variantId);
                int count = cartDAO.getCartCount(currentUser.getUserID());
                session.setAttribute("cartCount", count);
                sendResponse(response, isAjax, true, "Đã xóa sản phẩm khỏi giỏ hàng.", count, request.getContextPath() + "/cart");
            } else {
                List<CartItem> sessionCart = getSessionCart(session);
                sessionCart.removeIf(item -> item.getVariantId() == variantId);
                int count = calculateSessionCartCount(sessionCart);
                session.setAttribute("cartCount", count);
                sendResponse(response, isAjax, true, "Đã xóa sản phẩm khỏi giỏ hàng.", count, request.getContextPath() + "/cart");
            }
        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi xóa sản phẩm: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    /**
     * Handles clearing the entire cart.
     */
    private void handleClear(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session, User currentUser, boolean isAjax)
            throws IOException {

        try {
            if (currentUser != null) {
                cartDAO.clearCart(currentUser.getUserID());
            } else {
                session.removeAttribute("sessionCart");
            }
            session.setAttribute("cartCount", 0);
            sendResponse(response, isAjax, true, "Đã dọn sạch toàn bộ giỏ hàng.", 0, request.getContextPath() + "/cart");
        } catch (SQLException e) {
            sendResponse(response, isAjax, false, "Lỗi khi làm trống giỏ hàng: " + e.getMessage(), 0, request.getContextPath() + "/cart");
        }
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getSessionCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessionCart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("sessionCart", cart);
        }
        return cart;
    }

    private int calculateSessionCartCount(List<CartItem> cart) {
        int count = 0;
        if (cart != null) {
            for (CartItem item : cart) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    private void validateAndRefreshSessionCart(List<CartItem> cart, List<String> warnings) {
        if (cart == null) return;
        Iterator<CartItem> it = cart.iterator();
        while (it.hasNext()) {
            CartItem item = it.next();
            try {
                CartItem live = cartDAO.getVariantDetailForCart(item.getVariantId());
                if (live == null || !live.isActiveProduct()) {
                    warnings.add("Sản phẩm '" + item.getProductName() + "' đã ngừng bán.");
                    item.setActiveProduct(false);
                } else {
                    item.setUnitPrice(live.getUnitPrice());
                    item.setStockQuantity(live.getStockQuantity());
                    item.setImageUrl(live.getImageUrl());
                    if (item.getQuantity() > live.getStockQuantity()) {
                        item.setQuantity(Math.max(1, (int) live.getStockQuantity()));
                    }
                }
            } catch (SQLException e) {
                // Ignore transient db errors
            }
        }
    }

    private void sendResponse(HttpServletResponse response, boolean isAjax, boolean success,
                              String message, int cartCount, String redirectUrl) throws IOException {
        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(String.format("{\"success\":%b,\"message\":\"%s\",\"cartCount\":%d}",
                    success, escapeJson(message), cartCount));
            out.flush();
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    private void sendAjaxCartSummary(HttpServletResponse response, int userId, CartResult result)
            throws IOException, SQLException {
        List<CartItem> items = cartDAO.getCartItemsByUserId(userId);
        double subtotal = 0;
        int count = 0;
        for (CartItem it : items) {
            subtotal += it.getItemTotal();
            count += it.getQuantity();
        }
        double shipping = (subtotal >= FREE_SHIPPING_THRESHOLD || items.isEmpty()) ? 0 : STANDARD_SHIPPING_FEE;
        double grandTotal = subtotal + shipping;

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(String.format(Locale.US,
                "{\"success\":%b,\"message\":\"%s\",\"adjustedQuantity\":%d,\"cartCount\":%d,\"subtotal\":%.0f,\"shippingFee\":%.0f,\"grandTotal\":%.0f}",
                result.isSuccess(), escapeJson(result.getMessage()), result.getAdjustedQuantity(), count, subtotal, shipping, grandTotal));
        out.flush();
    }

    private void sendAjaxSessionCartSummary(HttpServletResponse response, List<CartItem> items)
            throws IOException {
        double subtotal = 0;
        int count = 0;
        for (CartItem it : items) {
            subtotal += it.getItemTotal();
            count += it.getQuantity();
        }
        double shipping = (subtotal >= FREE_SHIPPING_THRESHOLD || items.isEmpty()) ? 0 : STANDARD_SHIPPING_FEE;
        double grandTotal = subtotal + shipping;

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(String.format(Locale.US,
                "{\"success\":true,\"message\":\"Cập nhật thành công\",\"cartCount\":%d,\"subtotal\":%.0f,\"shippingFee\":%.0f,\"grandTotal\":%.0f}",
                count, subtotal, shipping, grandTotal));
        out.flush();
    }

    private int parseInt(String str, int defaultValue) {
        if (str == null || str.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
