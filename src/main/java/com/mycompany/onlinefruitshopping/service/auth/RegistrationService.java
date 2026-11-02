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
            return "Họ và tên chỉ được gồm 2-100 chữ cái và khoảng trắng.";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            return "Vui lòng nhập địa chỉ email hợp lệ.";
        }

        if (!ValidationUtil.isValidPassword(password)) {
            return "Mật khẩu phải có ít nhất 8 ký tự và chứa ít nhất một chữ cái và một chữ số.";
        }

        if (confirmPassword == null
                || !password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp.";
        }

        if (phone == null || !phone.matches("\\d{9,11}")) {
            return "Số điện thoại phải gồm 9 đến 11 chữ số.";
        }

        if (address == null || address.isBlank()) {
            return "Địa chỉ không được để trống.";
        }

        if (address.length() > 255) {
            return "Địa chỉ không được vượt quá 255 ký tự.";
        }

        if (!isSelfRegisterRole(role)) {
            return "Vui lòng chọn vai trò hợp lệ "
                    + "(Khách hàng, Chủ cửa hàng hoặc Nhân viên giao hàng).";
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
                    "Không thể lưu thông tin đăng ký đang chờ xử lý."
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
