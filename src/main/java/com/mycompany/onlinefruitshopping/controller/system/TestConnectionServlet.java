package com.mycompany.onlinefruitshopping.controller.system;

import com.mycompany.onlinefruitshopping.utils.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

@WebServlet("/test-db")
public class TestConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null) {
                response.getWriter().println("<h1>CONNECT DATABASE SUCCESS!</h1>");
                connection.close();
            }

        } catch (Exception e) {
            response.getWriter().println("<h1>CONNECT DATABASE FAILED!</h1>");
            response.getWriter().println("<pre>");
            e.printStackTrace(response.getWriter());
            response.getWriter().println("</pre>");
        }
    }
}