package com.sangreyoro.matching.presentation;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Serves the single-page web UI from the classpath folder /web (Presentation Layer). */
public class StaticFileHandler implements HttpHandler {
    private static final Map<String, String> TYPES = Map.of(
            "html", "text/html; charset=utf-8", "css", "text/css; charset=utf-8",
            "js", "application/javascript; charset=utf-8", "svg", "image/svg+xml");

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.contains("..")) {   // never serve files outside /web
            HttpExchangeHelper.send(ex, 400, "text/plain", "Bad path".getBytes(StandardCharsets.UTF_8));
            return;
        }
        try (InputStream in = StaticFileHandler.class.getResourceAsStream("/web" + path)) {
            if (in == null) {
                HttpExchangeHelper.send(ex, 404, "text/plain", "Not found".getBytes(StandardCharsets.UTF_8));
                return;
            }
            String ext = path.substring(path.lastIndexOf('.') + 1);
            HttpExchangeHelper.send(ex, 200, TYPES.getOrDefault(ext, "application/octet-stream"), in.readAllBytes());
        }
    }
}
