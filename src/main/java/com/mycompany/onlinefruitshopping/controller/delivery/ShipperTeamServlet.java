package com.mycompany.onlinefruitshopping.controller.delivery;

import com.mycompany.onlinefruitshopping.model.User;
import com.mycompany.onlinefruitshopping.service.delivery.DeliveryService;
import com.mycompany.onlinefruitshopping.utils.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns={"/delivery/team","/delivery/team/apply","/delivery/team/withdraw","/delivery/team/availability","/delivery/team/leave"})
public class ShipperTeamServlet extends HttpServlet {
    private final DeliveryService service=new DeliveryService();

    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
        User shipper=WebUtil.user(req);
        try {
            req.setAttribute("shops",service.findActiveShops(shipper));
            req.setAttribute("membership",service.getMyMembership(shipper));
            req.setAttribute("myRequests",service.getMyRequests(shipper));
            req.getRequestDispatcher("/jsp/delivery/shipper-team.jsp").forward(req,resp);
        } catch(IllegalArgumentException e) { resp.sendError(403); }
          catch(SQLException e) { log("Không tải được thông tin đội shipper",e); resp.sendError(503); }
    }

    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        User shipper=WebUtil.user(req); String path=req.getServletPath();
        try {
            switch(path) {
                case "/delivery/team/apply" -> service.applyToShop(shipper,WebUtil.positiveId(req.getParameter("shopId")));
                case "/delivery/team/withdraw" -> service.withdrawRequest(shipper,WebUtil.positiveId(req.getParameter("requestId")));
                case "/delivery/team/availability" -> service.setAvailability(shipper,"true".equals(req.getParameter("available")));
                case "/delivery/team/leave" -> service.requestLeave(shipper);
                default -> { resp.sendError(404); return; }
            }
            WebUtil.flash(req,"success","Đã cập nhật yêu cầu đội giao hàng.");
        } catch(IllegalArgumentException e) { WebUtil.flash(req,"error",e.getMessage()); }
          catch(SQLException e) { log("Lỗi cập nhật đội shipper",e); WebUtil.flash(req,"error","Không thể thực hiện thao tác. Vui lòng thử lại."); }
        resp.sendRedirect(req.getContextPath()+"/delivery/team");
    }
}
