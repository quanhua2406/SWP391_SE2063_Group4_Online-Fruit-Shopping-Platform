package com.mycompany.onlinefruitshopping.controller.shopowner;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.delivery.DeliveryService;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/shopowner/shop/shipper-team")
public class ShopShipperTeamServlet extends HttpServlet {
    private final DeliveryService service=new DeliveryService();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        User owner=WebUtil.user(req);
        try {
            req.setAttribute("requests",service.getTeamRequests(owner));
            req.setAttribute("staff",service.getTeamMembers(owner));
            req.setAttribute("ownerShop",service.getOwnerShop(owner));
            req.getRequestDispatcher("/jsp/shopowner/views/shipper-team.jsp").forward(req,resp);
        } catch(IllegalArgumentException e) { resp.sendError(403); }
          catch(SQLException e) { log("Không tải được đội shipper",e); resp.sendError(503); }
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        User owner=WebUtil.user(req); String action=req.getParameter("action");
        try {
            switch(action==null?"":action) {
                case "savePickupAddress" -> service.updateShopPickupAddress(owner,WebUtil.positiveId(req.getParameter("shopId")),req.getParameter("pickupAddress"));
                case "approve" -> service.reviewTeamRequest(owner,WebUtil.positiveId(req.getParameter("requestId")),true,"");
                case "reject" -> service.reviewTeamRequest(owner,WebUtil.positiveId(req.getParameter("requestId")),false,req.getParameter("reason"));
                case "pause" -> service.setTeamAssignmentEnabled(owner,WebUtil.positiveId(req.getParameter("shipperId")),false);
                case "resume" -> service.setTeamAssignmentEnabled(owner,WebUtil.positiveId(req.getParameter("shipperId")),true);
                case "terminate" -> service.terminateTeamMember(owner,WebUtil.positiveId(req.getParameter("shipperId")));
                default -> throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            WebUtil.flash(req,"success","Đã cập nhật đội shipper.");
        } catch(IllegalArgumentException e) { WebUtil.flash(req,"error",e.getMessage()); }
          catch(SQLException e) { log("Lỗi quản lý đội shipper",e); WebUtil.flash(req,"error","Không thể xử lý thao tác. Vui lòng thử lại."); }
        resp.sendRedirect(req.getContextPath()+"/shopowner/shop/shipper-team");
    }
}
