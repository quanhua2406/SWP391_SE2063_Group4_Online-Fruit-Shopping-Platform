package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.CustomerInfo;
import com.mycompany.onlinefruitshopping.dao.ShopOwnerRegistrationDAO.RequestInfo;
import com.mycompany.onlinefruitshopping.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(
    name = "ShopOwnerRegistrationServlet",
    urlPatterns = {
        "/register-shop-owner",
        "/shop-owner-status"
    }
)
public class ShopOwnerRegistrationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ShopOwnerRegistrationDAO dao;

    @Override
    public void init() throws ServletException {
        dao = new ShopOwnerRegistrationDAO();
    }

    // =====================================================
    // GET LOGGED-IN USER
    // =====================================================

    private User getLoggedInUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object sessionUser = session.getAttribute("user");

        if (sessionUser instanceof User) {
            return (User) sessionUser;
        }

        return null;
    }

    // =====================================================
    // GET: REGISTRATION FORM OR REQUEST STATUS
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login"
            );
            return;
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {

            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unable to identify the logged-in account."
            );
            return;
        }

        try {
            CustomerInfo customer =
                dao.getCustomerByEmail(user.getEmail().trim());

            if (customer == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only active customer accounts can register a shop."
                );
                return;
            }

            RequestInfo latestRequest =
                dao.getLatestRequest(customer.getUserId());

            request.setAttribute("customer", customer);
            request.setAttribute("latestRequest", latestRequest);

            // Display the request status page.
            if ("/shop-owner-status".equals(request.getServletPath())) {
                request.getRequestDispatcher(
                    "/shop-owner-status.jsp"
                ).forward(request, response);
                return;
            }

            // Resume the payment step for an unpaid request.
            if (latestRequest != null
                    && "PENDING_PAYMENT".equalsIgnoreCase(
                        latestRequest.getStatus())) {

                request.setAttribute(
                    "requestId",
                    latestRequest.getRequestId()
                );

                request.setAttribute(
                    "shopName",
                    latestRequest.getShopName()
                );

                request.getRequestDispatcher(
                    "/shop-owner-payment.jsp"
                ).forward(request, response);
                return;
            }

            // Do not allow duplicate requests during admin review.
            if (latestRequest != null
                    && ("PENDING_APPROVAL".equalsIgnoreCase(
                            latestRequest.getStatus())
                    || "PENDING".equalsIgnoreCase(
                            latestRequest.getStatus()))) {

                request.getRequestDispatcher(
                    "/shop-owner-status.jsp"
                ).forward(request, response);
                return;
            }

            request.getRequestDispatcher(
                "/register-shop-owner.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            log("Unable to load shop owner registration.", e);

            throw new ServletException(
                "Unable to load shop owner registration information.",
                e
            );
        }
    }

    // =====================================================
    // POST: SUBMIT SHOP OWNER REQUEST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = getLoggedInUser(request);

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login"
            );
            return;
        }

        if (user.getEmail() == null
                || user.getEmail().trim().isEmpty()) {

            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unable to identify the logged-in account."
            );
            return;
        }

        // Read form values.
        String shopName = trim(request.getParameter("shopName"));
        String description = trim(request.getParameter("description"));
        String shopPhone = trim(request.getParameter("shopPhone"));
        String shopEmail = trim(request.getParameter("shopEmail"));
        String shopAddress = trim(request.getParameter("shopAddress"));
        String city = trim(request.getParameter("city"));
        String businessType = trim(request.getParameter("businessType"));

        // Validate required fields.
        if (shopName.isEmpty()
                || description.isEmpty()
                || shopPhone.isEmpty()
                || shopAddress.isEmpty()
                || city.isEmpty()
                || businessType.isEmpty()) {

            showFormError(
                request, response,
                shopName, description, shopPhone,
                shopEmail, shopAddress, city, businessType,
                "Please complete all required fields."
            );
            return;
        }

        // Validate field lengths.
        if (shopName.length() > 150
                || description.length() > 1000
                || shopPhone.length() > 20
                || shopEmail.length() > 254
                || shopAddress.length() > 300
                || city.length() > 100
                || businessType.length() > 100) {

            showFormError(
                request, response,
                shopName, description, shopPhone,
                shopEmail, shopAddress, city, businessType,
                "One or more fields exceed the allowed length."
            );
            return;
        }

        // Validate optional shop email.
        if (!shopEmail.isEmpty()
                && !shopEmail.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showFormError(
                request, response,
                shopName, description, shopPhone,
                shopEmail, shopAddress, city, businessType,
                "Please enter a valid shop email address."
            );
            return;
        }

        try {
            CustomerInfo customer =
                dao.getCustomerByEmail(user.getEmail().trim());

            if (customer == null) {
                response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only active customer accounts can register a shop."
                );
                return;
            }

            // Check for an existing request.
            if (dao.hasOpenRequest(customer.getUserId())) {
                response.sendRedirect(
                    request.getContextPath() + "/register-shop-owner"
                );
                return;
            }

            // Submit the request using the existing boolean DAO method.
            boolean submitted = dao.submitRequest(
                customer.getUserId(),
                shopName,
                description,
                shopPhone,
                shopEmail,
                shopAddress,
                city,
                businessType
            );

            if (!submitted) {
                showFormError(
                    request, response,
                    shopName, description, shopPhone,
                    shopEmail, shopAddress, city, businessType,
                    "Unable to submit your request. "
                        + "A pending request may already exist."
                );
                return;
            }

            // Retrieve the saved request and its generated ID.
            RequestInfo latestRequest =
                dao.getLatestRequest(customer.getUserId());

            if (latestRequest == null) {
                throw new ServletException(
                    "The submitted request could not be retrieved."
                );
            }

            int requestId = latestRequest.getRequestId();

            // Store the request ID for the payment flow.
            HttpSession session = request.getSession();

            session.setAttribute(
                "pendingShopOwnerRequestId",
                requestId
            );

            // Pass request details to the payment page.
            request.setAttribute("requestId", requestId);
            request.setAttribute(
                "shopName",
                latestRequest.getShopName()
            );
            request.setAttribute("customer", customer);

            // Continue to shop owner payment.
            request.getRequestDispatcher(
                "/shop-owner-payment.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            log("Unable to submit shop owner request.", e);

            throw new ServletException(
                "Unable to submit the shop owner registration request.",
                e
            );
        }
    }

    // =====================================================
    // DISPLAY FORM WITH VALIDATION ERROR
    // =====================================================

    private void showFormError(
            HttpServletRequest request,
            HttpServletResponse response,
            String shopName,
            String description,
            String shopPhone,
            String shopEmail,
            String shopAddress,
            String city,
            String businessType,
            String errorMessage)
            throws ServletException, IOException {

        request.setAttribute("shopName", shopName);
        request.setAttribute("description", description);
        request.setAttribute("shopPhone", shopPhone);
        request.setAttribute("shopEmail", shopEmail);
        request.setAttribute("shopAddress", shopAddress);
        request.setAttribute("city", city);
        request.setAttribute("businessType", businessType);
        request.setAttribute("error", errorMessage);

        request.getRequestDispatcher(
            "/register-shop-owner.jsp"
        ).forward(request, response);
    }

    // =====================================================
    // TRIM INPUT VALUES
    // =====================================================

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}