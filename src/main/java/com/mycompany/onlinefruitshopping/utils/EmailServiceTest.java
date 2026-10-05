package com.mycompany.onlinefruitshopping.utils;

public class EmailServiceTest {

    public static void main(String[] args) {

        String recipientEmail = "he180293phamquangtien@gmail.com";
        String otp = "123456";

        try {

            EmailService.sendOTP(
                    recipientEmail,
                    otp
            );

            System.out.println("EMAIL SENT SUCCESS!");

        } catch (Exception e) {

            System.out.println("EMAIL SENT FAILED!");

            e.printStackTrace();
        }
    }
}