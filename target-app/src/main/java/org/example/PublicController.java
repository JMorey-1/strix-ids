package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Provides normal public endpoints for the target web application.
 *
 * These routes give the traffic generator realistic pages and actions to browse.
 * They help Strix learn what ordinary application traffic looks like before it
 * starts detecting suspicious behaviour.
 */
@RestController
public class PublicController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/products")
    public ResponseEntity<List<String>> products() {
        // Simple product listing used by normal browsing traffic.
        return ResponseEntity.ok(List.of("Product A", "Product B", "Product C"));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<String> product(@PathVariable String id) {
        return ResponseEntity.ok("Product " + id);
    }

    @GetMapping("/articles")
    public ResponseEntity<List<String>> articles() {
        // Gives normal users another public area to browse.
        return ResponseEntity.ok(List.of("Article 1", "Article 2", "Article 3"));
    }

    @PostMapping("/contact")
    public ResponseEntity<String> contact(@RequestBody Map<String, String> body) {
        // A normal POST request that is not related to login attempts.
        return ResponseEntity.ok("Message received");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/api/data")
    public ResponseEntity<Map<String, Object>> data() {
        // Small API-style endpoint used as part of normal application traffic.
        return ResponseEntity.ok(Map.of("status", "ok", "count", 42));
    }
}