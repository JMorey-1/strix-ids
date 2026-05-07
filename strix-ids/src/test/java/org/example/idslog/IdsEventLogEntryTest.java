package org.example.idslog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdsEventLogEntryTest {

    @Test
    void constructor_ShouldSetAllFields() {
        // Create feature vector
        double[] features = {5.0, 0.2, 0.1};

        // Create log entry
        IdsEventLogEntry entry = new IdsEventLogEntry(
                IdsEventLevel.ALERT,
                "10.0.0.5",
                0.76,
                "Alert: endpoint scanning behaviour detected",
                features
        );

        // Check field values
        assertNotNull(entry.getTime());
        assertTrue(entry.getTime().matches("\\d{2}:\\d{2}:\\d{2}"));
        assertEquals(IdsEventLevel.ALERT, entry.getLevel());
        assertEquals("10.0.0.5", entry.getIpAddress());
        assertEquals(0.76, entry.getScore(), 0.001);
        assertEquals("Alert: endpoint scanning behaviour detected", entry.getMessage());
        assertArrayEquals(features, entry.getFeatures(), 0.001);
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyEntry() {
        // Create empty log entry
        IdsEventLogEntry entry = new IdsEventLogEntry();

        // Check default values
        assertNull(entry.getTime());
        assertNull(entry.getLevel());
        assertNull(entry.getIpAddress());
        assertNull(entry.getScore());
        assertNull(entry.getMessage());
        assertNull(entry.getFeatures());
    }

    @Test
    void toConsoleString_WithScore_ShouldIncludeScoreAndFeatures() {
        // Create feature vector
        double[] features = {5.0, 0.2, 0.1};

        // Create log entry
        IdsEventLogEntry entry = new IdsEventLogEntry(
                IdsEventLevel.WATCH,
                "192.168.1.10",
                0.58,
                "Watch: suspicious behaviour under observation",
                features
        );

        String consoleString = entry.toConsoleString();

        // Check console output
        assertTrue(consoleString.contains("[IDS][WATCH]"));
        assertTrue(consoleString.contains("IP: 192.168.1.10"));
        assertTrue(consoleString.contains("score: 0.58"));
        assertTrue(consoleString.contains("message: Watch: suspicious behaviour under observation"));
        assertTrue(consoleString.contains("features: [5.0, 0.2, 0.1]"));
    }

    @Test
    void toConsoleString_WithNullScore_ShouldNotIncludeScoreText() {
        // Create system log entry
        IdsEventLogEntry entry = new IdsEventLogEntry(
                IdsEventLevel.SYSTEM,
                "system",
                null,
                "IDS service started",
                new double[0]
        );

        String consoleString = entry.toConsoleString();

        // Check console output
        assertTrue(consoleString.contains("[IDS][SYSTEM]"));
        assertTrue(consoleString.contains("IP: system"));
        assertFalse(consoleString.contains("score:"));
        assertTrue(consoleString.contains("message: IDS service started"));
    }
}