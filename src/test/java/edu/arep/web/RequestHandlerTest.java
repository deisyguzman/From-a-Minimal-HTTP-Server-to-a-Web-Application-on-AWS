package edu.arep.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RequestHandlerTest {
    private final RequestHandler handler = new RequestHandler();

    @Test
    void servesHomeWithHtmlType() {
        HttpResponse response = handler.handle(HttpRequest.parse("GET / HTTP/1.1"));
        assertEquals(200, response.status());
        assertEquals("text/html; charset=UTF-8", response.contentType());
        assertTrue(new String(response.body()).contains("Sequential Services"));
    }

    @Test
    void servesJavaScriptAsBytesWithCorrectType() {
        HttpResponse response = handler.handle(HttpRequest.parse("GET /app.js HTTP/1.1"));
        assertEquals(200, response.status());
        assertEquals("text/javascript; charset=UTF-8", response.contentType());
        assertTrue(response.body().length > 0);
    }

    @Test
    void servesPngAndJpegWithBinaryTypes() {
        HttpResponse png = handler.handle(HttpRequest.parse("GET /pixel.png HTTP/1.1"));
        HttpResponse jpeg = handler.handle(HttpRequest.parse("GET /sample.jpg HTTP/1.1"));
        assertEquals("image/png", png.contentType());
        assertEquals("image/jpeg", jpeg.contentType());
        assertTrue(png.body().length > 0);
        assertTrue(jpeg.body().length > 0);
    }

    @Test
    void returnsEscapedGreeting() {
        HttpResponse response = handler.handle(HttpRequest.parse("GET /api/greeting?name=Ada%20%22Lovelace%22 HTTP/1.1"));
        assertEquals(200, response.status());
        assertTrue(new String(response.body()).contains("Ada \\\"Lovelace\\\""));
    }

    @Test
    void validatesServicesAndMethods() {
        assertEquals(400, handler.handle(HttpRequest.parse("GET /api/square?value=nope HTTP/1.1")).status());
        assertEquals(400, handler.handle(HttpRequest.parse("GET /api/square?value=9223372036854775807 HTTP/1.1")).status());
        assertEquals(400, handler.handle(HttpRequest.parse("GET /api/greeting HTTP/1.1")).status());
        assertEquals(405, handler.handle(HttpRequest.parse("POST /api/time HTTP/1.1")).status());
    }

    @Test
    void rejectsUnknownAndUnsafeResources() {
        assertEquals(404, handler.handle(HttpRequest.parse("GET /missing.html HTTP/1.1")).status());
        assertEquals(400, handler.handle(HttpRequest.parse("GET /../pom.xml HTTP/1.1")).status());
    }
}
