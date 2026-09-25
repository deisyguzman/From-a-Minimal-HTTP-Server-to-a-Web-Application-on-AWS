package edu.arep.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HttpRequestTest {
    @Test
    void decodesPathAndQueryParameters() {
        HttpRequest request = HttpRequest.parse("GET /api/greeting?name=Ana%20Maria HTTP/1.1");
        assertEquals("GET", request.method());
        assertEquals("/api/greeting", request.path());
        assertEquals("Ana Maria", request.query().get("name"));
    }
}
