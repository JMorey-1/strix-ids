package org.example.mitigation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives mitigation actions from the Strix IDS and applies them inside the target application.
 */
@RestController
@RequestMapping("/internal/mitigation")
public class TargetMitigationController {

    private final TargetMitigationService targetMitigationService;

    public TargetMitigationController(TargetMitigationService targetMitigationService) {
        this.targetMitigationService = targetMitigationService;
    }

    @PostMapping("/actions")
    public ResponseEntity<String> receiveMitigationAction(@RequestBody MitigationActionRequest request) {
        if (request == null || request.getIpAddress() == null || request.getActionType() == null) {
            return ResponseEntity.badRequest().body("Missing mitigation action details");
        }

        targetMitigationService.applyMitigation(request);

        return ResponseEntity.ok("Mitigation action applied");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetMitigations() {
        targetMitigationService.clearMitigations();

        return ResponseEntity.ok("Target mitigation state reset");
    }
}