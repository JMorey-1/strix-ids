package org.example.audit;

import org.example.idslog.IdsEventLevel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes lightweight audit logs for Strix.
 *
 * These logs are intended as simple evidence files for demos, testing and report
 * screenshots. The dashboard can still use the in-memory IDS event log, while
 * these files preserve important events after the application has stopped.
 */
@Service
public class AuditLogService {

    private static final Path LOG_DIRECTORY = Path.of("logs");
    private static final Path AUDIT_LOG = LOG_DIRECTORY.resolve("strix-audit.log");
    private static final Path ALERT_LOG = LOG_DIRECTORY.resolve("strix-alerts.log");
    private static final Path MITIGATION_LOG = LOG_DIRECTORY.resolve("strix-mitigation.log");

    private static final DateTimeFormatter LOG_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Object logLock = new Object();

    public void logIdsEvent(IdsEventLevel level,
                            String ipAddress,
                            Double score,
                            String message) {
        String line = buildLogLine(
                level.name(),
                ipAddress,
                score,
                message
        );

        writeLine(AUDIT_LOG, line);

        if (level == IdsEventLevel.WATCH || level == IdsEventLevel.ALERT) {
            writeLine(ALERT_LOG, line);
        }
    }

    public void logMitigationAction(String ipAddress,
                                    String actionType,
                                    String reason,
                                    String result) {
        String line = timestamp()
                + " | MITIGATION"
                + " | ip=" + safeValue(ipAddress)
                + " | action=" + safeValue(actionType)
                + " | result=" + safeValue(result)
                + " | reason=" + safeValue(reason);

        writeLine(AUDIT_LOG, line);
        writeLine(MITIGATION_LOG, line);
    }

    private String buildLogLine(String eventType,
                                 String ipAddress,
                                 Double score,
                                 String message) {
        return timestamp()
                + " | " + safeValue(eventType)
                + " | ip=" + safeValue(ipAddress)
                + " | score=" + safeScore(score)
                + " | message=" + safeValue(message);
    }

    private void writeLine(Path filePath, String line) {
        synchronized (logLock) {
            try {
                Files.createDirectories(LOG_DIRECTORY);
                Files.writeString(
                        filePath,
                        line + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException ignored) {
                /*
                 * Audit logging should never stop the IDS from processing traffic.
                 * If a file write fails, the live dashboard and console output still
                 * continue to provide evidence during the demo.
                 */
            }
        }
    }

    private String timestamp() {
        return LocalDateTime.now().format(LOG_TIME_FORMATTER);
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "n/a";
        }

        return value.replace(System.lineSeparator(), " ");
    }

    private String safeScore(Double score) {
        if (score == null) {
            return "n/a";
        }

        return String.valueOf(score);
    }
}