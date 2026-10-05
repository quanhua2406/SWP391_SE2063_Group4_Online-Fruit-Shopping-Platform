package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationVerificationDAO {

    // =========================================================
    // SAVE PENDING REGISTRATION
    // =========================================================

    public boolean savePendingRegistration(
            String fullName,
            String email,
            String password,
            String phone,
            String address,
            String otp) throws SQLException {

        // Hash password before storing it
        String passwordHash =
                BCrypt.hashpw(
                        password,
                        BCrypt.gensalt(12)
                );

        String sql = """
                INSERT INTO registration_verifications
                (
                    full_name,
                    email,
                    password_hash,
                    phone,
                    address,
                    otp,
                    otp_expiry
                )
                VALUES
                (
                    ?, ?, ?, ?, ?, ?,
                    DATEADD(MINUTE, 5, GETDATE())
                )
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, phone);
            ps.setString(5, address);
            ps.setString(6, otp);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // GET PENDING REGISTRATION
    // =========================================================

    public PendingRegistration getPendingRegistration(
            String email) throws SQLException {

        String sql = """
                SELECT
                    verification_id,
                    full_name,
                    email,
                    password_hash,
                    phone,
                    address,
                    otp,
                    otp_expiry
                FROM registration_verifications
                WHERE email = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    PendingRegistration pending =
                            new PendingRegistration();

                    pending.setVerificationId(
                            rs.getInt("verification_id")
                    );

                    pending.setFullName(
                            rs.getString("full_name")
                    );

                    pending.setEmail(
                            rs.getString("email")
                    );

                    pending.setPasswordHash(
                            rs.getString("password_hash")
                    );

                    pending.setPhone(
                            rs.getString("phone")
                    );

                    pending.setAddress(
                            rs.getString("address")
                    );

                    pending.setOtp(
                            rs.getString("otp")
                    );

                    pending.setOtpExpiry(
                            rs.getTimestamp("otp_expiry")
                    );

                    return pending;
                }
            }
        }

        return null;
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    public boolean verifyOTP(
            String email,
            String otp) throws SQLException {

        String sql = """
                SELECT verification_id
                FROM registration_verifications
                WHERE email = ?
                  AND otp = ?
                  AND otp_expiry > GETDATE()
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);
            ps.setString(2, otp);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // =========================================================
    // DELETE PENDING REGISTRATION
    // =========================================================

    public boolean deletePendingRegistration(
            String email) throws SQLException {

        String sql = """
                DELETE FROM registration_verifications
                WHERE email = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, email);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // PENDING REGISTRATION MODEL
    // =========================================================

    public static class PendingRegistration {

        private int verificationId;
        private String fullName;
        private String email;
        private String passwordHash;
        private String phone;
        private String address;
        private String otp;
        private java.sql.Timestamp otpExpiry;

        public int getVerificationId() {
            return verificationId;
        }

        public void setVerificationId(int verificationId) {
            this.verificationId = verificationId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPasswordHash() {
            return passwordHash;
        }

        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }

        public java.sql.Timestamp getOtpExpiry() {
            return otpExpiry;
        }

        public void setOtpExpiry(
                java.sql.Timestamp otpExpiry) {
            this.otpExpiry = otpExpiry;
        }
    }
}