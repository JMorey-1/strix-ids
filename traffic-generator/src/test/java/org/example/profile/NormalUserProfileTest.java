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
 * This test uses a predictable Random object so the normal user behaviour
 * is repeatable instead of changing on every test run.
 *
 * This only tests one simple normal browsing path because the real profile
 * has several random session types.
 */
class NormalUserProfileTest {

  @Test
  void run_ShouldSendNormalBrowsingRequest() throws Exception {
    // Create fake client
    FakeTrafficClient trafficClient = new FakeTrafficClient();

    // Use predictable random values
    Random random = new ShortSessionRandom();

    NormalUserProfile profile = new NormalUserProfile(trafficClient, random);

    profile.run("10.0.0.80");

    // Check request count
    assertEquals(1, trafficClient.getCount);
    assertEquals(0, trafficClient.postCount);

    // Check request details
    assertEquals("10.0.0.80", trafficClient.lastIpAddress);
    assertTrue(trafficClient.paths.contains("/"));
  }

  private static class FakeTrafficClient extends TrafficClient {

    private int getCount;
    private int postCount;
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

    @Override
    public int get(String path, String ipAddress, String authHeader)
        throws IOException, InterruptedException {
      getCount++;
      lastIpAddress = ipAddress;
      paths.add(path);
      return 200;
    }

    @Override
    public int post(String path, String body, String ipAddress)
        throws IOException, InterruptedException {
      postCount++;
      lastIpAddress = ipAddress;
      return 200;
    }

    @Override
    public int post(String path, String body, String ipAddress, String authHeader)
        throws IOException, InterruptedException {
      postCount++;
      lastIpAddress = ipAddress;
      return 200;
    }
  }

  private static class ShortSessionRandom extends Random {

    @Override
    public int nextInt(int bound) {
      // Always choose the first option
      return 0;
    }

    @Override
    public boolean nextBoolean() {
      // Skip visiting home first
      return false;
    }

    @Override
    public double nextDouble() {
      // Skip optional repeat actions
      return 1.0;
    }
  }
}
