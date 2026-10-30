package com.example.timemicroservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;

/**
 * A small HTTP microservice that returns the current date and time for the root path.
 */
public class TimeMicroservice {

    public static final String VERSION = System.getenv().getOrDefault("VERSION", "dev");

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/healthz", new HealthHandler());
        server.createContext("/readyz", new HealthHandler());
        server.setExecutor(Executors.newFixedThreadPool(8));

        System.out.printf("time-microservice %s listening on :%d%n", VERSION, port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutting down gracefully...");
            server.stop(0);
            System.out.println("server stopped");
        }));
    }

    /** Builds the JSON response body for the root path. */
    public static String buildRootJson() {
        ZoneId zone = resolveZone();
        ZonedDateTime now = ZonedDateTime.now(zone);
        return String.format(
                "{\"date\":\"%s\",\"time\":\"%s\",\"timestamp\":\"%s\",\"timezone\":\"%s\",\"unix\":%d,\"version\":\"%s\"}",
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                zone.getId(),
                now.toEpochSecond(),
                VERSION
        );
    }

    static ZoneId resolveZone() {
        String tz = System.getenv("TZ");
        if (tz != null && !tz.isBlank()) {
            try {
                return ZoneId.of(tz);
            } catch (Exception ignored) {
                // fall through to default
            }
        }
        return ZoneId.systemDefault();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (!"/".equals(path)) {
                byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(404, body.length);
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(body);
                }
                return;
            }
            byte[] body = buildRootJson().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            byte[] body = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        }
    }
}
