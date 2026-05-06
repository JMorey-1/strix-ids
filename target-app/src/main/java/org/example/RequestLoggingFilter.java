package org.example;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Captures request activity from the target web application and forwards it to Strix.
 *
 * This filter runs around each incoming request, records the method, URI, IP address
 * and response status code and then sends on that information to the IDS as a request event.
 */
@Component
public class RequestLoggingFilter implements Filter {

    private static final String IDS_URL = "http://localhost:8081/events/request";

    // Reused client for sending request events to the IDS.
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        /*
         * The traffic generator sends X-Forwarded-For so I can simulate different IPs
         * while all requests are still coming from my own machine.
         */
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = req.getRemoteAddr();
        }

        String method = req.getMethod();
        String uri = req.getRequestURI();

        /*
         * The wrapper lets me read the final response status after the controller runs.
         * Without this, I would only know the request details not whether it returned
         * 404, etc
         */
        StatusCapturingResponseWrapper responseWrapper =
                new StatusCapturingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, responseWrapper);

        int statusCode = responseWrapper.getStatus();

        System.out.println("[REQUEST] " + method + " " + uri + " from " + ip + " -> " + statusCode);

        forwardToIds(ip, method, uri, statusCode);
    }

    private void forwardToIds(String ip, String method, String uri, int statusCode) {
        /*
         * Builds the JSON body manually.
         */
        String body = String.format(
                "{\"ip\":\"%s\",\"method\":\"%s\",\"uri\":\"%s\",\"timestamp\":%d,\"statusCode\":%d}",
                ip, method, uri, System.currentTimeMillis(), statusCode
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IDS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        /*
         * Send asynchronously so the target app does not wait for the IDS before to respond.
         */
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(e -> {
                    System.out.println("[FILTER] Failed to forward event to IDS: " + e.getMessage());
                    return null;
                });
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /**
     * Small response wrapper used to capture the status code returned by the target app.
     *
     * The servlet response does not make this easy to track directly, so this wrapper
     * stores the status whenever the controller changes it.
     */
    private static class StatusCapturingResponseWrapper extends HttpServletResponseWrapper {

        private int status = 200;

        public StatusCapturingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public int getStatus() {
            return status;
        }
    }
}