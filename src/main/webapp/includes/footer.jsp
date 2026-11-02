<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<footer class="footer" style="background: #0f172a; color: #94a3b8; padding: 48px 6% 24px; margin-top: 60px; font-size: 14px;">
    <div style="max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 36px; padding-bottom: 36px; border-bottom: 1px solid #1e293b;">
        <!-- Brand Col -->
        <div>
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 14px;">
                <svg viewBox="0 0 36 36" width="28" height="28" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 6C18 3.5 19 1.5 20 1" stroke="#15803d" stroke-width="2" stroke-linecap="round"/>
                    <path d="M18 4C21.5 2 24.5 3.5 23.5 6C21 6.5 19 5.5 18 4Z" fill="#22c55e"/>
                    <path d="M18 7C14 7 11 10.5 11 15C11 20 8 22 8 26C8 30 12 33 18 33C24 33 28 30 28 26C28 22 25 20 25 15C25 10.5 22 7 18 7Z" fill="#16a34a"/>
                </svg>
                <span style="font-weight: 800; font-size: 20px; color: #ffffff;">FreshFruit</span>
            </div>
            <p style="line-height: 1.6; margin: 0 0 16px; color: #94a3b8;">
                Nền tảng mua sắm và đăng ký hoa quả sạch trực tuyến hàng đầu. Cam kết 100% tươi ngon, có nguồn gốc kiểm định rõ ràng và giao hàng nhanh chóng.
            </p>
            <div style="color: #cbd5e1; font-size: 13px;">
                Hotline hỗ trợ: <strong style="color: #22c55e;">1900 8888</strong> (8:00 - 21:00)
            </div>
        </div>

        <!-- Quick Links -->
        <div>
            <h4 style="color: #ffffff; font-size: 15px; font-weight: 700; margin: 0 0 16px; text-transform: uppercase; letter-spacing: 0.5px;">Khám Phá Cửa Hàng</h4>
            <ul style="list-style: none; padding: 0; margin: 0; line-height: 2;">
                <li><a href="${pageContext.request.contextPath}/products" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Tất cả trái cây tươi</a></li>
                <li><a href="${pageContext.request.contextPath}/subscriptions" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Gói giao hoa quả định kỳ</a></li>
                <li><a href="${pageContext.request.contextPath}/cart" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Giỏ hàng mua sắm</a></li>
                <li><a href="${pageContext.request.contextPath}/orders" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Tra cứu & Theo dõi đơn hàng</a></li>
            </ul>
        </div>

        <!-- Support & Policies -->
        <div>
            <h4 style="color: #ffffff; font-size: 15px; font-weight: 700; margin: 0 0 16px; text-transform: uppercase; letter-spacing: 0.5px;">Chính Sách & Hỗ Trợ</h4>
            <ul style="list-style: none; padding: 0; margin: 0; line-height: 2;">
                <li><a href="${pageContext.request.contextPath}/payments" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Chính sách thanh toán trực tuyến</a></li>
                <li><a href="${pageContext.request.contextPath}/refunds" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Chính sách đổi trả & Hoàn tiền</a></li>
                <li><a href="${pageContext.request.contextPath}/checkout" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Giao hàng hỏa tốc 2 giờ</a></li>
                <li><a href="${pageContext.request.contextPath}/profile" style="color: #94a3b8; text-decoration: none; transition: color 0.2s;">Bảo mật thông tin khách hàng</a></li>
            </ul>
        </div>

        <!-- Payment & Certs -->
        <div>
            <h4 style="color: #ffffff; font-size: 15px; font-weight: 700; margin: 0 0 16px; text-transform: uppercase; letter-spacing: 0.5px;">Cổng Thanh Toán Hỗ Trợ</h4>
            <p style="line-height: 1.6; margin: 0 0 12px; color: #94a3b8;">
                Hỗ trợ thanh toán bảo mật qua VNPay Sandbox, thẻ ATM hơn 34 ngân hàng, thẻ quốc tế và chuyển khoản VietQR tự động 24/7.
            </p>
            <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px;">
                <span style="background: #1e293b; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #e2e8f0;">VNPay</span>
                <span style="background: #1e293b; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #e2e8f0;">VietQR</span>
                <span style="background: #1e293b; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #e2e8f0;">Thẻ ATM</span>
                <span style="background: #1e293b; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #e2e8f0;">Tiền mặt (COD)</span>
            </div>
        </div>
    </div>

    <div style="max-width: 1200px; margin: 20px auto 0; text-align: center; font-size: 13px; color: #64748b;">
        © 2026 FreshFruit Online Shopping Platform - SWP391 SE2063 Nhóm 4. All rights reserved.
    </div>
</footer>
