package org.example;

import org.example.profile.AdminProbeProfile;
import org.example.profile.BruteForceProfile;
import org.example.profile.EndpointScanProfile;
import org.example.profile.NormalUserProfile;
import org.example.profile.TrafficProfile;

import java.util.Random;
import java.security.SecureRandom;

/**
 * Starts the Strix traffic generator.
 *
 * This class wires together the traffic client, the different behaviour profiles
 * and the scenario runner. The generator is used to create normal traffic first,
 * then suspicious traffic for the IDS to detect.
 */
public class StrixGenerator {

    private static final String BASE_URL = getEnvOrDefault(
            "STRIX_TARGET_BASE_URL",
            "http://localhost:8080"
    );

    private static final String IDS_BASE_URL = getEnvOrDefault(
            "STRIX_IDS_BASE_URL",
            "http://localhost:8081"
    );

    public static void main(String[] args) throws Exception {
        Random random = new SecureRandom();

        TrafficClient trafficClient = new TrafficClient(BASE_URL, IDS_BASE_URL);

        /*
         * Each profile represents a different type of client behaviour.
         * This keeps the generator easier to extend than one large class.
         */
        TrafficProfile normalUserProfile = new NormalUserProfile(trafficClient, random);
        TrafficProfile bruteForceProfile = new BruteForceProfile(trafficClient, random);
        TrafficProfile endpointScanProfile = new EndpointScanProfile(trafficClient, random);
        TrafficProfile adminProbeProfile = new AdminProbeProfile(trafficClient, random);

        GeneratorScenarioRunner scenarioRunner = new GeneratorScenarioRunner(
                trafficClient,
                normalUserProfile,
                bruteForceProfile,
                endpointScanProfile,
                adminProbeProfile,
                random
        );

        scenarioRunner.runScenario();
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}