package org.example.profile;

import org.example.TrafficClient;

import java.util.Random;

/**
 * Simulates probing against protected admin routes.
 *
 * This profile repeatedly tries to access admin endpoints without a valid token.
 * It is meant to create suspicious admin-focused behaviour for Strix to detect
 * through admin route usage and unauthorised or forbidden responses.
 */
public class AdminProbeProfile implements TrafficProfile {

    private static final String[] ADMIN_ENDPOINTS = {
            "/admin",
            "/admin/users",
            "/admin/settings",
            "/admin/reports",
            "/admin/audit"
    };

    private final TrafficClient trafficClient;
    private final Random random;

    public AdminProbeProfile(TrafficClient trafficClient, Random random) {
        this.trafficClient = trafficClient;
        this.random = random;
    }

    @Override
    public void run(String ipAddress) throws Exception {
        System.out.println("[ATTACKER] " + ipAddress + " starting admin probe");

        // Use a random number of attempts so each run is slightly different.
        int attempts = random.nextInt(8) + 8; // 8 to 15 attempts

        for (int i = 0; i < attempts; i++) {
            String endpoint = ADMIN_ENDPOINTS[random.nextInt(ADMIN_ENDPOINTS.length)];

            /*
             * Mix GET and POST requests to make the probe a little more realistic.
             * Since no valid auth header is sent, the target app should reject these.
             */
            if (random.nextBoolean()) {
                trafficClient.get(endpoint, ipAddress);
            } else {
                trafficClient.post(endpoint, "{\"action\":\"probe\"}", ipAddress);
            }

            // Faster than normal browsing, but not as rapid as brute force.
            Thread.sleep(randomDelay(200, 600));
        }

        System.out.println("[ATTACKER] " + ipAddress + " admin probe complete");
    }

    private long randomDelay(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}