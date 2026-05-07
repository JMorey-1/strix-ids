package org.example.idslog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Represents one event in the Strix IDS log.
 *
 * These entries are used both for terminal output and the dashboard console.
 * Each entry stores the event level, IP address, anomaly score, message and
 * feature vector that produced the decision.
 */
public class IdsEventLogEntry {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private String time;
    private IdsEventLevel level;
    private String ipAddress;
    private Double score;
    private String message;
    private double[] features;

    // Needed for JSON serialisation when the dashboard reads recent events.
    public IdsEventLogEntry() {
    }

    public IdsEventLogEntry(IdsEventLevel level, String ipAddress, Double score,
                            String message, double[] features) {
        this.time = LocalDateTime.now().format(TIME_FORMATTER);
        this.level = level;
        this.ipAddress = ipAddress;
        this.score = score;
        this.message = message;
        this.features = features;
    }

    public String getTime() {
        return time;
    }

    public IdsEventLevel getLevel() {
        return level;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Double getScore() {
        return score;
    }

    public String getMessage() {
        return message;
    }

    public double[] getFeatures() {
        return features;
    }

    public String toConsoleString() {
        String scoreText = score == null ? "" : " score: " + score;

        // Keeps the terminal output close to what appears in the dashboard console.
        return "[IDS][" + level + "] IP: " + ipAddress
                + scoreText
                + " message: " + message
                + " features: " + Arrays.toString(features);
    }
}