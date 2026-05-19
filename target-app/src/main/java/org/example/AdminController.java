package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides simple admin endpoints for the target web application.
 *
 * <p>These routes are useful for generating admin probing behaviour during testing. They require a
 * basic hardcoded token so Strix can observe unauthorised and forbidden responses when suspicious
 * clients try to access admin areas.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

  private static final String ADMIN_TOKEN = "Bearer admin-token";

  @GetMapping
  public ResponseEntity<String> adminHome(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "Admin home");
  }

  @PostMapping
  public ResponseEntity<String> adminHomePost(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "Admin home action received");
  }

  @GetMapping("/users")
  public ResponseEntity<String> users(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "Admin user list");
  }

  @PostMapping("/users")
  public ResponseEntity<String> usersPost(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "Admin user action received");
  }

  @GetMapping("/settings")
  public ResponseEntity<String> settings(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "Admin settings");
  }

  @GetMapping("/reports")
  public ResponseEntity<String> reports(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "Admin reports");
  }

  @GetMapping("/audit")
  public ResponseEntity<String> audit(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return authorize(auth, "Admin audit log");
  }

  @PostMapping("/settings")
  public ResponseEntity<String> updateSettings(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "Admin settings updated");
  }

  @PostMapping("/reports")
  public ResponseEntity<String> generateReport(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "Admin report generation started");
  }

  @PostMapping("/audit")
  public ResponseEntity<String> auditAction(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody(required = false) String body) {
    return authorize(auth, "Admin audit action recorded");
  }

  private ResponseEntity<String> authorize(String auth, String successMessage) {
    /*
     * Missing auth returns 401.
     * This helps Strix recognise unauthorised admin probing.
     */
    if (auth == null || auth.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    /*
     * Incorrect auth returns 403.
     * This gives the IDS another useful signal for suspicious access attempts.
     */
    if (!ADMIN_TOKEN.equals(auth)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    return ResponseEntity.ok(successMessage);
  }
}
