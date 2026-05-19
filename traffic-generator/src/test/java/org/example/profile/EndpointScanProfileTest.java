package org.example.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.TrafficClient;
import org.junit.jupiter.api.Test;

/*
 * This test uses a predictable Random object so the scan behaviour
 * is repeatable instead of changing on every test run.
 *
 * The profile includes small sleep delays so this test may take
 * slightly longer than usual.
 */
class EndpointScanProfileTest {

  @Test
  void run_ShouldSendEndpointScanRequests() throws Exception {
    // Create fake client
    FakeTrafficClient trafficClient = new FakeTrafficClient();

    // Use predictable random values
    Random random = new DeterministicRandom();

    EndpointScanProfile profile = new EndpointScanProfile(trafficClient, random);

    profile.run("10.0.0.70");

    // Check request count
    assertEquals(15, trafficClient.getCount);

    // Check IP address
    assertEquals("10.0.0.70", trafficClient.lastIpAddress);

    // Check some scanned endpoints
    assertTrue(trafficClient.paths.contains("/"));
    assertTrue(trafficClient.paths.contains("/login"));
    assertTrue(trafficClient.paths.contains("/auth/login"));
    assertTrue(trafficClient.paths.contains("/admin"));
    assertTrue(trafficClient.paths.contains("/api/data"));
  }

  private static class FakeTrafficClient extends TrafficClient {

    private int getCount;
    private String lastIpAddress;
    private final List<String> paths = new ArrayList<>();

    FakeTrafficClient() {
      super("http://localhost:8080", "http://localhost:8081");
    }

    @Override
    public int get(String path, String ipAddress) throws IOException, InterruptedException {
      getCount++;
      lastIpAddress = ipAddress;
      paths.add(path);
      return 200;
    }
  }

  private static class DeterministicRandom extends Random {

    private int endpointIndex = 0;

    @Override
    public int nextInt(int bound) {
      // Use minimum request count
      if (bound == 16) {
        return 0;
      }

      // Use minimum delay
      if (bound == 251) {
        return 0;
      }

      // Cycle through endpoints
      return endpointIndex++ % bound;
    }
  }
}
