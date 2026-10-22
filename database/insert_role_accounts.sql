-- =============================================================
-- FreshFruit - Seed one account per role
-- =============================================================
-- Creates the four system roles (if missing) and one account
-- for each role. All accounts share the SAME password:
--
--     Tien123456
--
-- Accounts:
--   Admin          : he180293phamquangtien@gmail.com
--   Customer       : customer@freshfruit.com
--   Shop Owner     : shopowner@freshfruit.com
--   Delivery Staff : delivery@freshfruit.com
--
-- The password is stored as a BCrypt hash (jbcrypt, cost 12),
-- exactly like the application's registration/reset flow.
--
-- The script is idempotent: running it again does not create
-- duplicates (roles and users are matched by normalized name /
-- email, and existing role links are skipped).
-- =============================================================

SET NOCOUNT ON;

-- BCrypt hash of "Tien123456"
DECLARE @pw NVARCHAR(255) =
    N'$2a$12$uHOkSikpexQJHahuPHaQKOfaFI2kViFoJHw7niFr4NfeQnElWwORy';

-- -------------------------------------------------------------
-- 1) Make sure the four roles exist.
--    Matching ignores case, spaces and underscores, so it maps
--    to any existing variant (ADMIN, Shop Owner, SHOP_OWNER...).
-- -------------------------------------------------------------
INSERT INTO roles (role_name)
SELECT v.role_name
FROM (VALUES
        (N'Admin'),
        (N'Customer'),
        (N'ShopOwner'),
        (N'DeliveryStaff')
     ) AS v(role_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM roles r
    WHERE REPLACE(REPLACE(UPPER(r.role_name), ' ', ''), '_', '')
        = REPLACE(REPLACE(UPPER(v.role_name), ' ', ''), '_', '')
);

-- -------------------------------------------------------------
-- 2) The accounts to create
-- -------------------------------------------------------------
DECLARE @seed TABLE (
    full_name NVARCHAR(100),
    email     NVARCHAR(150),
    role_norm NVARCHAR(50)
);

INSERT INTO @seed (full_name, email, role_norm) VALUES
    (N'Pham Quang Tien',  N'he180293phamquangtien@gmail.com', 'ADMIN'),
    (N'Customer',         N'customer@freshfruit.com',          'CUSTOMER'),
    (N'Shop Owner',       N'shopowner@freshfruit.com',         'SHOPOWNER'),
    (N'Delivery Staff',   N'delivery@freshfruit.com',          'DELIVERYSTAFF');

-- -------------------------------------------------------------
-- 3) Insert the users (skipping any e-mail that already exists)
-- -------------------------------------------------------------
INSERT INTO users (full_name, email, password_hash, phone, status)
SELECT s.full_name, s.email, @pw, NULL, 'active'
FROM @seed s
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.email = s.email
);

-- -------------------------------------------------------------
-- 4) Assign the matching role to each user
-- -------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM @seed s
JOIN users u ON u.email = s.email
JOIN roles r
    ON REPLACE(REPLACE(UPPER(r.role_name), ' ', ''), '_', '') = s.role_norm
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.user_id
      AND ur.role_id = r.role_id
);

-- -------------------------------------------------------------
-- 5) Show the result
-- -------------------------------------------------------------
SELECT u.user_id,
       u.full_name,
       u.email,
       u.status,
       r.role_name
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.user_id
LEFT JOIN roles r ON r.role_id = ur.role_id
ORDER BY u.user_id;
GO
