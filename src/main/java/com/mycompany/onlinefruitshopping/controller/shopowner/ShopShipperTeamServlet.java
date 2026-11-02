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

    private final DeliveryService service = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        User owner = WebUtil.user(req);

        try {
            req.setAttribute(
                "requests",
                service.getTeamRequests(owner)
            );

            req.setAttribute(
                "staff",
                service.getTeamMembers(owner)
            );

            req.setAttribute(
                "ownerShop",
                service.getOwnerShop(owner)
            );

            req.getRequestDispatcher(
                "/jsp/shopowner/views/shipper-team.jsp"
            ).forward(req, resp);

        } catch (IllegalArgumentException e) {

            log("Lỗi quyền truy cập đội shipper", e);
            resp.sendError(403);

        } catch (SQLException e) {

            log("Lỗi SQL khi tải đội shipper", e);
            resp.sendError(503);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws IOException {

        User owner = WebUtil.user(req);

        String action = req.getParameter("action");

        try {

            switch (action == null ? "" : action) {

                case "savePickupAddress" ->

                    service.updateShopPickupAddress(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("shopId")
                        ),
                        req.getParameter("pickupAddress")
                    );

                case "approve" ->

                    service.reviewTeamRequest(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("requestId")
                        ),
                        true,
                        ""
                    );

                case "reject" ->

                    service.reviewTeamRequest(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("requestId")
                        ),
                        false,
                        req.getParameter("reason")
                    );

                case "pause" ->

                    service.setTeamAssignmentEnabled(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("shipperId")
                        ),
                        false
                    );

                case "resume" ->

                    service.setTeamAssignmentEnabled(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("shipperId")
                        ),
                        true
                    );

                case "terminate" ->

                    service.terminateTeamMember(
                        owner,
                        WebUtil.positiveId(
                            req.getParameter("shipperId")
                        )
                    );

                default -> throw new IllegalArgumentException(
                    "Thao tác không hợp lệ."
                );
            }

            WebUtil.flash(
                req,
                "success",
                "Đã cập nhật đội shipper."
            );

        } catch (IllegalArgumentException e) {

            // Lỗi nghiệp vụ: hiển thị thông báo thực tế
            log("Lỗi nghiệp vụ shipper team: " + action, e);

            WebUtil.flash(
                req,
                "error",
                e.getMessage()
            );

        } catch (SQLException e) {

            // Lỗi database: ghi lại nguyên nhân thực tế
            logSqlError(action, e);

            WebUtil.flash(
                req,
                "error",
                "Lỗi cơ sở dữ liệu khi xử lý yêu cầu. " +
                "Vui lòng kiểm tra log server."
            );

        } catch (Exception e) {

            // Lỗi bất ngờ
            log("Lỗi không xác định khi xử lý: " + action, e);

            WebUtil.flash(
                req,
                "error",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại."
            );
        }

        resp.sendRedirect(
            req.getContextPath() +
            "/shopowner/shop/shipper-team"
        );
    }

    private void logSqlError(String action, SQLException e) {

        log("SQL ERROR - Action: " + action, e);

        SQLException next = e.getNextException();

        while (next != null) {

            log(
                "SQL NEXT ERROR - State: " +
                next.getSQLState() +
                " - Code: " +
                next.getErrorCode() +
                " - Message: " +
                next.getMessage()
            );

            next = next.getNextException();
        }
    }
}
