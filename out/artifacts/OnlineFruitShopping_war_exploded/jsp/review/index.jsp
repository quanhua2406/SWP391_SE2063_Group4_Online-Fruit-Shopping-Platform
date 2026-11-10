<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String pId = request.getParameter("productId");
    if (pId == null || pId.isBlank()) pId = request.getParameter("product_id");
    if (pId == null || pId.isBlank()) pId = request.getParameter("id");

    if (pId != null && !pId.isBlank()) {
        response.sendRedirect(request.getContextPath() + "/product?id=" + pId.trim() + "#tab-reviews");
    } else {
        response.sendRedirect(request.getContextPath() + "/products");
    }
%>
