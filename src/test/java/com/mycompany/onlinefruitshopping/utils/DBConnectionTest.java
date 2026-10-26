package com.mycompany.onlinefruitshopping.utils;

import java.nio.file.*;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {
    @TempDir Path directory;
    String previous;
    @BeforeEach void remember() { previous = System.getProperty("fruitshop.db.config"); }
    @AfterEach void restore() {
        if (previous == null) System.clearProperty("fruitshop.db.config");
        else System.setProperty("fruitshop.db.config", previous);
    }
    private void config(String content) throws Exception {
        Path file = directory.resolve("db.properties"); Files.writeString(file, content);
        System.setProperty("fruitshop.db.config", file.toString());
    }
    @Test void readsSpecifiedFileAndReportsMissingFile() throws Exception {
        config("db.url=jdbc:record:test\ndb.user=fixture\ndb.password=value\n");
        assertEquals("fixture", DBConnection.loadConfiguration().getProperty("db.user"));
        System.setProperty("fruitshop.db.config", directory.resolve("absent").toString());
        assertThrows(SQLException.class, DBConnection::getConnection);
    }
    @Test void missingPathIsExplicitWhenEnvironmentIsAbsent() {
        System.clearProperty("fruitshop.db.config");
        String env = System.getenv("FRUITSHOP_DB_CONFIG");
        if (env == null || env.isBlank()) {
            assertTrue(assertThrows(SQLException.class, DBConnection::loadConfiguration).getMessage().contains("FRUITSHOP_DB_CONFIG"));
        } else {
            // An environment-configured runner must still prefer an explicit JVM path.
            System.setProperty("fruitshop.db.config", directory.resolve("absent").toString());
            assertThrows(SQLException.class, DBConnection::loadConfiguration);
        }
    }
    @Test void everyRequiredKeyIsValidatedBeforeConnecting() throws Exception {
        for (String key : new String[]{"db.url", "db.user", "db.password"}) {
            Properties p = new Properties(); p.setProperty("db.url", "jdbc:record:test");
            p.setProperty("db.user", "fixture"); p.setProperty("db.password", "value"); p.remove(key);
            Path file = directory.resolve("db.properties");
            try (var out = Files.newBufferedWriter(file)) { p.store(out, null); }
            System.setProperty("fruitshop.db.config", file.toString());
            assertTrue(assertThrows(SQLException.class, DBConnection::getConnection).getMessage().contains(key));
        }
    }
    @Test void passwordIsUntrimmedAndConnectionFailureHasNoFallback() throws Exception {
        config("db.url=jdbc:record:test\ndb.user= fixture \ndb.password=\\ secret \n");
        int[] attempts = {0};
        Driver driver = new Driver() {
            public Connection connect(String url, Properties p) throws SQLException {
                if (!acceptsURL(url)) return null;
                attempts[0]++; assertEquals("fixture", p.getProperty("user"));
                assertEquals(" secret ", p.getProperty("password")); throw new SQLException("recorded failure");
            }
            public boolean acceptsURL(String url) { return url.startsWith("jdbc:record:"); }
            public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(driver);
        try {
            assertEquals("recorded failure", assertThrows(SQLException.class, DBConnection::getConnection).getMessage());
            assertEquals(1, attempts[0]);
        } finally { DriverManager.deregisterDriver(driver); }
    }
}
