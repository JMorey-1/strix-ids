package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.profile.TrafficProfile;
import org.junit.jupiter.api.Test;

/*
 * This test runs the full scenario with fake traffic profiles.
 * It may take a few seconds because GeneratorScenarioRunner includes sleep delays.
 */
class GeneratorScenarioRunnerTest {

  @Test
  void runScenario_ShouldRunWarmupTrainingAndDetectionPhases() throws Exception {
    // Create fake client
    FakeTrafficClient trafficClient = new FakeTrafficClient();

    // Create fake profiles
    RecordingProfile normalUserProfile = new RecordingProfile();
    RecordingProfile bruteForceProfile = new RecordingProfile();
    RecordingProfile endpointScanProfile = new RecordingProfile();
    RecordingProfile adminProbeProfile = new RecordingProfile();

    // Use predictable delay values
    Random random = new DeterministicRandom();

    GeneratorScenarioRunner runner =
        new GeneratorScenarioRunner(
            trafficClient,
            normalUserProfile,
            bruteForceProfile,
            endpointScanProfile,
            adminProbeProfile,
            random);

    runner.runScenario();

    // Check IDS signals
    assertEquals(2, trafficClient.signalledPaths.size());
    assertEquals("/model/collect", trafficClient.signalledPaths.get(0));
    assertEquals("/model/train", trafficClient.signalledPaths.get(1));

    // Check warm-up and detection normal users
    assertEquals(25, normalUserProfile.runCount.get());

    // Check attacker profile counts
    assertEquals(3, bruteForceProfile.runCount.get());
    assertEquals(2, endpointScanProfile.runCount.get());
    assertEquals(2, adminProbeProfile.runCount.get());

    // Check expected IP groups
    assertTrue(normalUserProfile.ipAddresses.contains("192.168.1.1"));
    assertTrue(bruteForceProfile.ipAddresses.contains("10.0.0.1"));
    assertTrue(endpointScanProfile.ipAddresses.contains("172.16.0.1"));
    assertTrue(adminProbeProfile.ipAddresses.contains("203.0.113.1"));
  }

  private static class FakeTrafficClient extends TrafficClient {

    private final List<String> signalledPaths = new CopyOnWriteArrayList<>();

    FakeTrafficClient() {
      super("http://localhost:8080", "http://localhost:8081");
    }

    @Override
    public void signalIds(String path) {
      // Record IDS signal
      signalledPaths.add(path);
    }
  }

  private static class RecordingProfile implements TrafficProfile {

    private final AtomicInteger runCount = new AtomicInteger();
    private final List<String> ipAddresses = new CopyOnWriteArrayList<>();

    @Override
    public void run(String ipAddress) {
      // Record profile run
      runCount.incrementAndGet();
      ipAddresses.add(ipAddress);
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
