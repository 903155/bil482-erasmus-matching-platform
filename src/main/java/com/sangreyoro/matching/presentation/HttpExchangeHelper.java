package com.sangreyoro.matching.presentation;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Small helpers to read query/form parameters and to write responses with the JDK HTTP server. */
final class HttpExchangeHelper {
    private HttpExchangeHelper() {
    }

    static Map<String, String> query(HttpExchange ex) {
        return parse(ex.getRequestURI().getRawQuery());
    }

    static Map<String, String> form(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] body = in.readNBytes(16_384);   // DV-07: request bodies are limited to 16 KB
            return parse(new String(body, StandardCharsets.UTF_8));
        }
    }

    static Map<String, String> parse(String raw) {
        Map<String, String> m = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return m;
        for (String pair : raw.split("&")) {
            int i = pair.indexOf('=');
            String k = i < 0 ? pair : pair.substring(0, i);
            String v = i < 0 ? "" : pair.substring(i + 1);
            m.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
        }
        return m;
    }

    static void send(HttpExchange ex, int status, String contentType, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    static void json(HttpExchange ex, int status, String json) throws IOException {
        send(ex, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }
}
