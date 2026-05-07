package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Provides simple control endpoints for the anomaly detection model.
 *
 * This controller is mainly used by the traffic generator and during demos.
 * It lets me switch the IDS into training collection mode, train the model
 * from the collected samples and check the current model state.
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    private final AnomalyDetectionService anomalyDetectionService;

    public ModelController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @GetMapping("/collect")
    public ResponseEntity<String> startCollecting() {
        // Clears any previous training data and starts collecting normal traffic samples.
        anomalyDetectionService.startCollecting();

        return ResponseEntity.ok("Collecting training data");
    }

    @GetMapping("/train")
    public ResponseEntity<String> train() {
        // Trains the model using whatever normal traffic has been collected so far.
        anomalyDetectionService.trainOnCollected();

        return ResponseEntity.ok("Model trained on real traffic");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        // Simple status output for checking whether the IDS is collecting or trained.
        return ResponseEntity.ok(Map.of(
                "trained", anomalyDetectionService.isTrained(),
                "collecting", anomalyDetectionService.isCollecting()
        ));
    }
}