IF DB_NAME() <> N'OnlineFruitShopping_Test' THROW 51000, N'Test database required', 1;
IF EXISTS (SELECT 1 FROM users) THROW 51001, N'Fixture requires empty users', 1;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
INSERT roles(role_name) VALUES(N'Admin'),(N'Customer'),(N'ShopOwner'),(N'DeliveryStaff');
DECLARE @fixture TABLE(email varchar(254), full_name nvarchar(100), role_name nvarchar(50));
INSERT @fixture VALUES
('admin@fruitshop.test',N'Fixture Admin',N'Admin'),
('customer.a@fruitshop.test',N'Customer A',N'Customer'),
('customer.b@fruitshop.test',N'Customer B',N'Customer'),
('owner.a@fruitshop.test',N'Owner A',N'ShopOwner'),
('owner.b@fruitshop.test',N'Owner B',N'ShopOwner'),
('owner.new@fruitshop.test',N'Owner New',N'ShopOwner'),
('delivery.a@fruitshop.test',N'Delivery A',N'DeliveryStaff'),
('delivery.b@fruitshop.test',N'Delivery B',N'DeliveryStaff');
INSERT users(full_name,email,password_hash,status)
SELECT full_name,email,@FixtureHash,'active' FROM @fixture;
INSERT user_roles(user_id,role_id)
SELECT u.user_id,r.role_id FROM @fixture f JOIN users u ON u.email=f.email
JOIN roles r ON UPPER(REPLACE(REPLACE(r.role_name,'_',''),' ',''))=UPPER(REPLACE(REPLACE(f.role_name,'_',''),' ',''));
INSERT shops(owner_id,shop_name,pickup_address,status)
SELECT user_id,CASE email WHEN 'owner.a@fruitshop.test' THEN N'Fixture Shop A' ELSE N'Fixture Shop B' END,N'Test pickup','active'
FROM users WHERE email IN ('owner.a@fruitshop.test','owner.b@fruitshop.test');
INSERT categories(category_name) VALUES(N'Fixture Fruit');
INSERT products(shop_id,category_id,product_name,status)
SELECT shop_id,(SELECT category_id FROM categories WHERE category_name=N'Fixture Fruit'),shop_name+N' Apple','published' FROM shops;
INSERT product_variants(product_id,sku,unit,weight_value,packaging_type,price)
SELECT product_id,'FIXTURE-'+CAST(product_id AS varchar(10)),N'kg',1,N'Box',100 FROM products;
INSERT inventory_batches(variant_id,batch_code,quantity,expiry_date)
SELECT variant_id,'FIXTURE-BATCH-'+CAST(variant_id AS varchar(10)),100,DATEADD(day,30,GETDATE()) FROM product_variants;
INSERT shop_delivery_staff(shop_id,shipper_id,status)
SELECT s.shop_id,u.user_id,N'Active' FROM shops s JOIN users u
ON (s.shop_name=N'Fixture Shop A' AND u.email='delivery.a@fruitshop.test') OR (s.shop_name=N'Fixture Shop B' AND u.email='delivery.b@fruitshop.test');
UPDATE shop_delivery_staff SET status=N'Inactive' WHERE shipper_id=(SELECT user_id FROM users WHERE email='delivery.b@fruitshop.test');
INSERT orders(shop_id,user_id,total_amount,receiver_name,receiver_phone,shipping_address,order_status,payment_method,payment_status)
SELECT s.shop_id,u.user_id,200,u.full_name,'0000000000',N'Test address',N'Confirmed',N'COD',N'Unpaid'
FROM shops s JOIN users u ON (s.shop_name=N'Fixture Shop A' AND u.email='customer.a@fruitshop.test') OR (s.shop_name=N'Fixture Shop B' AND u.email='customer.b@fruitshop.test');
INSERT order_items(order_id,variant_id,price,quantity)
SELECT o.order_id,v.variant_id,100,2 FROM orders o JOIN products p ON p.shop_id=o.shop_id JOIN product_variants v ON v.product_id=p.product_id;
INSERT deliveries(shop_id,order_id,shipper_id,delivery_status)
SELECT o.shop_id,o.order_id,staff.shipper_id,N'Assigned' FROM orders o JOIN shop_delivery_staff staff ON staff.shop_id=o.shop_id;
COMMIT;
