package com.mycompany.onlinefruitshopping.utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private ValidationUtil() {
        // Prevent creating object
    }

    /**
     * Validate Gmail
     */
   public static boolean isValidEmail(String email) {

    if (email == null || email.isBlank()) {
        return false;
    }

    String value = email.trim().toLowerCase();

    String emailRegex =
            "^[A-Za-z0-9._%+-]+@(gmail\\.com|fpt\\.edu\\.vn)$";

    return Pattern.matches(emailRegex, value);
}

    /**
     * Validate Full Name
     *
     * Rules:
     * - Required
     * - 2 - 100 characters
     * - Cannot contain numbers
     * - Only letters and spaces
     */
    public static boolean isValidFullName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return false;
        }

        String value = fullName.trim();

        if (value.length() < 2 || value.length() > 100) {
            return false;
        }

        // Allow Vietnamese letters and spaces
        String nameRegex =
                "^[\\p{L} ]+$";

        return Pattern.matches(nameRegex, value);
    }

    /**
     * Validate Password
     *
     * Rules:
     * - At least 8 characters
     * - At least 1 letter
     * - At least 1 number
     */
    public static boolean isValidPassword(String password) {

        if (password == null || password.isBlank()) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {

            if (Character.isLetter(c)) {
                hasLetter = true;
            }

            if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        return hasLetter && hasNumber;
    }

    /**
     * Validate Phone
     *
     * Rules:
     * - Optional
     * - Only digits
     * - Must start with 0
     * - 10 or 11 digits
     */
    public static boolean isValidPhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return true;
        }

        String value = phone.trim();

        String phoneRegex =
                "^0[0-9]{9,10}$";

        return Pattern.matches(phoneRegex, value);
    }

    /**
     * Validate Address
     *
     * Rules:
     * - Optional
     * - Maximum 255 characters
     */
    public static boolean isValidAddress(String address) {

        if (address == null || address.isBlank()) {
            return true;
        }

        String value = address.trim();

        return value.length() <= 255;
    }
}