package org.example.profile;

import java.util.Random;
import org.example.TrafficClient;

/**
 * Simulates normal user behaviour against the target web application.
 *
 * <p>This profile is used during both warm-up and detection. During warm-up it gives Strix examples
 * of ordinary browsing behaviour, then during detection it helps prove that normal users are not
 * being flagged as attacks.
 */
public class NormalUserProfile implements TrafficProfile {

  private static final String USER_TOKEN = "Bearer user-token";

  private static final String[] PUBLIC_ENDPOINTS = {
    "/",
    "/products",
    "/products/1",
    "/products/2",
    "/products/3",
    "/articles",
    "/contact",
    "/api/data"
  };

  private static final String[] AUTHENTICATED_ENDPOINTS = {"/user/profile", "/user/dashboard"};

  private final TrafficClient trafficClient;
  private final Random random;

  public NormalUserProfile(TrafficClient trafficClient, Random random) {
    this.trafficClient = trafficClient;
    this.random = random;
  }

  @Override
  public void run(String ipAddress) throws Exception {
    System.out.println("[NORMAL] " + ipAddress + " starting session");

    /*
     * Normal users do not all behave the same way.
     * I vary the session type so the training data is not too rigid.
     */
    int sessionType = random.nextInt(3);

    if (sessionType == 0) {
      runShortBrowsingSession(ipAddress);
    } else if (sessionType == 1) {
      runStandardSession(ipAddress);
    } else {
      runLongerLoggedInSession(ipAddress);
    }

    System.out.println("[NORMAL] " + ipAddress + " session complete");
  }

  private void runShortBrowsingSession(String ipAddress) throws Exception {
    maybeVisitHome(ipAddress);

    // Short sessions represent users who only browse a few public pages.
    int browseCount = random.nextInt(3) + 1;
    for (int i = 0; i < browseCount; i++) {
      visitRandomPublicEndpoint(ipAddress);

      // Occasionally repeat a normal action, like a real user browsing around.
      if (random.nextDouble() < 0.25) {
        repeatLastStyleAction(ipAddress);
      }

      Thread.sleep(randomDelay(1200, 4000));
    }
  }

  private void runStandardSession(String ipAddress) throws Exception {
    maybeVisitHome(ipAddress);

    boolean loggedIn = maybeLogin(ipAddress);

    // Standard sessions mix public browsing with optional logged-in activity.
    int browseCount = random.nextInt(4) + 2;
    for (int i = 0; i < browseCount; i++) {
      visitRandomPublicEndpoint(ipAddress);
      Thread.sleep(randomDelay(800, 3500));
    }

    if (loggedIn) {
      maybeVisitAuthenticatedArea(ipAddress);
      maybeSubmitContactForm(ipAddress);
      maybeLogout(ipAddress);
    }
  }

  private void runLongerLoggedInSession(String ipAddress) throws Exception {
    trafficClient.get("/", ipAddress);
    Thread.sleep(randomDelay(1000, 2500));

    boolean loggedIn = maybeLogin(ipAddress);

    // Longer sessions create a more active, but still normal, user pattern.
    int browseCount = random.nextInt(5) + 4;
    for (int i = 0; i < browseCount; i++) {
      if (loggedIn && random.nextDouble() < 0.35) {
        visitRandomAuthenticatedEndpoint(ipAddress);
      } else {
        visitRandomPublicEndpoint(ipAddress);
      }

      // Sometimes a user clicks around quickly without it being suspicious.
      if (random.nextDouble() < 0.20) {
        visitRandomPublicEndpoint(ipAddress);
      }

      Thread.sleep(randomDelay(700, 2800));
    }

    if (loggedIn) {
      maybeSubmitContactForm(ipAddress);
      maybeLogout(ipAddress);
    }
  }

  private void maybeVisitHome(String ipAddress) throws Exception {
    if (random.nextBoolean()) {
      trafficClient.get("/", ipAddress);
      Thread.sleep(randomDelay(1000, 3000));
    }
  }

  private boolean maybeLogin(String ipAddress) throws Exception {
    /*
     * Not every normal user logs in.
     * This keeps normal behaviour mixed between public and authenticated traffic.
     */
    if (random.nextDouble() > 0.35) {
      int status =
          trafficClient.post(
              "/auth/login",
              "{\"username\":\"user" + random.nextInt(10) + "\",\"password\":\"pass123\"}",
              ipAddress);

      Thread.sleep(randomDelay(900, 2500));

      return status == 200;
    }

    return false;
  }

  private void maybeVisitAuthenticatedArea(String ipAddress) throws Exception {
    int count = random.nextInt(2) + 1;

    for (int i = 0; i < count; i++) {
      visitRandomAuthenticatedEndpoint(ipAddress);
      Thread.sleep(randomDelay(900, 2200));
    }
  }

  private void maybeSubmitContactForm(String ipAddress) throws Exception {
    if (random.nextDouble() < 0.25) {
      trafficClient.post("/contact", "{\"message\":\"hello\"}", ipAddress);
      Thread.sleep(randomDelay(500, 1500));
    }
  }

  private void maybeLogout(String ipAddress) throws Exception {
    if (random.nextDouble() < 0.85) {
      trafficClient.post("/auth/logout", "{}", ipAddress, USER_TOKEN);
    }
  }

  private void visitRandomPublicEndpoint(String ipAddress) throws Exception {
    String endpoint = PUBLIC_ENDPOINTS[random.nextInt(PUBLIC_ENDPOINTS.length)];

    trafficClient.get(endpoint, ipAddress);
  }

  private void visitRandomAuthenticatedEndpoint(String ipAddress) throws Exception {
    String endpoint = AUTHENTICATED_ENDPOINTS[random.nextInt(AUTHENTICATED_ENDPOINTS.length)];

    trafficClient.get(endpoint, ipAddress, USER_TOKEN);
  }

  private void repeatLastStyleAction(String ipAddress) throws Exception {
    if (random.nextBoolean()) {
      trafficClient.get("/products", ipAddress);
    } else {
      trafficClient.get("/articles", ipAddress);
    }
  }

  private long randomDelay(int min, int max) {
    return min + random.nextInt(max - min + 1);
  }
}
