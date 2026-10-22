package com.mycompany.onlinefruitshopping.service.auth;

import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.utils.EmailService;
import com.mycompany.onlinefruitshopping.utils.ValidationUtil;

import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business logic for the "forgot password" flow:
 *
 * <ol>
 *   <li>request: email an OTP to a registered address</li>
 *   <li>verify: check the OTP the user typed</li>
 *   <li>reset: store a new BCrypt password hash</li>
 * </ol>
 */
public class PasswordResetService {

    // =========================================================
    // RESULT TYPES
    // =========================================================

    public enum RequestStatus {
        INVALID_EMAIL,
        ACCOUNT_NOT_FOUND,
        OTP_SENT,
        SEND_FAILED
    }

    /**
     * Maximum number of wrong OTP entries allowed before the
     * OTP is invalidated (basic brute-force protection).
     */
    private static final int MAX_OTP_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Number of consecutive failed OTP checks per email.
     * Kept in memory: good enough to slow down brute force
     * without requiring a schema change.
     */
    private static final Map<String, Integer> FAILED_ATTEMPTS =
            new ConcurrentHashMap<>();

    private final UserDAO userDAO = new UserDAO();

    // =========================================================
    // STEP 1 - REQUEST OTP
    // =========================================================

    public RequestStatus requestReset(String email)
            throws SQLException {

        if (!ValidationUtil.isValidEmail(email)) {
            return RequestStatus.INVALID_EMAIL;
        }

        if (!userDAO.emailExists(email)) {
            return RequestStatus.ACCOUNT_NOT_FOUND;
        }

        String otp = generateOTP();

        boolean saved = userDAO.saveResetOTP(email, otp);

        if (!saved) {
            return RequestStatus.SEND_FAILED;
        }

        // Fresh OTP -> reset the failed-attempt counter
        FAILED_ATTEMPTS.remove(email);

        try {

            EmailService.sendPasswordResetOTP(email, otp);

        } catch (MessagingException e) {

            // OTP is stored but the mail could not be sent.
            return RequestStatus.SEND_FAILED;
        }

        return RequestStatus.OTP_SENT;
    }

    // =========================================================
    // STEP 2 - VERIFY OTP
    // =========================================================

    public boolean verifyOtp(String email,
                             String otp)
            throws SQLException {

        if (!RegistrationService.isValidOtp(otp)) {
            return false;
        }

        // -----------------------------------------------------
        // Brute-force guard
        // -----------------------------------------------------

        int attempts = FAILED_ATTEMPTS.getOrDefault(email, 0);

        if (attempts >= MAX_OTP_ATTEMPTS) {

            // Too many wrong tries -> invalidate the OTP.
            userDAO.clearResetOTP(email);

            return false;
        }

        boolean verified = userDAO.verifyResetOTP(email, otp);

        if (!verified) {

            FAILED_ATTEMPTS.put(email, attempts + 1);
            return false;
        }

        // -----------------------------------------------------
        // Success -> OTP can only be used once
        // -----------------------------------------------------

        FAILED_ATTEMPTS.remove(email);
        userDAO.clearResetOTP(email);

        return true;
    }

    // =========================================================
    // STEP 3 - RESET PASSWORD
    // =========================================================

    /**
     * @return an error message, or {@code null} when the
     *         password is acceptable.
     */
    public String validateNewPassword(String password,
                                      String confirmPassword) {

        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }

        if (!ValidationUtil.isValidPassword(password)) {
            return "Password must be at least 8 characters and contain "
                    + "at least one letter and one number.";
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Confirm password cannot be empty.";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }

        return null;
    }

    public boolean resetPassword(String email,
                                 String password)
            throws SQLException {

        return userDAO.resetPassword(email, password);
    }

    // =========================================================
    // OTP GENERATION
    // =========================================================

    private String generateOTP() {

        int number = 100000 + RANDOM.nextInt(900000);

        return String.valueOf(number);
    }
}
