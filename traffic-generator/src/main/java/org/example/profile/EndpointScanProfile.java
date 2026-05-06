package org.example.profile;

import org.example.TrafficClient;

import java.util.Random;

/**
 * Simulates endpoint scanning behaviour.
 *
 * This profile sends requests across a wide range of public, protected and
 * non-existent routes. The aim is to create behaviour that looks different from
 * normal browsing, especially through high endpoint variety and error responses.
 */
public class EndpointScanProfile implements TrafficProfile {

    private static final String[] SCAN_ENDPOINTS = {
            "/", "/login", "/auth/login", "/register", "/contact",
            "/products", "/products/1", "/products/99",
            "/admin", "/admin/users", "/admin/settings", "/admin/reports",
            "/user/profile", "/user/dashboard",
            "/api/data", "/api/admin", "/api/config",
            "/debug", "/hidden", "/backup", "/test"
    };

    private final TrafficClient trafficClient;
    private final Random random;

    public EndpointScanProfile(TrafficClient trafficClient, Random random) {
        this.trafficClient = trafficClient;
        this.random = random;
    }

    @Override
    public void run(String ipAddress) throws Exception {
        System.out.println("[ATTACKER] " + ipAddress + " starting endpoint scan");

        // Each scan uses a random number of requests so the pattern is not identical every time.
        int requestCount = random.nextInt(16) + 15; // 15 to 30 requests

        for (int i = 0; i < requestCount; i++) {
            String endpoint = SCAN_ENDPOINTS[random.nextInt(SCAN_ENDPOINTS.length)];

            /*
             * The scanner moves quickly between different endpoints.
             * Some will return normal responses, while others should return 401, 403 or 404.
             */
            trafficClient.get(endpoint, ipAddress);

            Thread.sleep(randomDelay(100, 350));
        }

        System.out.println("[ATTACKER] " + ipAddress + " endpoint scan complete");
    }

    private long randomDelay(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}