package org.example.profile;

import org.example.TrafficClient;

import java.util.Random;

/**
 * Simulates brute-force login behaviour.
 *
 * This profile repeatedly sends failed login attempts against the authentication
 * endpoint. It is designed to create a clear suspicious pattern for Strix to
 * detect through POST traffic, login attempts and 401 responses.
 */
public class BruteForceProfile implements TrafficProfile {

    private static final int BRUTE_FORCE_ATTEMPTS = 50;

    private final TrafficClient trafficClient;
    private final Random random;

    public BruteForceProfile(TrafficClient trafficClient, Random random) {
        this.trafficClient = trafficClient;
        this.random = random;
    }

    @Override
    public void run(String ipAddress) throws Exception {
        System.out.println("[ATTACKER] " + ipAddress + " starting brute force");

        for (int i = 0; i < BRUTE_FORCE_ATTEMPTS; i++) {
            /*
             * The username stays as admin while the password changes each time.
             * This creates repeated failed login behaviour from the same IP.
             */
            trafficClient.post(
                    "/auth/login",
                    "{\"username\":\"admin\",\"password\":\"attempt" + i + "\"}",
                    ipAddress
            );

            // Very short delay to make the login attempts look automated.
            Thread.sleep(randomDelay(50, 150));
        }

        System.out.println("[ATTACKER] " + ipAddress + " brute force complete");
    }

    private long randomDelay(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}