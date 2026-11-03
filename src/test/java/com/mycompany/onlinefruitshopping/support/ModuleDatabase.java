package com.mycompany.onlinefruitshopping.support;
import java.sql.*;
import java.util.UUID;
/** H2 fixture built from the uploaded merged schema plus module tables; does not emulate SQL Server locks/indexes. */
public final class ModuleDatabase {
 public final String url="jdbc:h2:mem:modules_"+UUID.randomUUID()+";MODE=MSSQLServer;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS dbo\\;SET SCHEMA dbo";
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
  exec("CREATE ALIAS IF NOT EXISTS SYSDATETIME FOR 'com.mycompany.onlinefruitshopping.support.ModuleDatabase.now'");
  exec("CREATE DOMAIN sqlserver_bit AS TINYINT CHECK (VALUE IN(0,1))");
  installTables("database/reference/OnlineFruitShopping_DB_FINAL_MERGED.sql");
  installTables("database/module_delivery_extensions.sql");
  exec(
   "INSERT INTO users(user_id,full_name,email,password_hash,status,phone) VALUES(10,'Owner','owner@test.invalid','test','active','010'),(11,'Other owner','owner2@test.invalid','test','active','011'),(20,'Shipper','shipper@test.invalid','test','active','020'),(21,'Other shipper','shipper2@test.invalid','test','active','021'),(30,'Customer','customer@test.invalid','test','active','030'),(31,'Other customer','customer2@test.invalid','test','active','031')",
   "INSERT INTO roles(role_id,role_name) VALUES(1,'ShopOwner'),(2,'DeliveryStaff'),(3,'Customer')",
   "INSERT INTO user_roles VALUES(10,1),(11,1),(20,2),(21,2),(30,3),(31,3)",
   "INSERT INTO shops(shop_id,owner_id,shop_name,status) VALUES(1,10,'Shop A','active'),(2,11,'Shop B','active')",
   "INSERT INTO user_addresses(address_id,user_id,recipient_name,phone,address_line,ward,district,province) VALUES(1,30,'Customer','0900000000','1 Test street','Ward','District','City')",
   "INSERT INTO shop_delivery_staff(shop_id,shipper_id,status) VALUES(1,20,'Active'),(1,21,'Active')",
   "INSERT INTO shop_delivery_settings(shop_id,shipper_id) SELECT shop_id,shipper_id FROM shop_delivery_staff",
   "INSERT INTO vouchers(voucher_id,code,discount_type,discount_value,start_date,end_date,usage_limit) VALUES(1,'LEGACY','percent',10,DATEADD(DAY,-1,CURRENT_TIMESTAMP),DATEADD(DAY,1,CURRENT_TIMESTAMP),10)",
   "INSERT INTO orders(order_id,shop_id,user_id,address_id,voucher_id,status,total_amount) VALUES(100,1,30,1,NULL,'shipping',100000),(101,1,30,1,NULL,'preparing',100000),(102,2,30,1,NULL,'preparing',100000),(103,1,30,1,NULL,'pending_confirmation',75000)",
   "INSERT INTO payments(order_id,method,status,amount) VALUES(100,'cod','pending',100000),(101,'cod','pending',100000),(102,'cod','pending',100000),(103,'cod','pending',75000)",
   "INSERT INTO deliveries(order_id,staff_id,shipper_id,shop_id,status,delivery_status) VALUES(100,20,20,1,'delivering','Delivering')",
   "INSERT INTO delivery_attempts(delivery_id,shipper_id,assigned_by,delivery_status,pickup_time,receiver_name,receiver_phone,shipping_address,pickup_address) VALUES(1,20,10,'Delivering',CURRENT_TIMESTAMP,'Customer','0900000000','Snapshot address','Shop pickup')",
   "INSERT INTO categories(category_id,category_name) VALUES(1,'Fruit')",
   "INSERT INTO products(product_id,shop_id,category_id,product_name,status,hidden_by_admin) VALUES(1,1,1,'Orange','published',0),(2,2,1,'Apple','published',0),(3,1,1,'Hidden','published',1)",
   "INSERT INTO product_variants(variant_id,product_id,sku,price,unit) VALUES(1,1,'ORANGE',100000,'piece'),(2,2,'APPLE',100000,'piece'),(3,3,'HIDDEN',100000,'piece')",
   "INSERT INTO order_items(order_id,variant_id,quantity,unit_price) VALUES(100,1,1,100000),(101,1,1,100000),(103,1,1,50000)");
 }
 /** Load actual CREATE TABLE statements, preserving columns, FKs, CHECKs and defaults.
  * Only bracket/identity/named-default/MAX/bit syntax is translated for H2. SQL Server batch,
  * filtered indexes and locking semantics require a separate SQL Server run. */
 private void installTables(String file) throws SQLException {
  try {
   String script=java.nio.file.Files.readString(java.nio.file.Path.of(file));
   var matcher=java.util.regex.Pattern.compile("(?is)CREATE TABLE dbo[.]\\[?(\\w+)\\]?\\s*\\((.*?)\\n\\s*\\);").matcher(script);
   int tables=0;
   while(matcher.find()) {
    String ddl=matcher.group().replace("[", "").replace("]", "").replaceAll("(?i)IDENTITY\\(1,1\\)","GENERATED BY DEFAULT AS IDENTITY")
       .replaceAll("(?i)CONSTRAINT \\w+ DEFAULT","DEFAULT")
       .replaceAll("(?i)\\bbit\\b","sqlserver_bit")
       .replaceAll("(?i)nvarchar\\(MAX\\)","nvarchar(1000000)");
    exec(ddl);tables++;
   }
   if(tables==0) throw new SQLException("No CREATE TABLE statements in "+file);
  } catch(java.io.IOException e) {throw new SQLException("Cannot read merged schema",e);}
 }
 public static Timestamp now(){return new Timestamp(System.currentTimeMillis());}
}
