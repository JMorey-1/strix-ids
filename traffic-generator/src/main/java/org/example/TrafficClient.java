package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Small HTTP client used by the traffic generator.
 *
 * This class keeps the request-sending logic in one place. The generator can then
 * focus on behaviour patterns, while this client handles GET requests, POST requests,
 * fake IP headers and signalling the IDS.
 */
public class TrafficClient {

    private final String baseUrl;
    private final String idsBaseUrl;
    private final HttpClient client;

    public TrafficClient(String baseUrl, String idsBaseUrl) {
        this.baseUrl = baseUrl;
        this.idsBaseUrl = idsBaseUrl;
        this.client = HttpClient.newHttpClient();
    }

    public int get(String path, String ipAddress) throws IOException, InterruptedException {
        return get(path, ipAddress, null);
    }

    public int get(String path, String ipAddress, String authHeader)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                // Used to simulate different clients from one machine.
                .header("X-Forwarded-For", ipAddress);

        if (authHeader != null && !authHeader.isBlank()) {
            builder.header("Authorization", authHeader);
        }

        HttpRequest request = builder.GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logRequest("GET", ipAddress, path, response.statusCode());

        return response.statusCode();
    }

    public int post(String path, String body, String ipAddress) throws IOException, InterruptedException {
        return post(path, body, ipAddress, null);
    }

    public int post(String path, String body, String ipAddress, String authHeader)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                // Used to simulate different clients from one machine.
                .header("X-Forwarded-For", ipAddress);

        if (authHeader != null && !authHeader.isBlank()) {
            builder.header("Authorization", authHeader);
        }

        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logRequest("POST", ipAddress, path, response.statusCode());

        return response.statusCode();
    }

    public void signalIds(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(idsBaseUrl + path))
                .GET()
                .build();

        /*
         * The generator uses this to tell Strix when to collect training data
         * and when to train the model.
         */
        client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[GENERATOR] Signalled IDS: " + path);
    }

    public void resetTargetMitigations() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/internal/mitigation/reset"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        /*
         * Clears old target app mitigation state before a new demo run.
         * This prevents previous rate limits or blacklist entries from affecting
         * the next warm-up and detection cycle.
         */
        client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[GENERATOR] Reset target mitigation state");
    }

    public int verificationGet(String label, String path, String ipAddress)
            throws IOException, InterruptedException {
        /*
         * Used at the end of the scenario to prove that mitigation is being enforced.
         * For example, an attacker IP should receive 429 or 403, while a normal IP
         * should still receive a normal successful response.
         */
        int statusCode = get(path, ipAddress);

        System.out.println("[GENERATOR][VERIFY] " + label
                + " ip=" + ipAddress
                + " path=" + path
                + " status=" + statusCode);

        return statusCode;
    }

    private void logRequest(String method, String ipAddress, String path, int statusCode) {
        System.out.println("[" + method + "] " + ipAddress + " " + path + " -> " + statusCode);
    }
}