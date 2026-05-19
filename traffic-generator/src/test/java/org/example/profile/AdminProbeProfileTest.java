package org.example.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Random;
import org.example.TrafficClient;
import org.junit.jupiter.api.Test;

/*
 * This test uses a predictable Random object so the profile behaviour
 * is repeatable instead of changing on every test run.
 *
 * The profile includes small sleep delays so this test may take
 * slightly longer than usual to run.
 */
class AdminProbeProfileTest {

  @Test
  void run_ShouldSendAdminProbeRequests() throws Exception {
    // Create fake client
    FakeTrafficClient trafficClient = new FakeTrafficClient();

    // Use predictable random values
    Random random = new DeterministicRandom();

    AdminProbeProfile profile = new AdminProbeProfile(trafficClient, random);

    profile.run("10.0.0.50");

    // Check request counts
    assertEquals(4, trafficClient.getCount);
    assertEquals(4, trafficClient.postCount);
    assertEquals(8, trafficClient.totalCount);

    // Check request details
    assertEquals("/admin", trafficClient.lastPath);
    assertEquals("10.0.0.50", trafficClient.lastIpAddress);
    assertEquals("{\"action\":\"probe\"}", trafficClient.lastBody);
  }

  private static class FakeTrafficClient extends TrafficClient {

    private int getCount;
    private int postCount;
    private int totalCount;
    private String lastPath;
    private String lastIpAddress;
    private String lastBody;

    FakeTrafficClient() {
      super("http://localhost:8080", "http://localhost:8081");
    }

    @Override
    public int get(String path, String ipAddress) throws IOException, InterruptedException {
      getCount++;
      totalCount++;
      lastPath = path;
      lastIpAddress = ipAddress;
      return 401;
    }

    @Override
    public int post(String path, String body, String ipAddress)
        throws IOException, InterruptedException {
      postCount++;
      totalCount++;
      lastPath = path;
      lastIpAddress = ipAddress;
      lastBody = body;
      return 401;
    }
  }

  private static class DeterministicRandom extends Random {

    private int booleanCalls = 0;

    @Override
    public int nextInt(int bound) {
      // Always pick first option
      return 0;
    }

    @Override
    public boolean nextBoolean() {
      // Alternate GET and POST
      return booleanCalls++ % 2 == 0;
    }
  }
}
