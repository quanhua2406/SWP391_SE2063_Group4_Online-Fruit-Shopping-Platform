package com.mycompany.onlinefruitshopping.controller;

import com.mycompany.onlinefruitshopping.dao.ProductDAO;
import com.mycompany.onlinefruitshopping.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/products/search-suggestions")
public class SearchSuggestionServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SearchSuggestionServlet.class.getName());
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        String query = request.getParameter("q");
        if (query == null || query.isBlank()) {
            query = request.getParameter("keyword");
        }

        PrintWriter out = response.getWriter();
        if (query == null || query.trim().length() < 1) {
            out.print("[]");
            return;
        }

        try {
            List<Product> suggestions = productDAO.getSearchSuggestions(query.trim(), 6);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < suggestions.size(); i++) {
                Product p = suggestions.get(i);
                if (i > 0) sb.append(",");
                sb.append("{");
                sb.append("\"id\":").append(p.getProductId()).append(",");
                sb.append("\"name\":").append(escapeJson(p.getProductName())).append(",");
                sb.append("\"category\":").append(escapeJson(p.getCategoryName() != null ? p.getCategoryName() : "")).append(",");
                sb.append("\"origin\":").append(escapeJson(p.getOrigin() != null ? p.getOrigin() : "")).append(",");
                sb.append("\"price\":").append(p.getPrice()).append(",");
                sb.append("\"unit\":").append(escapeJson(p.getUnit() != null ? p.getUnit() : "")).append(",");
                sb.append("\"packaging\":").append(escapeJson(p.getPackagingType() != null ? p.getPackagingType() : "")).append(",");
                sb.append("\"imageUrl\":").append(escapeJson(p.getImageUrl() != null ? p.getImageUrl() : ""));
                sb.append("}");
            }
            sb.append("]");
            out.print(sb.toString());

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lỗi tìm kiếm gợi ý", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
