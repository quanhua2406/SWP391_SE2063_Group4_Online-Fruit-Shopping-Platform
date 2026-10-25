package com.mycompany.onlinefruitshopping.controller.customer;

import com.mycompany.onlinefruitshopping.dao.user.AddressDAO;
import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.auth.LoginService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/addresses")
public class AddressServlet extends HttpServlet {

    private final AddressDAO addressDAO = new AddressDAO();

    // =========================================================
    // GET
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        // Only Customer can use Manage Addresses
        if (user.getRoleName() == null
                || !user.getRoleName().equalsIgnoreCase("Customer")) {

            response.sendRedirect(
                    request.getContextPath() + "/profile"
            );
            return;
        }

        try {

            List<Address> addresses =
                    addressDAO.getAddressesByUserId(user.getUserID());

            request.setAttribute("addresses", addresses);

            request.getRequestDispatcher("/jsp/customer/addresses.jsp")
                    .forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load addresses.",
                    e
            );
        }
    }

    // =========================================================
    // POST
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        // Only Customer
        if (user.getRoleName() == null
                || !user.getRoleName().equalsIgnoreCase("Customer")) {

            response.sendRedirect(
                    request.getContextPath() + "/profile"
            );
            return;
        }

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            response.sendRedirect(
                    request.getContextPath() + "/addresses"
            );
            return;
        }

        try {

            switch (action) {

                case "add":
                    addAddress(request, response, user);
                    break;

                case "update":
                    updateAddress(request, response, user);
                    break;

                case "delete":
                    deleteAddress(request, response, user);
                    break;

                case "setDefault":
                    setDefaultAddress(request, response, user);
                    break;

                default:
                    response.sendRedirect(
                            request.getContextPath() + "/addresses"
                    );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Address operation failed.",
                    e
            );
        }
    }

    // =========================================================
    // ADD
    // =========================================================
    private void addAddress(HttpServletRequest request,
                            HttpServletResponse response,
                            User user)
            throws SQLException, ServletException, IOException {

        String recipientName =
                clean(request.getParameter("recipientName"));

        String phone =
                clean(request.getParameter("phone"));

        String addressLine =
                clean(request.getParameter("addressLine"));

        String ward =
                clean(request.getParameter("ward"));

        String district =
                clean(request.getParameter("district"));

        String province =
                clean(request.getParameter("province"));

        boolean isDefault =
                "true".equalsIgnoreCase(
                        request.getParameter("isDefault")
                );

        String error = validateAddress(
                recipientName,
                phone,
                addressLine,
                ward,
                district,
                province
        );

        if (error != null) {

            setFormData(
                    request,
                    recipientName,
                    phone,
                    addressLine,
                    ward,
                    district,
                    province,
                    isDefault
            );

            request.setAttribute("error", error);

            loadAddressesAndForward(request, response, user);

            return;
        }

        Address address = new Address();

        address.setUserId(user.getUserID());
        address.setRecipientName(recipientName);
        address.setPhone(phone);
        address.setAddressLine(addressLine);
        address.setWard(ward);
        address.setDistrict(district);
        address.setProvince(province);
        address.setDefault(isDefault);

        addressDAO.addAddress(address);

        response.sendRedirect(
                request.getContextPath()
                + "/addresses?success=added"
        );
    }

    // =========================================================
    // UPDATE
    // =========================================================
    private void updateAddress(HttpServletRequest request,
                               HttpServletResponse response,
                               User user)
            throws SQLException, ServletException, IOException {

        int addressId;

        try {

            addressId = Integer.parseInt(
                    request.getParameter("addressId")
            );

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/addresses?error=invalid"
            );

            return;
        }

        String recipientName =
                clean(request.getParameter("recipientName"));

        String phone =
                clean(request.getParameter("phone"));

        String addressLine =
                clean(request.getParameter("addressLine"));

        String ward =
                clean(request.getParameter("ward"));

        String district =
                clean(request.getParameter("district"));

        String province =
                clean(request.getParameter("province"));

        boolean isDefault =
                "true".equalsIgnoreCase(
                        request.getParameter("isDefault")
                );

        String error = validateAddress(
                recipientName,
                phone,
                addressLine,
                ward,
                district,
                province
        );

        if (error != null) {

            request.setAttribute("editId", addressId);

            setFormData(
                    request,
                    recipientName,
                    phone,
                    addressLine,
                    ward,
                    district,
                    province,
                    isDefault
            );

            request.setAttribute("error", error);

            loadAddressesAndForward(request, response, user);

            return;
        }

        Address address = new Address();

        address.setAddressId(addressId);
        address.setUserId(user.getUserID());
        address.setRecipientName(recipientName);
        address.setPhone(phone);
        address.setAddressLine(addressLine);
        address.setWard(ward);
        address.setDistrict(district);
        address.setProvince(province);
        address.setDefault(isDefault);

        addressDAO.updateAddress(address);

        response.sendRedirect(
                request.getContextPath()
                + "/addresses?success=updated"
        );
    }

    // =========================================================
    // DELETE
    // =========================================================
    private void deleteAddress(HttpServletRequest request,
                               HttpServletResponse response,
                               User user)
            throws SQLException, IOException {

        try {

            int addressId = Integer.parseInt(
                    request.getParameter("addressId")
            );

            addressDAO.deleteAddress(
                    addressId,
                    user.getUserID()
            );

        } catch (NumberFormatException e) {
            // Ignore invalid ID
        }

        response.sendRedirect(
                request.getContextPath()
                + "/addresses?success=deleted"
        );
    }

    // =========================================================
    // SET DEFAULT
    // =========================================================
    private void setDefaultAddress(HttpServletRequest request,
                                   HttpServletResponse response,
                                   User user)
            throws SQLException, IOException {

        try {

            int addressId = Integer.parseInt(
                    request.getParameter("addressId")
            );

            addressDAO.setDefaultAddress(
                    addressId,
                    user.getUserID()
            );

        } catch (NumberFormatException e) {
            // Ignore invalid ID
        }

        response.sendRedirect(
                request.getContextPath()
                + "/addresses?success=default"
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private String validateAddress(
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String province) {

        // Recipient name
        if (recipientName.isEmpty()) {
            return "Recipient name cannot be empty.";
        }

        if (recipientName.length() < 2
                || recipientName.length() > 100) {

            return "Recipient name must be between 2 and 100 characters.";
        }

        if (!recipientName.matches(
                "^[\\p{L}][\\p{L}\\s'.-]*$")) {

            return "Recipient name contains invalid characters.";
        }

        // Phone
        if (phone.isEmpty()) {
            return "Phone number cannot be empty.";
        }

        if (!phone.matches("^0\\d{9,10}$")) {
            return "Phone number must contain 10 or 11 digits and start with 0.";
        }

        // Address
        if (addressLine.isEmpty()) {
            return "Address cannot be empty.";
        }

        if (addressLine.length() < 5
                || addressLine.length() > 255) {

            return "Address must be between 5 and 255 characters.";
        }

        // Ward
        if (ward.isEmpty()) {
            return "Ward cannot be empty.";
        }

        if (ward.length() > 100) {
            return "Ward cannot exceed 100 characters.";
        }

        // District
        if (district.isEmpty()) {
            return "District cannot be empty.";
        }

        if (district.length() > 100) {
            return "District cannot exceed 100 characters.";
        }

        // Province
        if (province.isEmpty()) {
            return "Province cannot be empty.";
        }

        if (province.length() > 100) {
            return "Province cannot exceed 100 characters.";
        }

        return null;
    }

    // =========================================================
    // CLEAN INPUT
    // =========================================================
    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    // =========================================================
    // KEEP FORM DATA WHEN VALIDATION FAILS
    // =========================================================
    private void setFormData(
            HttpServletRequest request,
            String recipientName,
            String phone,
            String addressLine,
            String ward,
            String district,
            String province,
            boolean isDefault) {

        request.setAttribute("recipientName", recipientName);
        request.setAttribute("addressPhone", phone);
        request.setAttribute("addressLine", addressLine);
        request.setAttribute("ward", ward);
        request.setAttribute("district", district);
        request.setAttribute("province", province);
        request.setAttribute("isDefault", isDefault);
    }

    // =========================================================
    // LOAD ADDRESSES AFTER VALIDATION ERROR
    // =========================================================
    private void loadAddressesAndForward(
            HttpServletRequest request,
            HttpServletResponse response,
            User user)
            throws SQLException, ServletException, IOException {

        List<Address> addresses =
                addressDAO.getAddressesByUserId(
                        user.getUserID()
                );

        request.setAttribute("addresses", addresses);

        request.getRequestDispatcher("/jsp/customer/addresses.jsp")
                .forward(request, response);
    }
}