-- Read-only diagnostic. Run in the intended OnlineFruitShopping_Test database.
-- No tables, columns, constraints or production data are changed.
SELECT DB_NAME() AS current_database;

WITH required(table_name, column_name) AS (
    SELECT * FROM (VALUES
      ('users','user_id'),('users','status'),('users','full_name'),
      ('user_roles','user_id'),('user_roles','role_id'),('roles','role_id'),('roles','role_name'),
      ('shops','shop_id'),('shops','owner_id'),('shops','status'),('shops','shop_name'),('shops','pickup_address'),
      ('shop_delivery_staff','shop_id'),('shop_delivery_staff','shipper_id'),('shop_delivery_staff','status'),
      ('deliveries','delivery_id'),('deliveries','order_id'),('deliveries','shop_id'),('deliveries','shipper_id'),
      ('deliveries','delivery_status'),('deliveries','note'),('deliveries','pickup_time'),('deliveries','delivered_time'),
      ('orders','order_id'),('orders','shop_id'),('orders','user_id'),('orders','order_status'),
      ('orders','payment_method'),('orders','payment_status'),('orders','total_amount'),
      ('orders','receiver_name'),('orders','receiver_phone'),('orders','shipping_address'),('orders','note'),
      ('orders','voucher_id'),('orders','coupon_id'),('orders','coupon_subtotal'),('orders','coupon_discount'),
      ('order_items','order_item_id'),('order_items','order_id'),('order_items','variant_id'),('order_items','price'),('order_items','quantity'),
      ('products','product_id'),('products','shop_id'),('products','product_name'),('products','status'),
      ('product_variants','variant_id'),('product_variants','product_id'),('product_variants','sku'),('product_variants','price'),
      ('notifications','notification_id'),('notifications','user_id'),('notifications','title'),
      ('notifications','message'),('notifications','is_read'),('notifications','created_at'),
      ('coupons','coupon_id'),('coupons','shop_id'),('coupons','code'),('coupons','coupon_name'),('coupons','description'),
      ('coupons','discount_type'),('coupons','discount_value'),('coupons','max_discount'),('coupons','min_order_amount'),
      ('coupons','total_limit'),('coupons','per_customer_limit'),('coupons','starts_at'),('coupons','ends_at'),
      ('coupons','is_active'),('coupons','is_deleted'),('coupons','version'),('coupons','created_at'),('coupons','updated_at'),
      ('coupon_usages','usage_id'),('coupon_usages','coupon_id'),('coupon_usages','shop_id'),('coupon_usages','order_id'),
      ('coupon_usages','customer_id'),('coupon_usages','code_snapshot'),('coupon_usages','subtotal'),
      ('coupon_usages','discount_amount'),('coupon_usages','final_amount'),('coupon_usages','used_at'),
      ('coupon_usages','usage_status'),('coupon_usages','removed_at'),
      ('promotions','promotion_id'),('promotions','shop_id'),('promotions','promotion_name'),('promotions','description'),
      ('promotions','discount_type'),('promotions','discount_value'),('promotions','starts_at'),('promotions','ends_at'),
      ('promotions','is_active'),('promotions','is_deleted'),('promotions','version'),
      ('promotions','created_at'),('promotions','updated_at'),
      ('promotion_products','promotion_id'),('promotion_products','product_id')
    ) AS columns_to_check(table_name, column_name)
)
SELECT table_name, column_name AS missing_column
FROM required
WHERE COL_LENGTH('dbo.' + table_name, column_name) IS NULL
ORDER BY table_name, column_name;
-- Empty result means only that these columns exist. It does not validate types,
-- constraints, triggers, indexes, fixtures, pricing rules or concurrent operations.
