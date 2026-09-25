package edu.arep.web;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public record HttpRequest(String method, String target, String path, Map<String, String> query) {
    public static HttpRequest parse(String requestLine) {
        String[] parts = requestLine.split(" ", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Malformed request line");
        }
        URI uri = URI.create(parts[1]);
        Map<String, String> query = new LinkedHashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery != null && !rawQuery.isBlank()) {
            for (String pair : rawQuery.split("&")) {
                String[] keyValue = pair.split("=", 2);
                String key = decode(keyValue[0]);
                String value = keyValue.length == 2 ? decode(keyValue[1]) : "";
                query.put(key, value);
            }
        }
        String path = uri.getPath();
        return new HttpRequest(parts[0], parts[1], path == null || path.isBlank() ? "/" : path, query);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
