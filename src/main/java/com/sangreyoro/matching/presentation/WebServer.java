package com.sangreyoro.matching.presentation;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/** Embedded HTTP server of the JDK (no external web framework). A fixed thread pool serves the requests. */
public class WebServer {
    private static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private final HttpServer server;
    private final ExecutorService pool;

    public WebServer(int port, MatchController api) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        pool = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
        server.setExecutor(pool);
        server.createContext("/api/", api);
        server.createContext("/", new StaticFileHandler());
    }

    public void start() {
        server.start();
        LOG.info(() -> "UC2 Find a Match running on http://localhost:" + port());
    }

    public int port() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
        pool.shutdownNow();
    }
}
