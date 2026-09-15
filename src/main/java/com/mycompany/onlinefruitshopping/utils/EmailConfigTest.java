package com.mycompany.onlinefruitshopping.utils;

public class EmailConfigTest {

    public static void main(String[] args) {

        String email = System.getenv("FRUITSHOP_EMAIL");
        String password = System.getenv("FRUITSHOP_EMAIL_PASSWORD");

        System.out.println("Email: " + email);

        if (password != null && !password.isBlank()) {
            System.out.println("Email password: LOADED");
        } else {
            System.out.println("Email password: NOT LOADED");
        }
    }
}