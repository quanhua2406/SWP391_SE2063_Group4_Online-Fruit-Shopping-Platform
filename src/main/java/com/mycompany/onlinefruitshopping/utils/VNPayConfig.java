
package com.mycompany.onlinefruitshopping.utils;
import java.io.StringReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VNPayConfig {

    private VNPayConfig() {
    }

    public static final long AMOUNT = 200000L;

    public static final String PAYMENT_URL =
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    public static final String VERSION = "2.1.0";
    public static final String COMMAND = "pay";
    public static final String CURR_CODE = "VND";
    public static final String ORDER_TYPE = "other";

    private static final Path CONFIG_FILE =
            Path.of(
                    System.getProperty("user.home"),
                    ".onlinefruitshopping",
                    "vnpay.properties"
            );

    public static String getTmnCode() {
        return requiredConfig("VNPAY_TMN_CODE");
    }

    public static String getHashSecret() {
        return requiredConfig("VNPAY_HASH_SECRET");
    }

    public static String getReturnUrl() {
        return requiredConfig("VNPAY_RETURN_URL");
    }

    
private static String requiredConfig(String key) {

    // First try the environment variable.
    String value = System.getenv(key);

    if (value != null && !value.isBlank()) {
        return value.trim();
    }

    // Read the configuration file.
    if (!Files.isRegularFile(CONFIG_FILE)) {
        throw new IllegalStateException(
                "VNPay config file not found: " + CONFIG_FILE
        );
    }

    try {
        String content = Files.readString(
                CONFIG_FILE,
                StandardCharsets.UTF_8
        );

        // Remove UTF-8 BOM if present at the beginning.
        if (!content.isEmpty()
                && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }

        Properties properties = new Properties();

        try (StringReader reader = new StringReader(content)) {
            properties.load(reader);
        }

        value = properties.getProperty(key);

        if (value != null && !value.isBlank()) {
            return value.trim();
        }

        throw new IllegalStateException(
                "Missing key " + key
                + " in VNPay config file: " + CONFIG_FILE
        );

    } catch (IOException e) {
        throw new IllegalStateException(
                "Cannot read VNPay config file: " + CONFIG_FILE,
                e
        );
    }
}

}
