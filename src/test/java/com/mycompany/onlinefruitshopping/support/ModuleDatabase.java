package com.mycompany.onlinefruitshopping.support;
import java.sql.*;
import java.util.UUID;
/** H2 contract fixture: canonical group columns only; does not emulate SQL Server locks/indexes. */
public final class ModuleDatabase {
 public final String url="jdbc:h2:mem:modules_"+UUID.randomUUID()+";MODE=MSSQLServer;DB_CLOSE_DELAY=-1";
 public Connection open() throws SQLException {
  Connection real=DriverManager.getConnection(url);
  return (Connection) java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy,method,args)->{
   if(method.getName().equals("prepareStatement")) recordSql((String)args[0]);
   try{return method.invoke(real,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
  });
 }
 private static synchronized void recordSql(String sql) throws java.io.IOException {
  // Parse-only artifact: named placeholders have no values; never execute this file.
  java.nio.file.Files.writeString(java.nio.file.Path.of("target/module-prepared-sql.sql"),
      sql.replace("?","@p")+";\nGO\n",java.nio.file.StandardOpenOption.CREATE,java.nio.file.StandardOpenOption.APPEND);
 }
 public void exec(String... queries) throws SQLException {try(Connection c=open();Statement s=c.createStatement()){for(String q:queries)s.execute(q);}}
 public String scalar(String q) throws SQLException {try(Connection c=open();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){if(!r.next())throw new SQLException("No row");return r.getString(1);}}
 public int count(String q) throws SQLException {return Integer.parseInt(scalar(q));}
 public ModuleDatabase() throws SQLException {
  exec(
   "CREATE ALIAS IF NOT EXISTS SYSDATETIME FOR 'com.mycompany.onlinefruitshopping.support.ModuleDatabase.now'",
   "CREATE TABLE users(user_id INT PRIMARY KEY,full_name NVARCHAR(100),status VARCHAR(20))",
   "CREATE TABLE roles(role_id INT PRIMARY KEY,role_name VARCHAR(30))",
   "CREATE TABLE user_roles(user_id INT,role_id INT,PRIMARY KEY(user_id,role_id))",
   "CREATE TABLE shops(shop_id INT PRIMARY KEY,owner_id INT,shop_name NVARCHAR(100),status VARCHAR(20))",
   "CREATE TABLE user_addresses(address_id INT PRIMARY KEY,user_id INT,recipient_name NVARCHAR(100),phone VARCHAR(20),address_line NVARCHAR(255),ward NVARCHAR(100),district NVARCHAR(100),province NVARCHAR(100))",
   "CREATE TABLE shop_delivery_staff(shop_id INT,shipper_id INT,status VARCHAR(20),PRIMARY KEY(shop_id,shipper_id))",
   "CREATE TABLE orders(order_id INT PRIMARY KEY,shop_id INT,user_id INT,address_id INT,voucher_id INT,status VARCHAR(30),total_amount DECIMAL(12,2))",
   "CREATE TABLE payments(payment_id INT IDENTITY PRIMARY KEY,order_id INT UNIQUE,method VARCHAR(20),status VARCHAR(20),amount DECIMAL(12,2),transaction_ref VARCHAR(255),paid_at TIMESTAMP)",
   "CREATE TABLE deliveries(delivery_id INT IDENTITY PRIMARY KEY,order_id INT UNIQUE,staff_id INT,status VARCHAR(30),note NVARCHAR(255),proof_image_url NVARCHAR(255))",
   "CREATE TABLE delivery_attempts(attempt_id INT IDENTITY PRIMARY KEY,delivery_id INT REFERENCES deliveries(delivery_id),shipper_id INT,assigned_by INT,delivery_status VARCHAR(30) DEFAULT 'Assigned',assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,accepted_at TIMESTAMP,pickup_time TIMESTAMP,delivered_time TIMESTAMP,closed_at TIMESTAMP,receiver_name NVARCHAR(100),receiver_phone VARCHAR(20),shipping_address NVARCHAR(1000),pickup_address NVARCHAR(1000),note NVARCHAR(255),proof_image_url NVARCHAR(255),version INT DEFAULT 0, CHECK((delivery_status IN('Delivered','Failed','Rejected') AND closed_at IS NOT NULL) OR (delivery_status IN('Assigned','Accepted','PickedUp','Delivering') AND closed_at IS NULL)), CHECK(delivery_status <> 'Delivered' OR delivered_time IS NOT NULL))",
   "CREATE TABLE notifications(notification_id INT IDENTITY PRIMARY KEY,user_id INT,title NVARCHAR(200),content NVARCHAR(500),is_read BIT,read_at TIMESTAMP,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
   "CREATE TABLE order_status_history(history_id INT IDENTITY PRIMARY KEY,order_id INT,status VARCHAR(50),changed_by INT,changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
   "CREATE TABLE products(product_id INT PRIMARY KEY,shop_id INT,product_name NVARCHAR(255),status VARCHAR(30),hidden_by_admin BIT DEFAULT 0)",
   "CREATE TABLE product_variants(variant_id INT PRIMARY KEY,product_id INT,sku VARCHAR(100),price DECIMAL(12,2))",
   "CREATE TABLE order_items(order_item_id INT IDENTITY PRIMARY KEY,order_id INT,variant_id INT,quantity DECIMAL(10,2),unit_price DECIMAL(12,2))",
   "CREATE TABLE coupons(coupon_id INT IDENTITY PRIMARY KEY,shop_id INT,code VARCHAR(30),coupon_name NVARCHAR(150),description NVARCHAR(500),discount_type VARCHAR(10),discount_value DECIMAL(18,2),max_discount DECIMAL(18,2),min_order_amount DECIMAL(18,2),total_limit INT,per_customer_limit INT,starts_at TIMESTAMP,ends_at TIMESTAMP,is_active BIT DEFAULT 1,is_deleted BIT DEFAULT 0,version INT DEFAULT 0,created_at TIMESTAMP,updated_at TIMESTAMP,UNIQUE(shop_id,code))",
   "CREATE TABLE coupon_usages(usage_id INT IDENTITY PRIMARY KEY,coupon_id INT REFERENCES coupons(coupon_id),shop_id INT,order_id INT REFERENCES orders(order_id),customer_id INT,code_snapshot VARCHAR(30),subtotal DECIMAL(18,2),discount_amount DECIMAL(18,2),final_amount DECIMAL(18,2),used_at TIMESTAMP,usage_status VARCHAR(20),removed_at TIMESTAMP,CHECK(final_amount=subtotal-discount_amount),CHECK((usage_status='Applied' AND removed_at IS NULL) OR (usage_status='Removed' AND removed_at IS NOT NULL)))",
   "CREATE TABLE promotions(promotion_id INT IDENTITY PRIMARY KEY,shop_id INT,promotion_name NVARCHAR(150),description NVARCHAR(500),discount_type VARCHAR(10),discount_value DECIMAL(18,2),starts_at TIMESTAMP,ends_at TIMESTAMP,is_active BIT DEFAULT 1,is_deleted BIT DEFAULT 0,version INT DEFAULT 0,created_at TIMESTAMP,updated_at TIMESTAMP)",
   "CREATE TABLE promotion_products(promotion_id INT REFERENCES promotions(promotion_id),product_id INT REFERENCES products(product_id),PRIMARY KEY(promotion_id,product_id))",
   "INSERT INTO users VALUES(10,'Owner','active'),(11,'Other owner','active'),(20,'Shipper','active'),(21,'Other shipper','active'),(30,'Customer','active'),(31,'Other customer','active')",
   "INSERT INTO roles VALUES(1,'ShopOwner'),(2,'DeliveryStaff'),(3,'Customer')",
   "INSERT INTO user_roles VALUES(10,1),(11,1),(20,2),(21,2),(30,3),(31,3)",
   "INSERT INTO shops VALUES(1,10,'Shop A','active'),(2,11,'Shop B','active')",
   "INSERT INTO user_addresses VALUES(1,30,'Customer','0900000000','1 Test street','Ward','District','City')",
   "INSERT INTO shop_delivery_staff VALUES(1,20,'Active'),(1,21,'Active'),(2,21,'Active')",
   "INSERT INTO orders VALUES(100,1,30,1,NULL,'shipping',100000),(101,1,30,1,NULL,'preparing',100000),(102,2,30,1,NULL,'preparing',100000),(103,1,30,1,NULL,'pending_confirmation',75000)",
   "INSERT INTO payments(order_id,method,status,amount) VALUES(100,'cod','pending',100000),(101,'cod','pending',100000),(102,'cod','pending',100000),(103,'cod','pending',75000)",
   "INSERT INTO deliveries(order_id,staff_id,status) VALUES(100,20,'delivering')",
   "INSERT INTO delivery_attempts(delivery_id,shipper_id,assigned_by,delivery_status,pickup_time,receiver_name,receiver_phone,shipping_address,pickup_address) VALUES(1,20,10,'Delivering',CURRENT_TIMESTAMP,'Customer','0900000000','Snapshot address','Shop pickup')",
   "INSERT INTO products VALUES(1,1,'Orange','published',0),(2,2,'Apple','published',0),(3,1,'Hidden','published',1)",
   "INSERT INTO product_variants VALUES(1,1,'ORANGE',100000),(2,2,'APPLE',100000),(3,3,'HIDDEN',100000)",
   "INSERT INTO order_items(order_id,variant_id,quantity,unit_price) VALUES(100,1,1,100000),(101,1,1,100000),(103,1,0.5,100000)");
 }
 public static Timestamp now(){return new Timestamp(System.currentTimeMillis());}
}
