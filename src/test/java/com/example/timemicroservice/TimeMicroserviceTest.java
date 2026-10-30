package com.example.timemicroservice;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class TimeMicroserviceTest {

    @Test
    void buildRootJsonHasRequiredFields() {
        String json = TimeMicroservice.buildRootJson();
        assertTrue(json.contains("\"date\":"));
        assertTrue(json.contains("\"time\":"));
        assertTrue(json.contains("\"timestamp\":"));
        assertTrue(json.contains("\"timezone\":"));
        assertTrue(json.contains("\"unix\":"));
        assertTrue(json.contains("\"version\":"));
    }

    @Test
    void endpointsWork() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new TimeMicroservice.RootHandler());
        server.createContext("/healthz", new TimeMicroservice.HealthHandler());
        server.createContext("/readyz", new TimeMicroservice.HealthHandler());
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        int port = server.getAddress().getPort();
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> root = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, root.statusCode());
            assertTrue(root.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
            assertTrue(root.body().contains("\"date\":"));

            HttpResponse<String> health = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/healthz")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, health.statusCode());

            HttpResponse<String> ready = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/readyz")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, ready.statusCode());

            HttpResponse<String> notFound = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/does-not-exist")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(404, notFound.statusCode());
        } finally {
            server.stop(0);
        }
    }
}
