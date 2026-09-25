package edu.arep.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public final class RequestHandler {
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".html", "text/html; charset=UTF-8",
            ".js", "text/javascript; charset=UTF-8",
            ".css", "text/css; charset=UTF-8",
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".svg", "image/svg+xml");

    public HttpResponse handle(HttpRequest request) {
        if (!"GET".equals(request.method())) {
            return json(405, "method_not_allowed", "Only GET is supported");
        }
        try {
            return switch (request.path()) {
                case "/api/greeting" -> greeting(request.query());
                case "/api/square" -> square(request.query());
                case "/api/time" -> json(200, "time", "{\"time\":\"" + Instant.now() + "\"}");
                case "/api/health" -> json(200, "status", "{\"status\":\"ok\"}");
                default -> staticResource(request.path());
            };
        } catch (IllegalArgumentException exception) {
            return json(400, "invalid_request", exception.getMessage());
        }
    }

    private HttpResponse greeting(Map<String, String> query) {
        String name = query.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return json(200, "greeting", "{\"message\":\"Hello, " + escapeJson(name) + "!\"}");
    }

    private HttpResponse square(Map<String, String> query) {
        String raw = query.get("value");
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("value is required");
        }
        try {
            long value = Long.parseLong(raw);
            long square = Math.multiplyExact(value, value);
            return json(200, "square", "{\"value\":" + value + ",\"square\":" + square + "}");
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException("value must be a valid integer");
        }
    }

    private HttpResponse staticResource(String path) {
        String resourcePath = "/public" + ("/".equals(path) ? "/index.html" : path);
        if (path.contains("\\") || path.split("/").length != path.replace("//", "/").split("/").length
                || path.contains("..")) {
            return text(400, "Unsafe path");
        }
        String type = CONTENT_TYPES.entrySet().stream()
                .filter(entry -> resourcePath.toLowerCase().endsWith(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse(null);
        if (type == null) {
            return text(404, "Resource not found");
        }
        try (InputStream input = RequestHandler.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return text(404, "Resource not found");
            }
            return new HttpResponse(200, type, input.readAllBytes());
        } catch (IOException exception) {
            return text(500, "Unable to read resource");
        }
    }

    private HttpResponse json(int status, String type, String value) {
        String body = value.startsWith("{") ? value : "{\"error\":\"" + escapeJson(value) + "\"}";
        return HttpResponse.text(status, "application/json; charset=UTF-8", body);
    }

    private HttpResponse text(int status, String message) {
        return HttpResponse.text(status, "text/plain; charset=UTF-8", message + "\n");
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\b", "\\b").replace("\f", "\\f");
    }
}
