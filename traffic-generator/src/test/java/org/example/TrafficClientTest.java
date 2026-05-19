package org.example;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TrafficClientTest {

  private final List<HttpServer> servers = new ArrayList<>();

  @AfterEach
  void tearDown() {
    // Stop test servers
    for (HttpServer server : servers) {
      server.stop(0);
    }
  }

  @Test
  void get_ShouldSendForwardedIpAndReturnStatusCode() throws Exception {
    // Create test server
    RecordingHandler handler = new RecordingHandler(200);
    HttpServer server = startServer("/products", handler);

    TrafficClient client = new TrafficClient(baseUrl(server), "http://127.0.0.1:9999");

    int statusCode = client.get("/products", "10.0.0.5");

    // Check response
    assertEquals(200, statusCode);

    // Check request details
    RecordedRequest request = handler.getRecordedRequest();
    assertEquals("GET", request.method);
    assertEquals("/products", request.path);
    assertEquals("10.0.0.5", request.forwardedFor);
  }

  @Test
  void get_WithAuthHeader_ShouldSendAuthorizationHeader() throws Exception {
    // Create test server
    RecordingHandler handler = new RecordingHandler(200);
    HttpServer server = startServer("/admin", handler);

    TrafficClient client = new TrafficClient(baseUrl(server), "http://127.0.0.1:9999");

    int statusCode = client.get("/admin", "10.0.0.10", "Bearer admin-token");

    // Check response
    assertEquals(200, statusCode);

    // Check auth header
    RecordedRequest request = handler.getRecordedRequest();
    assertEquals("Bearer admin-token", request.authorization);
  }

  @Test
  void post_ShouldSendBodyAndReturnStatusCode() throws Exception {
    // Create test server
    RecordingHandler handler = new RecordingHandler(201);
    HttpServer server = startServer("/auth/login", handler);

    TrafficClient client = new TrafficClient(baseUrl(server), "http://127.0.0.1:9999");

    String body = "{\"username\":\"user1\",\"password\":\"pass123\"}";

    int statusCode = client.post("/auth/login", body, "10.0.0.20");

    // Check response
    assertEquals(201, statusCode);

    // Check request details
    RecordedRequest request = handler.getRecordedRequest();
    assertEquals("POST", request.method);
    assertEquals("/auth/login", request.path);
    assertEquals("10.0.0.20", request.forwardedFor);
    assertEquals("application/json", request.contentType);
    assertEquals(body, request.body);
  }

  @Test
  void post_WithAuthHeader_ShouldSendAuthorizationHeader() throws Exception {
    // Create test server
    RecordingHandler handler = new RecordingHandler(200);
    HttpServer server = startServer("/admin/settings", handler);

    TrafficClient client = new TrafficClient(baseUrl(server), "http://127.0.0.1:9999");

    int statusCode =
        client.post("/admin/settings", "{\"mode\":\"safe\"}", "10.0.0.30", "Bearer admin-token");

    // Check response
    assertEquals(200, statusCode);

    // Check auth header
    RecordedRequest request = handler.getRecordedRequest();
    assertEquals("Bearer admin-token", request.authorization);
  }

  @Test
  void signalIds_ShouldSendGetRequestToIds() throws Exception {
    // Create fake IDS server
    RecordingHandler handler = new RecordingHandler(200);
    HttpServer idsServer = startServer("/model/collect", handler);

    TrafficClient client = new TrafficClient("http://127.0.0.1:9999", baseUrl(idsServer));

    client.signalIds("/model/collect");

    // Check IDS request
    RecordedRequest request = handler.getRecordedRequest();
    assertEquals("GET", request.method);
    assertEquals("/model/collect", request.path);
  }

  private HttpServer startServer(String path, RecordingHandler handler) throws IOException {
    // Start local server
    HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(path, handler);
    server.start();
    servers.add(server);
    return server;
  }

  private String baseUrl(HttpServer server) {
    // Build local URL
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  private static class RecordingHandler implements HttpHandler {

    private final int statusCode;
    private final CountDownLatch latch = new CountDownLatch(1);
    private RecordedRequest recordedRequest;

    RecordingHandler(int statusCode) {
      this.statusCode = statusCode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      // Record request
      String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

      recordedRequest =
          new RecordedRequest(
              exchange.getRequestMethod(),
              exchange.getRequestURI().getPath(),
              exchange.getRequestHeaders().getFirst("X-Forwarded-For"),
              exchange.getRequestHeaders().getFirst("Authorization"),
              exchange.getRequestHeaders().getFirst("Content-Type"),
              body);

      // Send response
      byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(statusCode, response.length);
      exchange.getResponseBody().write(response);
      exchange.close();

      latch.countDown();
    }

    RecordedRequest getRecordedRequest() throws InterruptedException {
      // Wait for request
      boolean received = latch.await(1, TimeUnit.SECONDS);
      assertTrue(received);
      assertNotNull(recordedRequest);
      return recordedRequest;
    }
  }

  private record RecordedRequest(
      String method,
      String path,
      String forwardedFor,
      String authorization,
      String contentType,
      String body) {}
}
