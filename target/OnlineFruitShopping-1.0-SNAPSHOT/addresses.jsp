<%@page import="java.util.List"%>
<%@page import="com.mycompany.onlinefruitshopping.model.Address"%>

<%@page contentType="text/html"
        pageEncoding="UTF-8"%>

<%
    List<Address> addresses =
            (List<Address>) request.getAttribute("addresses");

    String success = request.getParameter("success");
    String error = (String) request.getAttribute("error");

    String editIdParam = request.getParameter("editId");

    Address editAddress = null;

    if (editIdParam != null && addresses != null) {

        try {

            int editId = Integer.parseInt(editIdParam);

            for (Address a : addresses) {

                if (a.getAddressId() == editId) {
                    editAddress = a;
                    break;
                }
            }

        } catch (NumberFormatException ignored) {
        }
    }

    Object editIdObj = request.getAttribute("editId");

    if (editAddress == null
            && editIdObj != null
            && addresses != null) {

        try {

            int editId = Integer.parseInt(
                    editIdObj.toString()
            );

            for (Address a : addresses) {

                if (a.getAddressId() == editId) {
                    editAddress = a;
                    break;
                }
            }

        } catch (NumberFormatException ignored) {
        }
    }


    /*
     * Values returned from server after validation error.
     */
    String formRecipientName =
            request.getAttribute("recipientName") != null
                    ? request.getAttribute("recipientName").toString()
                    : "";

    String formPhone =
            request.getAttribute("addressPhone") != null
                    ? request.getAttribute("addressPhone").toString()
                    : "";

    String formAddressLine =
            request.getAttribute("addressLine") != null
                    ? request.getAttribute("addressLine").toString()
                    : "";

    String formWard =
            request.getAttribute("ward") != null
                    ? request.getAttribute("ward").toString()
                    : "";

    String formDistrict =
            request.getAttribute("district") != null
                    ? request.getAttribute("district").toString()
                    : "";

    String formProvince =
            request.getAttribute("province") != null
                    ? request.getAttribute("province").toString()
                    : "";

    boolean formDefault =
            request.getAttribute("isDefault") != null
                    && Boolean.TRUE.equals(
                            request.getAttribute("isDefault")
                    );


    /*
     * If editing an existing address,
     * use its current values.
     */
    if (editAddress != null) {

        formRecipientName = editAddress.getRecipientName();
        formPhone = editAddress.getPhone();
        formAddressLine = editAddress.getAddressLine();
        formWard = editAddress.getWard();
        formDistrict = editAddress.getDistrict();
        formProvince = editAddress.getProvince();
        formDefault = editAddress.isDefault();
    }
%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Manage Addresses - FreshFruit</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f5f7fa;
            font-family: Arial, sans-serif;
        }

        .container {
            width: 1000px;
            max-width: 94%;
            margin: 40px auto;
        }

        h1 {
            text-align: center;
            color: #198754;
            margin-bottom: 30px;
        }

        .message {
            padding: 13px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }

        .success {
            background: #d1e7dd;
            color: #0f5132;
            border: 1px solid #badbcc;
        }

        .error {
            background: #f8d7da;
            color: #842029;
            border: 1px solid #f5c2c7;
        }

        .card {
            background: white;
            padding: 25px;
            margin-bottom: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.07);
        }

        .card h2 {
            margin-top: 0;
            color: #333;
        }

        .form-group {
            margin-bottom: 18px;
        }

        label {
            display: block;
            font-weight: bold;
            margin-bottom: 7px;
            color: #333;
        }

        input[type="text"] {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
            font-size: 14px;
            transition: 0.2s;
        }

        input[type="text"]:focus {
            outline: none;
            border-color: #198754;
            box-shadow: 0 0 0 2px rgba(25,135,84,0.1);
        }

        input.input-error {
            border-color: #dc3545;
            background: #fff8f8;
        }

        .field-error {
            display: none;
            color: #dc3545;
            font-size: 13px;
            margin-top: 6px;
        }

        .field-error.show {
            display: block;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .checkbox {
            display: flex;
            align-items: center;
            gap: 8px;
            margin: 10px 0 20px;
        }

        .checkbox input {
            width: auto;
        }

        .checkbox label {
            margin: 0;
            font-weight: normal;
        }

        button {
            border: none;
            padding: 11px 18px;
            border-radius: 7px;
            cursor: pointer;
            font-weight: bold;
            font-size: 14px;
        }

        .save-button {
            background: #198754;
            color: white;
        }

        .save-button:hover {
            background: #157347;
        }

        .cancel-button {
            background: #6c757d;
            color: white;
            text-decoration: none;
            padding: 11px 18px;
            border-radius: 7px;
            display: inline-block;
            margin-left: 5px;
        }

        .cancel-button:hover {
            background: #5c636a;
        }

        .address-card {
            border: 1px solid #ddd;
            border-radius: 10px;
            padding: 18px;
            margin-bottom: 15px;
        }

        .address-card.default {
            border: 2px solid #198754;
        }

        .default-badge {
            display: inline-block;
            background: #198754;
            color: white;
            padding: 4px 9px;
            border-radius: 20px;
            font-size: 12px;
            margin-bottom: 10px;
        }

        .address-info {
            line-height: 1.7;
        }

        .address-actions {
            margin-top: 15px;
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .edit-btn {
            background: #0d6efd;
            color: white;
        }

        .delete-btn {
            background: #dc3545;
            color: white;
        }

        .default-btn {
            background: #ffc107;
            color: #212529;
        }

        .back-btn {
            display: inline-block;
            text-decoration: none;
            color: white;
            background: #198754;
            padding: 11px 18px;
            border-radius: 7px;
            margin-top: 5px;
        }

        .back-btn:hover {
            background: #157347;
        }

        .empty {
            text-align: center;
            color: #777;
            padding: 20px;
        }

        @media (max-width: 700px) {

            .form-row {
                grid-template-columns: 1fr;
            }

        }

    </style>

</head>

<body>

<div class="container">

    <h1>Manage Addresses</h1>


    <!-- SUCCESS MESSAGE -->

    <% if ("added".equals(success)) { %>

        <div class="message success">
            Address added successfully.
        </div>

    <% } else if ("updated".equals(success)) { %>

        <div class="message success">
            Address updated successfully.
        </div>

    <% } else if ("deleted".equals(success)) { %>

        <div class="message success">
            Address deleted successfully.
        </div>

    <% } else if ("default".equals(success)) { %>

        <div class="message success">
            Default address updated successfully.
        </div>

    <% } %>


    <!-- SERVER VALIDATION ERROR -->

    <% if (error != null && !error.trim().isEmpty()) { %>

        <div class="message error">
            <%= error %>
        </div>

    <% } %>


    <!-- =====================================================
         ADD / EDIT ADDRESS
         ===================================================== -->

    <div class="card">

        <h2>
            <%= editAddress == null
                    ? "Add New Address"
                    : "Edit Address" %>
        </h2>


        <form id="addressForm"
              method="post"
              action="<%= request.getContextPath() %>/addresses"
              novalidate>

            <input type="hidden"
                   name="action"
                   value="<%= editAddress == null
                           ? "add"
                           : "update" %>">


            <% if (editAddress != null) { %>

                <input type="hidden"
                       name="addressId"
                       value="<%= editAddress.getAddressId() %>">

            <% } %>


            <!-- RECIPIENT NAME -->

            <div class="form-group">

                <label for="recipientName">
                    Recipient Name
                </label>

                <input type="text"
                       id="recipientName"
                       name="recipientName"
                       maxlength="100"
                       value="<%= formRecipientName %>"
                       placeholder="Enter recipient name">

                <div id="recipientNameError"
                     class="field-error">
                </div>

            </div>


            <!-- PHONE -->

            <div class="form-group">

                <label for="phone">
                    Phone Number
                </label>

                <input type="text"
                       id="phone"
                       name="phone"
                       maxlength="11"
                       value="<%= formPhone %>"
                       placeholder="Example: 0912345678">

                <div id="phoneError"
                     class="field-error">
                </div>

            </div>


            <!-- ADDRESS -->

            <div class="form-group">

                <label for="addressLine">
                    Address
                </label>

                <input type="text"
                       id="addressLine"
                       name="addressLine"
                       maxlength="255"
                       value="<%= formAddressLine %>"
                       placeholder="House number, street...">

                <div id="addressLineError"
                     class="field-error">
                </div>

            </div>


            <!-- WARD + DISTRICT -->

            <div class="form-row">

                <div class="form-group">

                    <label for="ward">
                        Ward
                    </label>

                    <input type="text"
                           id="ward"
                           name="ward"
                           maxlength="100"
                           value="<%= formWard %>"
                           placeholder="Ward">

                    <div id="wardError"
                         class="field-error">
                    </div>

                </div>


                <div class="form-group">

                    <label for="district">
                        District
                    </label>

                    <input type="text"
                           id="district"
                           name="district"
                           maxlength="100"
                           value="<%= formDistrict %>"
                           placeholder="District">

                    <div id="districtError"
                         class="field-error">
                    </div>

                </div>

            </div>


            <!-- PROVINCE -->

            <div class="form-group">

                <label for="province">
                    Province / City
                </label>

                <input type="text"
                       id="province"
                       name="province"
                       maxlength="100"
                       value="<%= formProvince %>"
                       placeholder="Province / City">

                <div id="provinceError"
                     class="field-error">
                </div>

            </div>


            <!-- DEFAULT -->

            <div class="checkbox">

                <input type="checkbox"
                       id="isDefault"
                       name="isDefault"
                       value="true"
                    <%= formDefault ? "checked" : "" %>>

                <label for="isDefault">
                    Set as default address
                </label>

            </div>


            <!-- BUTTON -->

            <button type="submit"
                    class="save-button">

                <%= editAddress == null
                        ? "Add Address"
                        : "Update Address" %>

            </button>


            <% if (editAddress != null) { %>

                <a class="cancel-button"
                   href="<%= request.getContextPath() %>/addresses">

                    Cancel

                </a>

            <% } %>

        </form>

    </div>


    <!-- =====================================================
         ADDRESS LIST
         ===================================================== -->

    <div class="card">

        <h2>
            My Addresses
        </h2>


        <% if (addresses == null || addresses.isEmpty()) { %>

            <div class="empty">
                You do not have any saved addresses yet.
            </div>

        <% } else { %>


            <% for (Address address : addresses) { %>

                <div class="address-card
                     <%= address.isDefault()
                             ? "default"
                             : "" %>">


                    <% if (address.isDefault()) { %>

                        <span class="default-badge">
                            Default Address
                        </span>

                    <% } %>


                    <div class="address-info">

                        <strong>
                            <%= address.getRecipientName() %>
                        </strong>

                        <br>

                        Phone:
                        <%= address.getPhone() %>

                        <br>

                        <%= address.getAddressLine() %>,
                        <%= address.getWard() %>,
                        <%= address.getDistrict() %>,
                        <%= address.getProvince() %>

                    </div>


                    <div class="address-actions">


                        <!-- EDIT -->

                        <a class="cancel-button"
                           style="background:#0d6efd;"
                           href="<%= request.getContextPath() %>/addresses?editId=<%= address.getAddressId() %>">

                            Edit

                        </a>


                        <!-- DELETE -->

                        <form method="post"
                              action="<%= request.getContextPath() %>/addresses"
                              style="display:inline;">

                            <input type="hidden"
                                   name="action"
                                   value="delete">

                            <input type="hidden"
                                   name="addressId"
                                   value="<%= address.getAddressId() %>">

                            <button type="submit"
                                    class="delete-btn"
                                    onclick="return confirmDelete();">

                                Delete

                            </button>

                        </form>


                        <!-- SET DEFAULT -->

                        <% if (!address.isDefault()) { %>

                            <form method="post"
                                  action="<%= request.getContextPath() %>/addresses"
                                  style="display:inline;">

                                <input type="hidden"
                                       name="action"
                                       value="setDefault">

                                <input type="hidden"
                                       name="addressId"
                                       value="<%= address.getAddressId() %>">

                                <button type="submit"
                                        class="default-btn">

                                    Set Default

                                </button>

                            </form>

                        <% } %>


                    </div>

                </div>

            <% } %>

        <% } %>

    </div>


    <!-- BACK -->

    <a class="back-btn"
       href="<%= request.getContextPath() %>/profile">

        ← Back to Profile

    </a>

</div>


<script>

    const form =
        document.getElementById("addressForm");


    const recipientName =
        document.getElementById("recipientName");

    const phone =
        document.getElementById("phone");

    const addressLine =
        document.getElementById("addressLine");

    const ward =
        document.getElementById("ward");

    const district =
        document.getElementById("district");

    const province =
        document.getElementById("province");


    const recipientNameError =
        document.getElementById("recipientNameError");

    const phoneError =
        document.getElementById("phoneError");

    const addressLineError =
        document.getElementById("addressLineError");

    const wardError =
        document.getElementById("wardError");

    const districtError =
        document.getElementById("districtError");

    const provinceError =
        document.getElementById("provinceError");


    // =====================================================
    // SHOW ERROR
    // =====================================================

    function showError(input, errorElement, message) {

        input.classList.add("input-error");

        errorElement.textContent = message;

        errorElement.classList.add("show");
    }


    // =====================================================
    // CLEAR ERROR
    // =====================================================

    function clearError(input, errorElement) {

        input.classList.remove("input-error");

        errorElement.textContent = "";

        errorElement.classList.remove("show");
    }


    // =====================================================
    // VALIDATE RECIPIENT NAME
    // =====================================================

    function validateRecipientName() {

        const value =
            recipientName.value.trim();

        clearError(
            recipientName,
            recipientNameError
        );


        if (value === "") {

            showError(
                recipientName,
                recipientNameError,
                "Recipient name cannot be empty."
            );

            return false;
        }


        if (value.length < 2
                || value.length > 100) {

            showError(
                recipientName,
                recipientNameError,
                "Recipient name must be between 2 and 100 characters."
            );

            return false;
        }


        const nameRegex =
            /^[\p{L}][\p{L}\s'.-]*$/u;


        if (!nameRegex.test(value)) {

            showError(
                recipientName,
                recipientNameError,
                "Recipient name contains invalid characters."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // VALIDATE PHONE
    // =====================================================

    function validatePhone() {

        const value =
            phone.value.trim();

        clearError(
            phone,
            phoneError
        );


        if (value === "") {

            showError(
                phone,
                phoneError,
                "Phone number cannot be empty."
            );

            return false;
        }


        const phoneRegex =
            /^0\d{9,10}$/;


        if (!phoneRegex.test(value)) {

            showError(
                phone,
                phoneError,
                "Phone number must contain 10 or 11 digits and start with 0."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // VALIDATE ADDRESS
    // =====================================================

    function validateAddressLine() {

        const value =
            addressLine.value.trim();

        clearError(
            addressLine,
            addressLineError
        );


        if (value === "") {

            showError(
                addressLine,
                addressLineError,
                "Address cannot be empty."
            );

            return false;
        }


        if (value.length < 5
                || value.length > 255) {

            showError(
                addressLine,
                addressLineError,
                "Address must be between 5 and 255 characters."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // VALIDATE WARD
    // =====================================================

    function validateWard() {

        const value =
            ward.value.trim();

        clearError(
            ward,
            wardError
        );


        if (value === "") {

            showError(
                ward,
                wardError,
                "Ward cannot be empty."
            );

            return false;
        }


        if (value.length > 100) {

            showError(
                ward,
                wardError,
                "Ward cannot exceed 100 characters."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // VALIDATE DISTRICT
    // =====================================================

    function validateDistrict() {

        const value =
            district.value.trim();

        clearError(
            district,
            districtError
        );


        if (value === "") {

            showError(
                district,
                districtError,
                "District cannot be empty."
            );

            return false;
        }


        if (value.length > 100) {

            showError(
                district,
                districtError,
                "District cannot exceed 100 characters."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // VALIDATE PROVINCE
    // =====================================================

    function validateProvince() {

        const value =
            province.value.trim();

        clearError(
            province,
            provinceError
        );


        if (value === "") {

            showError(
                province,
                provinceError,
                "Province / City cannot be empty."
            );

            return false;
        }


        if (value.length > 100) {

            showError(
                province,
                provinceError,
                "Province / City cannot exceed 100 characters."
            );

            return false;
        }


        return true;
    }


    // =====================================================
    // SUBMIT VALIDATION
    // =====================================================

    form.addEventListener(
        "submit",
        function(event) {

            const validName =
                validateRecipientName();

            const validPhone =
                validatePhone();

            const validAddress =
                validateAddressLine();

            const validWard =
                validateWard();

            const validDistrict =
                validateDistrict();

            const validProvince =
                validateProvince();


            if (!validName
                    || !validPhone
                    || !validAddress
                    || !validWard
                    || !validDistrict
                    || !validProvince) {

                event.preventDefault();

                const firstError =
                    document.querySelector(
                        ".input-error"
                    );

                if (firstError) {

                    firstError.focus();

                    firstError.scrollIntoView({
                        behavior: "smooth",
                        block: "center"
                    });
                }

                return false;
            }

        }
    );


    // =====================================================
    // LIVE VALIDATION
    // =====================================================

    recipientName.addEventListener(
        "input",
        validateRecipientName
    );

    phone.addEventListener(
        "input",
        validatePhone
    );

    addressLine.addEventListener(
        "input",
        validateAddressLine
    );

    ward.addEventListener(
        "input",
        validateWard
    );

    district.addEventListener(
        "input",
        validateDistrict
    );

    province.addEventListener(
        "input",
        validateProvince
    );


    // =====================================================
    // DELETE CONFIRM
    // =====================================================

    function confirmDelete() {

        return confirm(
            "Are you sure you want to delete this address?"
        );
    }

</script>

</body>
</html>