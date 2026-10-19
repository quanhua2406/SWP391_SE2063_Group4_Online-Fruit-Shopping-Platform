package com.mycompany.onlinefruitshopping.service.auth;

import com.mycompany.onlinefruitshopping.dao.auth.RegistrationVerificationDAO;
import com.mycompany.onlinefruitshopping.dao.auth.RegistrationVerificationDAO.PendingRegistration;
import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.utils.EmailService;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.sql.SQLException;

/**
 * Business logic for the sign-up flow:
 *
 * <ol>
 *   <li>validate the registration form</li>
 *   <li>store a pending registration + email an OTP</li>
 *   <li>create the real account once the OTP is verified</li>
 * </ol>
 */
public class RegistrationService {

    // =========================================================
    // RESULT TYPES
    // =========================================================

    public enum VerifyStatus {
        SUCCESS,
        INVALID_OTP,
        INVALID_ROLE,
        PENDING_NOT_FOUND,
        EMAIL_ALREADY_REGISTERED,
        CREATE_FAILED
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RegistrationVerificationDAO verificationDAO =
            new RegistrationVerificationDAO();

    private final UserDAO userDAO = new UserDAO();

    /**
     * Roles a visitor may pick when signing up.
     * Admin accounts can never be self-registered.
     */
    public static final java.util.List<String> SELF_REGISTER_ROLES =
            java.util.List.of(
                    "CUSTOMER",
                    "SHOPOWNER",
                    "DELIVERYSTAFF"
            );

    /**
     * Normalizes {@code role} and checks whether it is one of the
     * roles allowed at self-registration.
     */
    public static boolean isSelfRegisterRole(String role) {

        return SELF_REGISTER_ROLES.contains(
                ValidationUtil.normalizeRoleName(role)
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * @return an error message, or {@code null} when the form is valid.
     */
    public String validateRegistration(String fullName,
                                       String email,
                                       String password,
                                       String confirmPassword,
                                       String phone,
                                       String address,
                                       String role) {

        if (!ValidationUtil.isValidFullName(fullName)) {
            return "Full name must contain 2-100 letters and spaces only.";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return "Please enter a valid email address.";
        }

        if (!ValidationUtil.isValidPassword(password)) {
            return "Password must be at least 8 characters and contain "
                    + "at least one letter and one number.";
        }

        if (confirmPassword == null
                || !password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        if (phone == null || !phone.matches("\\d{9,11}")) {
            return "Phone number must contain 9 to 11 digits.";
        }

        if (address == null || address.isBlank()) {
            return "Address cannot be empty.";
        }

        if (address.length() > 255) {
            return "Address cannot exceed 255 characters.";
        }

        if (!isSelfRegisterRole(role)) {
            return "Please choose a valid role "
                    + "(Customer, Shop Owner or Delivery Staff).";
        }

        return null;
    }

    public boolean isEmailRegistered(String email)
            throws SQLException {

        return userDAO.emailExists(email);
    }

    /**
     * Checks that the requested role really exists in the database
     * before a pending registration is stored.
     */
    public boolean roleExists(String role)
            throws SQLException {

        return userDAO.roleExistsByName(role);
    }

    // =========================================================
    // START REGISTRATION (STORE PENDING + SEND OTP)
    // =========================================================

    /**
     * Stores a pending registration and emails the OTP.
     */
    public void startRegistration(String fullName,
                                  String email,
                                  String password,
                                  String phone,
                                  String address)
            throws SQLException, MessagingException {

        String otp = generateOTP();

        boolean saved =
                verificationDAO.savePendingRegistration(
                        fullName,
                        email,
                        password,
                        phone,
                        address,
                        otp
                );

        if (!saved) {
            throw new SQLException(
                    "Unable to store pending registration."
            );
        }

        EmailService.sendRegistrationOTP(email, otp);
    }

    // =========================================================
    // COMPLETE REGISTRATION (VERIFY OTP + CREATE USER)
    // =========================================================

    public VerifyStatus completeRegistration(String email,
                                             String otp,
                                             String role)
            throws SQLException {

        if (!isValidOtp(otp)) {
            return VerifyStatus.INVALID_OTP;
        }

        if (!isSelfRegisterRole(role)) {
            return VerifyStatus.INVALID_ROLE;
        }

        if (!verificationDAO.verifyOTP(email, otp)) {
            return VerifyStatus.INVALID_OTP;
        }

        PendingRegistration pending =
                verificationDAO.getPendingRegistration(email);

        if (pending == null) {
            return VerifyStatus.PENDING_NOT_FOUND;
        }

        // Email could have been registered while the OTP was pending.
        if (userDAO.emailExists(email)) {

            verificationDAO.deletePendingRegistration(email);

            return VerifyStatus.EMAIL_ALREADY_REGISTERED;
        }

        boolean registered =
                userDAO.registerUser(
                        pending.getFullName(),
                        pending.getEmail(),
                        pending.getPasswordHash(),
                        pending.getPhone(),
                        pending.getAddress(),
                        role
                );

        if (!registered) {
            return VerifyStatus.CREATE_FAILED;
        }

        verificationDAO.deletePendingRegistration(email);

        return VerifyStatus.SUCCESS;
    }

    // =========================================================
    // OTP HELPERS
    // =========================================================

    public static boolean isValidOtp(String otp) {

        return otp != null && otp.matches("\\d{6}");
    }

    private String generateOTP() {

        int number = 100000 + RANDOM.nextInt(900000);

        return String.valueOf(number);
    }
}
