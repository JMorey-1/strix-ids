package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return authorize(auth, "User dashboard");
    }

    private ResponseEntity<String> authorize(String auth, String successMessage) {
        if (auth == null || auth.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (!USER_TOKEN.equals(auth) && !ADMIN_TOKEN.equals(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        return ResponseEntity.ok(successMessage);
    }
}