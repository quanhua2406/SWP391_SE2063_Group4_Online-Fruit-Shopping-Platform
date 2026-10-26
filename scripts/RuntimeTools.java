import com.mycompany.onlinefruitshopping.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.file.*;
import java.util.*;

/** Run with target/classes and the JDBC/BCrypt jars on the classpath. No secrets printed. */
class RuntimeTools {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("credentials")) {
            Path file = Path.of("config/test-accounts.properties");
            if (Files.exists(file)) throw new IllegalStateException("Credentials already exist");
            String password = UUID.randomUUID().toString() + "aA!9";
            Properties p = new Properties();
            for (String name : List.of("admin", "customer.a", "customer.b", "owner.a", "owner.b", "delivery.a", "delivery.b", "owner.new"))
                p.setProperty(name + "@fruitshop.test", password);
            p.setProperty("fixture.hash", BCrypt.hashpw(password, BCrypt.gensalt()));
            try (var writer = Files.newBufferedWriter(file)) { p.store(writer, "Local test credentials - never commit"); }
            System.out.println("Local fixture credentials created.");
        } else {
            try (var c = DBConnection.getConnection(); var s = c.createStatement();
                 var r = s.executeQuery("SELECT @@SERVERNAME, DB_NAME()")) {
                r.next();
                if (!r.getString(2).equals("OnlineFruitShopping_Test")) throw new IllegalStateException("Wrong database");
                System.out.println("server=" + r.getString(1) + "; database=" + r.getString(2));
            }
        }
    }
}
