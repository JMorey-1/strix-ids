package org.example;

import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides simple authentication endpoints for the target web application.
 *
 * <p>This controller is intentionally basic. It gives the traffic generator a login route to use
 * during normal browsing and gives Strix a way to observe failed login attempts during brute-force
 * simulations.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

  private static final Set<String> VALID_USERS =
      Set.of(
          "user0", "user1", "user2", "user3", "user4", "user5", "user6", "user7", "user8", "user9");

  private static final String USER_PASSWORD = "pass123";
  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "admin123";

  @GetMapping("/login")
  public ResponseEntity<String> loginPage() {
    // Represents loading the login page before submitting credentials.
    return ResponseEntity.ok("Login page");
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
    String username = credentials.get("username");
    String password = credentials.get("password");

    // Bad request means the client did not send the expected login fields.
    if (username == null || password == null) {
      return ResponseEntity.badRequest().body("Missing username or password");
    }

    // Normal users all share the same simple password for simulation purposes.
    if (isValidUserLogin(username, password)) {
      return ResponseEntity.ok("User login successful");
    }

    // Admin login is separate so admin traffic can be simulated too.
    if (isValidAdminLogin(username, password)) {
      return ResponseEntity.ok("Admin login successful");
    }

    // Failed logins create useful 401 signals for the IDS.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout() {
    return ResponseEntity.ok("Logged out");
  }

  private boolean isValidUserLogin(String username, String password) {
    return VALID_USERS.contains(username) && USER_PASSWORD.equals(password);
  }

  private boolean isValidAdminLogin(String username, String password) {
    return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
  }
}
