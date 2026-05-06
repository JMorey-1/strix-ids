package org.example.profile;

import org.example.TrafficClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * This test uses a predictable Random object so the delay is repeatable.
 * The profile includes 50 small sleep delays so this test may take a few seconds.
 */
class BruteForceProfileTest {

    @Test
    void run_ShouldSendFiftyFailedLoginAttempts() throws Exception {
        // Create fake client
        FakeTrafficClient trafficClient = new FakeTrafficClient();

        // Use predictable random values
        Random random = new DeterministicRandom();

        BruteForceProfile profile = new BruteForceProfile(trafficClient, random);

        profile.run("10.0.0.60");

        // Check request count
        assertEquals(50, trafficClient.postCount);

        // Check request details
        assertEquals("/auth/login", trafficClient.lastPath);
        assertEquals("10.0.0.60", trafficClient.lastIpAddress);

        // Check first and last attempts
        assertTrue(trafficClient.bodies.get(0).contains("\"password\":\"attempt0\""));
        assertTrue(trafficClient.bodies.get(49).contains("\"password\":\"attempt49\""));
    }

    private static class FakeTrafficClient extends TrafficClient {

        private int postCount;
        private String lastPath;
        private String lastIpAddress;
        private final List<String> bodies = new ArrayList<>();

        FakeTrafficClient() {
            super("http://localhost:8080", "http://localhost:8081");
        }

        @Override
        public int post(String path, String body, String ipAddress)
                throws IOException, InterruptedException {
            postCount++;
            lastPath = path;
            lastIpAddress = ipAddress;
            bodies.add(body);
            return 401;
        }
    }

    private static class DeterministicRandom extends Random {

        @Override
        public int nextInt(int bound) {
            // Always use minimum delay
            return 0;
        }
    }
}