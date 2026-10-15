-- =============================================================================
-- SEED DATA: 1 ADMIN & 1 SHOP OWNER (Online Fruit Shopping Platform)
-- Mật khẩu mặc định cho cả 2 tài khoản: 123456
-- Chuẩn mã hóa: BCrypt (cost factor 12)
-- Hash BCrypt của '123456': $2a$12$SUJehGj7YoI12Z7FdHofvOzc4VMJ8ZBDM6n6aaruARFSGJX3rrhLW
-- =============================================================================

USE OnlineFruitShopping;
GO

BEGIN TRANSACTION;

-- 1. THÊM TÀI KHOẢN ADMIN
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com')
BEGIN
    INSERT INTO users (full_name, email, phone, password_hash, status, is_member, created_at)
    VALUES (
        N'Quản Trị Viên',
        'admin@gmail.com',
        '0901234567',
        '$2a$12$SUJehGj7YoI12Z7FdHofvOzc4VMJ8ZBDM6n6aaruARFSGJX3rrhLW',
        'active',
        0,
        GETDATE()
    );
END
ELSE
BEGIN
    UPDATE users 
    SET password_hash = '$2a$12$SUJehGj7YoI12Z7FdHofvOzc4VMJ8ZBDM6n6aaruARFSGJX3rrhLW',
        status = 'active'
    WHERE email = 'admin@gmail.com';
END;

-- Gán quyền Admin (role_id = 4)
DECLARE @adminId INT = (SELECT user_id FROM users WHERE email = 'admin@gmail.com');
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @adminId AND role_id = 4)
BEGIN
    INSERT INTO user_roles (user_id, role_id)
    VALUES (@adminId, 4);
END;


-- 2. THÊM TÀI KHOẢN SHOP OWNER
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'shopowner@gmail.com')
BEGIN
    INSERT INTO users (full_name, email, phone, password_hash, status, is_member, created_at)
    VALUES (
        N'Chủ Shop Hoàng Long',
        'shopowner@gmail.com',
        '0987654321',
        '$2a$12$SUJehGj7YoI12Z7FdHofvOzc4VMJ8ZBDM6n6aaruARFSGJX3rrhLW',
        'active',
        0,
        GETDATE()
    );
END
ELSE
BEGIN
    UPDATE users 
    SET password_hash = '$2a$12$SUJehGj7YoI12Z7FdHofvOzc4VMJ8ZBDM6n6aaruARFSGJX3rrhLW',
        status = 'active'
    WHERE email = 'shopowner@gmail.com';
END;

-- Gán quyền ShopOwner (role_id = 2)
DECLARE @shopOwnerId INT = (SELECT user_id FROM users WHERE email = 'shopowner@gmail.com');
IF NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = @shopOwnerId AND role_id = 2)
BEGIN
    INSERT INTO user_roles (user_id, role_id)
    VALUES (@shopOwnerId, 2);
END;


-- 3. TẠO CỬA HÀNG CHO SHOP OWNER (Bảng shops)
IF NOT EXISTS (SELECT 1 FROM shops WHERE owner_id = @shopOwnerId)
BEGIN
    INSERT INTO shops (owner_id, shop_name, description, status, created_at)
    VALUES (
        @shopOwnerId,
        N'Trái Cây Tươi Hoàng Long',
        N'Cửa hàng chuyên cung cấp trái cây tươi organic nhập khẩu và nội địa, các combo định kỳ sạch đạt chuẩn VietGAP.',
        'active',
        GETDATE()
    );
END;

COMMIT TRANSACTION;
GO

-- Kiểm tra kết quả
SELECT u.user_id, u.full_name, u.email, u.phone, r.role_name, s.shop_name, u.status
FROM users u
JOIN user_roles ur ON u.user_id = ur.user_id
JOIN roles r ON ur.role_id = r.role_id
LEFT JOIN shops s ON u.user_id = s.owner_id
WHERE u.email IN ('admin@gmail.com', 'shopowner@gmail.com');
GO
