package org.example;

import org.example.detection.DetectionClassificationService;
import org.example.detection.DetectionResult;
import org.example.idslog.IdsEventLevel;
import org.example.idslog.IdsEventLogService;
import org.example.mitigation.MitigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives request events from the target web application.
 *
 * This controller is the main entry point into the IDS. It takes each incoming
 * request event, turns it into a feature vector, then either collects it for
 * training or scores it against the trained model.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final FeatureExtractionService featureExtractionService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final DetectionClassificationService detectionClassificationService;
    private final IdsEventLogService idsEventLogService;
    private final MitigationService mitigationService;

    public EventController(FeatureExtractionService featureExtractionService,
                           AnomalyDetectionService anomalyDetectionService,
                           DetectionClassificationService detectionClassificationService,
                           IdsEventLogService idsEventLogService,
                           MitigationService mitigationService) {
        this.featureExtractionService = featureExtractionService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.detectionClassificationService = detectionClassificationService;
        this.idsEventLogService = idsEventLogService;
        this.mitigationService = mitigationService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> receiveEvent(@RequestBody RequestEvent event) {
        // Every request is converted into the current behaviour profile for this IP.
        double[] features = featureExtractionService.extractFeatures(event.getIp(), event);

        if (anomalyDetectionService.isCollecting()) {
            anomalyDetectionService.collect(features);

            // During training, still log the samples so the dashboard shows some activity.
            idsEventLogService.addEvent(
                    IdsEventLevel.COLLECT,
                    event.getIp(),
                    null,
                    "Collecting training sample",
                    features
            );

            return ResponseEntity.ok().build();
        }

        if (anomalyDetectionService.isTrained()) {
            double score = anomalyDetectionService.score(features);

            // The classifier turns the raw model score into SCORE, WATCH or ALERT.
            DetectionResult result = detectionClassificationService.classify(score, features);

            idsEventLogService.addEvent(
                    result.getLevel(),
                    event.getIp(),
                    result.getScore(),
                    result.getMessage(),
                    result.getFeatures()
            );

            // Only WATCH and ALERT events affect mitigation state.
            mitigationService.processDetectionEvent(
                    event.getIp(),
                    result.getLevel(),
                    result.getMessage()
            );

            return ResponseEntity.ok().build();
        }

        // This covers events arriving before training has started or completed.
        idsEventLogService.addEvent(
                IdsEventLevel.WAITING,
                event.getIp(),
                null,
                "Event received but model is not trained",
                features
        );

        return ResponseEntity.ok().build();
    }
}