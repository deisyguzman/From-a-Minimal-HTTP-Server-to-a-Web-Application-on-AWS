package edu.arep.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public record HttpResponse(int status, String contentType, byte[] body) {
    private static final Map<Integer, String> REASONS = Map.of(
            200, "OK", 400, "Bad Request", 404, "Not Found", 405, "Method Not Allowed", 500, "Internal Server Error");

    public void writeTo(OutputStream output) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + REASONS.getOrDefault(status, "Response") + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        output.write(body);
        output.flush();
    }

    public static HttpResponse text(int status, String contentType, String body) {
        return new HttpResponse(status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }
}
