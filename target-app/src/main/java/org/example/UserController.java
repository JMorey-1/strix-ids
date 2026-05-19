package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides simple protected user endpoints for the target web application.
 *
 * <p>These routes give the traffic generator user-specific pages to access. They require a basic
 * hardcoded token so Strix can observe unauthorised and forbidden responses when suspicious clients
 * try to access user areas.
 */
@RestController
@RequestMapping("/user")
public class UserController {

  private static final String USER_TOKEN = "Bearer user-token";
  private static final String ADMIN_TOKEN = "Bearer admin-token";

  @GetMapping("/profile")
  public ResponseEntity<String> profile(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "User profile");
  }

  @PostMapping("/profile")
  public ResponseEntity<String> updateProfile(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "User profile updated");
  }

  @GetMapping("/dashboard")
  public ResponseEntity<String> dashboard(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "User dashboard");
  }

  @PostMapping("/dashboard")
  public ResponseEntity<String> dashboardAction(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "User dashboard action received");
  }

  private ResponseEntity<String> authorize(String auth, String successMessage) {
    /*
     * Missing auth returns 401.
     * This helps Strix recognise unauthorised user area access.
     */
    if (auth == null || auth.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    /*
     * Incorrect auth returns 403.
     * Admin auth is also allowed so admin-style traffic can access user areas.
     */
    if (!USER_TOKEN.equals(auth) && !ADMIN_TOKEN.equals(auth)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    return ResponseEntity.ok(successMessage);
  }
}
