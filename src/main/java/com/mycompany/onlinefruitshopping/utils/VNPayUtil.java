
package com.mycompany.onlinefruitshopping.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class VNPayUtil {

    private VNPayUtil() {}

    public static String hmacSHA512(
            String key,
            String data) throws Exception {

        Mac hmac = Mac.getInstance("HmacSHA512");

        SecretKeySpec secretKey = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8),
            "HmacSHA512"
        );

        hmac.init(secretKey);

        byte[] bytes = hmac.doFinal(
            data.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }

    public static String encode(String value) {
        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        );
    }

    public static String buildHashData(
            SortedMap<String, String> params) {

        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, String> entry
                : params.entrySet()) {

            if (data.length() > 0) {
                data.append('&');
            }

            data.append(entry.getKey())
                .append('=')
                .append(encode(entry.getValue()));
        }

        return data.toString();
    }

    public static String buildQuery(
            SortedMap<String, String> params) {

        return buildHashData(params);
    }

    public static boolean verifySignature(
            Map<String, String[]> input,
            String secret) throws Exception {

        String receivedHash = first(input, "vnp_SecureHash");

        if (receivedHash == null
                || receivedHash.isEmpty()) {
            return false;
        }

        SortedMap<String, String> params =
            new TreeMap<>();

        for (Map.Entry<String, String[]> entry
                : input.entrySet()) {

            String key = entry.getKey();

            if (key.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(key)
                    && !"vnp_SecureHashType".equals(key)) {

                String value = first(input, key);

                if (value != null) {
                    params.put(key, value);
                }
            }
        }

        String calculatedHash = hmacSHA512(
            secret,
            buildHashData(params)
        );

        return MessageDigest.isEqual(
            calculatedHash.toLowerCase().getBytes(
                StandardCharsets.US_ASCII
            ),
            receivedHash.toLowerCase().getBytes(
                StandardCharsets.US_ASCII
            )
        );
    }

    public static String first(
            Map<String, String[]> params,
            String name) {

        String[] values = params.get(name);

        if (values == null || values.length == 0) {
            return null;
        }

        return values[0];
    }
}
