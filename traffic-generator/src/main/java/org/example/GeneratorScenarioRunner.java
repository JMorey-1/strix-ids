package org.example;

import org.example.profile.TrafficProfile;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runs the full traffic generation scenario for Strix.
 *
 * The scenario has three main stages: warm-up traffic, model training and
 * detection traffic. Normal users are used first so the IDS can learn ordinary
 * behaviour, then normal and suspicious profiles run together during detection.
 */
public class GeneratorScenarioRunner {

    private static final int WARMUP_USER_COUNT = 10;
    private static final int WARMUP_ROUNDS = 2;
    private static final int DETECTION_USER_COUNT = 5;
    private static final int BRUTE_FORCE_ATTACKER_COUNT = 3;
    private static final int ENDPOINT_SCAN_ATTACKER_COUNT = 2;
    private static final int ADMIN_PROBE_ATTACKER_COUNT = 2;

    private final TrafficClient trafficClient;
    private final TrafficProfile normalUserProfile;
    private final TrafficProfile bruteForceProfile;
    private final TrafficProfile endpointScanProfile;
    private final TrafficProfile adminProbeProfile;
    private final Random random;

    public GeneratorScenarioRunner(TrafficClient trafficClient,
                                   TrafficProfile normalUserProfile,
                                   TrafficProfile bruteForceProfile,
                                   TrafficProfile endpointScanProfile,
                                   TrafficProfile adminProbeProfile,
                                   Random random) {
        this.trafficClient = trafficClient;
        this.normalUserProfile = normalUserProfile;
        this.bruteForceProfile = bruteForceProfile;
        this.endpointScanProfile = endpointScanProfile;
        this.adminProbeProfile = adminProbeProfile;
        this.random = random;
    }

    public void runScenario() throws InterruptedException {
        runWarmupPhase();
        trainModel();
        runDetectionPhase();
        runMitigationVerification();
    }

    private void runWarmupPhase() throws InterruptedException {
        System.out.println("[GENERATOR] Starting warm-up phase...");

        try {
            /*
             * Clear any old target app mitigation state before a new scenario run.
             * This prevents previous rate limits or blocked IPs from affecting warm-up.
             */
            trafficClient.resetTargetMitigations();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to reset target mitigation state: " + e.getMessage());
        }

        try {
            // Tell the IDS to clear old training data and begin collecting normal samples.
            trafficClient.signalIds("/model/collect");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to signal IDS collect phase: " + e.getMessage());
        }

        ExecutorService warmupExecutor = Executors.newFixedThreadPool(10);

        // Warm-up uses only normal users so the model learns ordinary behaviour.
        for (int i = 1; i <= WARMUP_USER_COUNT; i++) {
            String ipAddress = "192.168.1." + i;
            warmupExecutor.submit(() -> runWarmupUser(ipAddress));
        }

        shutdownAndAwait(warmupExecutor, "warm-up");
    }

    private void runWarmupUser(String ipAddress) {
        try {
            // Each warm-up user runs more than once to give the IDS enough samples.
            for (int i = 0; i < WARMUP_ROUNDS; i++) {
                normalUserProfile.run(ipAddress);
                Thread.sleep(randomDelay(2000, 5000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[ERROR] Warm-up user " + ipAddress + " interrupted");
        } catch (Exception e) {
            System.out.println("[ERROR] Warm-up user " + ipAddress + ": " + e.getMessage());
        }
    }

    private void trainModel() throws InterruptedException {
        System.out.println("[GENERATOR] Warm-up complete, training model...");

        try {
            // Tell the IDS to train the model using the samples collected during warm-up.
            trafficClient.signalIds("/model/train");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to signal IDS train phase: " + e.getMessage());
        }

        // Small pause so the IDS has time to finish training before detection traffic starts.
        Thread.sleep(2000);
    }

    private void runDetectionPhase() throws InterruptedException {
        System.out.println("[GENERATOR] Starting detection phase...");

        ExecutorService detectionExecutor = Executors.newFixedThreadPool(20);

        // Normal users continue during detection so the IDS sees mixed traffic.
        for (int i = 1; i <= DETECTION_USER_COUNT; i++) {
            String ipAddress = "192.168.1." + i;
            detectionExecutor.submit(() -> runProfile(normalUserProfile, ipAddress, "Normal user"));
        }

        // Brute-force attackers repeatedly hit the login endpoint.
        for (int i = 1; i <= BRUTE_FORCE_ATTACKER_COUNT; i++) {
            String ipAddress = "10.0.0." + i;
            detectionExecutor.submit(() -> runProfile(bruteForceProfile, ipAddress, "Brute-force attacker"));
        }

        // Endpoint scanners touch lots of different routes.
        for (int i = 1; i <= ENDPOINT_SCAN_ATTACKER_COUNT; i++) {
            String ipAddress = "172.16.0." + i;
            detectionExecutor.submit(() -> runProfile(endpointScanProfile, ipAddress, "Endpoint scanner"));
        }

        // Admin probers focus on protected admin routes.
        for (int i = 1; i <= ADMIN_PROBE_ATTACKER_COUNT; i++) {
            String ipAddress = "203.0.113." + i;
            detectionExecutor.submit(() -> runProfile(adminProbeProfile, ipAddress, "Admin prober"));
        }

        shutdownAndAwait(detectionExecutor, "detection");
        System.out.println("[GENERATOR] Detection phase complete");
    }

    private void runMitigationVerification() throws InterruptedException {
        System.out.println("[GENERATOR] Starting mitigation verification...");

        try {
            /*
             * These requests give a simple final proof that the target app is enforcing
             * IDS mitigation actions. Attacker IPs should eventually receive 429 or 403,
             * while a normal IP should still receive a normal successful response.
             */
            for (int i = 1; i <= 8; i++) {
                trafficClient.verificationGet("Brute-force attacker check", "/", "10.0.0.1");
                Thread.sleep(300);
            }

            trafficClient.verificationGet("Endpoint scanner check", "/admin/users", "172.16.0.1");
            trafficClient.verificationGet("Admin prober check", "/admin/users", "203.0.113.1");
            trafficClient.verificationGet("Normal user check", "/", "192.168.1.1");

            System.out.println("[GENERATOR] Mitigation verification complete");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] Mitigation verification failed: " + e.getMessage());
        }
    }

    private void runProfile(TrafficProfile profile, String ipAddress, String label) {
        try {
            profile.run(ipAddress);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[ERROR] " + label + " " + ipAddress + " interrupted");
        } catch (Exception e) {
            System.out.println("[ERROR] " + label + " " + ipAddress + ": " + e.getMessage());
        }
    }

    private void shutdownAndAwait(ExecutorService executorService, String phaseName)
            throws InterruptedException {
        executorService.shutdown();

        boolean finished = executorService.awaitTermination(120, TimeUnit.SECONDS);
        if (!finished) {
            System.out.println("[WARNING] " + phaseName + " phase timed out");
            executorService.shutdownNow();
        }
    }

    private long randomDelay(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}