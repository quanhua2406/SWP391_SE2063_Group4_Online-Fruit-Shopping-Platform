/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.onlinefruitshopping.utils;

/**
 *
 * @author Dell
 */


import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null) {
                System.out.println("CONNECT DATABASE SUCCESS!");
            }

        } catch (Exception e) {
            System.out.println("CONNECT DATABASE FAILED!");
            e.printStackTrace();
        }
    }
}
