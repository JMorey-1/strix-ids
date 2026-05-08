package org.example.mitigation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes the mitigation data used by the dashboard.
 *
 * This controller lets the frontend read suspicious IPs, read current
 * blacklist and clear mitigation state before a fresh demo or test run.
 */
@RestController
@RequestMapping("/api")
public class MitigationController {

    private final MitigationService mitigationService;

    public MitigationController(MitigationService mitigationService) {
        this.mitigationService = mitigationService;
    }

    @GetMapping("/suspicious-ips")
    public List<MitigationRecord> getSuspiciousIps() {
        // Used by the Suspicious IPs panel on the dashboard.
        return mitigationService.getSuspiciousRecords();
    }

    @GetMapping("/blacklist")
    public List<MitigationRecord> getBlacklist() {
        // Used by the Blacklist panel on the dashboard.
        return mitigationService.getBlacklist();
    }

    @PostMapping("/mitigation/reset")
    public ResponseEntity<Void> resetMitigationState() {
        // Clears old mitigation data so a new run starts cleanly.
        mitigationService.resetMitigationState();

        return ResponseEntity.noContent().build();
    }
}