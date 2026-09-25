package edu.arep.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() { }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "35000"));
        String host = System.getenv().getOrDefault("HOST", "0.0.0.0");
        for (int index = 0; index < args.length; index++) {
            if ("--port".equals(args[index]) && index + 1 < args.length) {
                port = Integer.parseInt(args[++index]);
            } else if ("--host".equals(args[index]) && index + 1 < args.length) {
                host = args[++index];
            }
        }
        new Main().serve(host, port);
    }

    void serve(String host, int port) throws IOException {
        RequestHandler handler = new RequestHandler();
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(host, port));
            System.out.println("Sequential server listening on " + host + ":" + server.getLocalPort());
            while (!server.isClosed()) {
                try (Socket client = server.accept()) {
                    handle(client, handler);
                } catch (RuntimeException exception) {
                    System.err.println("Request rejected: " + exception.getMessage());
                }
            }
        }
    }

    private void handle(Socket client, RequestHandler handler) throws IOException {
        client.setSoTimeout(5000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            HttpResponse.text(400, "text/plain; charset=UTF-8", "Malformed request\n").writeTo(client.getOutputStream());
            return;
        }
        try {
            String header;
            while ((header = reader.readLine()) != null && !header.isEmpty()) {
                // Consume headers; this lab has no request body.
            }
            handler.handle(HttpRequest.parse(requestLine)).writeTo(client.getOutputStream());
        } catch (IllegalArgumentException exception) {
            HttpResponse.text(400, "text/plain; charset=UTF-8", "Malformed request\n").writeTo(client.getOutputStream());
        }
    }
}
