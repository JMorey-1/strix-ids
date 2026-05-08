package org.example.dashboard.service;

import org.example.AnomalyDetectionService;
import org.example.dashboard.model.DashboardStatus;
import org.example.idslog.IdsEventLevel;
import org.example.idslog.IdsEventLogEntry;
import org.example.idslog.IdsEventLogService;
import org.example.mitigation.MitigationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Provides the high-level dashboard status summary.
 *
 * The dashboard gets its main values from the live IDS event log,
 * anomaly detection service and mitigation service.
 */
@Service
public class DashboardStateService {

    private final AnomalyDetectionService anomalyDetectionService;
    private final IdsEventLogService idsEventLogService;
    private final MitigationService mitigationService;
    private final int windowSeconds;
    private final Instant startedAt;

    public DashboardStateService(AnomalyDetectionService anomalyDetectionService,
                                 IdsEventLogService idsEventLogService,
                                 MitigationService mitigationService,
                                 @Value("${strix.window.seconds:30}") int windowSeconds) {
        this.anomalyDetectionService = anomalyDetectionService;
        this.idsEventLogService = idsEventLogService;
        this.mitigationService = mitigationService;
        this.windowSeconds = windowSeconds;
        this.startedAt = Instant.now();
    }

    public DashboardStatus getStatus() {
        List<IdsEventLogEntry> recentEvents = idsEventLogService.getRecentEvents();

        int totalRequests = recentEvents.size();
        int activeAlerts = countActiveAlerts(recentEvents);
        int blockedIps = mitigationService.getBlacklist().size();

        return new DashboardStatus(
                getMode(),
                getEngineStatus(),
                formatUptime(),
                totalRequests,
                activeAlerts,
                blockedIps,
                "Isolation Forest",
                getModelTrainingState(),
                windowSeconds + "s",
                getModelConfidence(activeAlerts)
        );
    }

    private String getMode() {
        if (anomalyDetectionService.isCollecting()) {
            return "TRAINING";
        }

        if (anomalyDetectionService.isTrained()) {
            return "MONITORING";
        }

        return "WAITING";
    }

    private String getEngineStatus() {
        if (anomalyDetectionService.isCollecting()) {
            return "Collecting";
        }

        if (anomalyDetectionService.isTrained()) {
            return "Online";
        }

        return "Not Trained";
    }

    private String getModelTrainingState() {
        if (anomalyDetectionService.isCollecting()) {
            return "Collecting";
        }

        if (anomalyDetectionService.isTrained()) {
            return "Trained";
        }

        return "Waiting";
    }

    private String getModelConfidence(int activeAlerts) {
        if (!anomalyDetectionService.isTrained()) {
            return "Unavailable";
        }

        if (activeAlerts > 0) {
            return "Review";
        }

        return "Stable";
    }

    private int countActiveAlerts(List<IdsEventLogEntry> events) {
        return (int) events.stream()
                .filter(event -> event.getLevel() == IdsEventLevel.WATCH
                        || event.getLevel() == IdsEventLevel.ALERT)
                .count();
    }

    private String formatUptime() {
        long totalSeconds = Duration.between(startedAt, Instant.now()).getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}