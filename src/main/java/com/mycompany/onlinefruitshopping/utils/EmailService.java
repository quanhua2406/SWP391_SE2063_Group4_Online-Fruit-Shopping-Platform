package com.mycompany.onlinefruitshopping.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private EmailService() {
        // Prevent object creation
    }

    /**
     * OTP used while creating a new account.
     */
    public static void sendRegistrationOTP(
            String recipientEmail,
            String otp) throws MessagingException {

        sendOTP(
                recipientEmail,
                otp,
                "FreshFruit - Account Verification OTP",
                "Your FreshFruit account verification OTP is:",
                "If you did not try to create a FreshFruit "
                        + "account, please ignore this email."
        );
    }

    /**
     * OTP used to reset a forgotten password.
     */
    public static void sendPasswordResetOTP(
            String recipientEmail,
            String otp) throws MessagingException {

        sendOTP(
                recipientEmail,
                otp,
                "FreshFruit - Password Reset OTP",
                "Your FreshFruit password reset OTP is:",
                "If you did not request a password reset, "
                        + "please ignore this email."
        );
    }

    /**
     * Sends an OTP email. Kept public for backwards
     * compatibility; prefer the two named methods above.
     */
    public static void sendOTP(String recipientEmail, String otp)
            throws MessagingException {

        sendOTP(
                recipientEmail,
                otp,
                "FreshFruit - Password Reset OTP",
                "Your FreshFruit password reset OTP is:",
                "If you did not request a password reset, "
                        + "please ignore this email."
        );
    }

    private static void sendOTP(
            String recipientEmail,
            String otp,
            String subject,
            String intro,
            String footer) throws MessagingException {

        // =====================================================
        // 1. Load mail.properties from src/main/resources
        // =====================================================

        Properties config = new Properties();

        try (InputStream input =
                     EmailService.class
                             .getClassLoader()
                             .getResourceAsStream("mail.properties")) {

            if (input == null) {
                throw new MessagingException(
                        "mail.properties not found."
                );
            }

            config.load(input);

        } catch (IOException e) {

            throw new MessagingException(
                    "Cannot load mail.properties.",
                    e
            );
        }

        // =====================================================
        // 2. Read email configuration
        // =====================================================

        String username =
                config.getProperty("mail.username");

        String password =
                config.getProperty("mail.password");

        if (username == null || username.isBlank()) {

            throw new MessagingException(
                    "mail.username is missing."
            );
        }

        if (password == null || password.isBlank()) {

            throw new MessagingException(
                    "mail.password is missing."
            );
        }

        if (recipientEmail == null
                || recipientEmail.isBlank()) {

            throw new MessagingException(
                    "Recipient email is missing."
            );
        }

        if (otp == null || otp.isBlank()) {

            throw new MessagingException(
                    "OTP is missing."
            );
        }

        // Remove unnecessary spaces
        final String finalUsername =
                username.trim();

        final String finalPassword =
                password.trim();

        final String finalRecipient =
                recipientEmail.trim();

        final String finalOtp =
                otp.trim();

        // =====================================================
        // 3. Debug information
        // =====================================================

        System.out.println(
                "========================================"
        );

        System.out.println(
                "EMAIL CONFIG"
        );

        System.out.println(
                "Sender: "
                        + finalUsername
        );

        System.out.println(
                "Password: LOADED"
        );

        System.out.println(
                "Recipient: "
                        + finalRecipient
        );

        System.out.println(
                "OTP: "
                        + finalOtp
        );

        System.out.println(
                "========================================"
        );

        // =====================================================
        // 4. Gmail SMTP configuration
        // =====================================================

        Properties mailProperties =
                new Properties();

        mailProperties.put(
                "mail.smtp.auth",
                "true"
        );

        mailProperties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        mailProperties.put(
                "mail.smtp.host",
                "smtp.gmail.com"
        );

        mailProperties.put(
                "mail.smtp.port",
                "587"
        );

        mailProperties.put(
                "mail.smtp.connectiontimeout",
                "10000"
        );

        mailProperties.put(
                "mail.smtp.timeout",
                "10000"
        );

        mailProperties.put(
                "mail.smtp.writetimeout",
                "10000"
        );

        // =====================================================
        // 5. Create mail session
        // =====================================================

        Session session =
                Session.getInstance(
                        mailProperties,
                        new Authenticator() {

                            @Override
                            protected PasswordAuthentication
                            getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        finalUsername,
                                        finalPassword
                                );
                            }
                        }
                );

        // =====================================================
        // 6. Create email message
        // =====================================================

        Message message =
                new MimeMessage(session);

        // Sender
        message.setFrom(
                new InternetAddress(
                        finalUsername
                )
        );

        // Receiver
        message.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(
                        finalRecipient
                )
        );

        // Subject
        message.setSubject(subject);

        // =====================================================
        // 7. Email content
        // =====================================================

        String content =
                "Hello,\n\n"
                + intro + "\n\n"
                + finalOtp
                + "\n\n"
                + "This OTP will expire in 5 minutes.\n\n"
                + footer + "\n\n"
                + "FreshFruit Team";

        message.setText(content);

        // =====================================================
        // 8. Send email
        // =====================================================

        Transport.send(message);

        System.out.println(
                "OTP EMAIL SENT SUCCESSFULLY"
        );
    }

    // =====================================================
    // NOTIFICATION EMAILS (plain text)
    // =====================================================

    /**
     * Sends a plain-text notification email, e.g. the low stock
     * alert. Uses the same {@code mail.properties} account as the
     * OTP emails.
     *
     * @throws MessagingException when the mail is not configured or
     *         the SMTP server refuses the message
     */
    public static void sendTextEmail(String recipientEmail,
                                     String subject,
                                     String text)
            throws MessagingException {

        if (recipientEmail == null || recipientEmail.isBlank()) {

            throw new MessagingException(
                    "Recipient email is missing."
            );
        }

        if (subject == null || subject.isBlank()) {

            throw new MessagingException(
                    "Subject is missing."
            );
        }

        if (text == null || text.isBlank()) {

            throw new MessagingException(
                    "Email content is missing."
            );
        }

        Properties config = loadMailConfig();

        Session session = createSession(config);

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(
                config.getProperty("mail.username").trim()
        ));

        message.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(recipientEmail.trim())
        );

        message.setSubject(subject);

        message.setText(text);

        Transport.send(message);
    }

    private static Properties loadMailConfig()
            throws MessagingException {

        Properties config = new Properties();

        try (InputStream input =
                     EmailService.class
                             .getClassLoader()
                             .getResourceAsStream("mail.properties")) {

            if (input == null) {
                throw new MessagingException(
                        "mail.properties not found."
                );
            }

            config.load(input);

        } catch (IOException e) {

            throw new MessagingException(
                    "Cannot load mail.properties.",
                    e
            );
        }

        String username = config.getProperty("mail.username");
        String password = config.getProperty("mail.password");

        if (username == null || username.isBlank()) {
            throw new MessagingException("mail.username is missing.");
        }

        if (password == null || password.isBlank()) {
            throw new MessagingException("mail.password is missing.");
        }

        return config;
    }

    private static Session createSession(Properties config) {

        Properties mailProperties = new Properties();

        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.host", "smtp.gmail.com");
        mailProperties.put("mail.smtp.port", "587");
        mailProperties.put("mail.smtp.connectiontimeout", "10000");
        mailProperties.put("mail.smtp.timeout", "10000");
        mailProperties.put("mail.smtp.writetimeout", "10000");

        final String username =
                config.getProperty("mail.username").trim();

        final String password =
                config.getProperty("mail.password").trim();

        return Session.getInstance(
                mailProperties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                username,
                                password
                        );
                    }
                }
        );
    }
}