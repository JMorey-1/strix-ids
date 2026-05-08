package org.example.mitigation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MitigationRecordTest {

    @Test
    void constructor_ShouldCreateWatchRecordForIpAddress() {
        // Create mitigation record
        MitigationRecord record = new MitigationRecord("10.0.0.5");

        // Check initial values
        assertNull(record.getId());
        assertEquals("10.0.0.5", record.getIpAddress());
        assertEquals(0, record.getWatchCount());
        assertEquals(0, record.getAlertCount());
        assertEquals(0, record.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, record.getStatus());
        assertEquals("Initial suspicious activity observed", record.getReason());
        assertNotNull(record.getFirstSeen());
        assertNotNull(record.getLastSeen());
        assertNull(record.getBlockedAt());
        assertNull(record.getExpiresAt());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyRecord() {
        // Create empty record
        MitigationRecord record = new MitigationRecord();

        // Check default values
        assertNull(record.getId());
        assertNull(record.getIpAddress());
        assertEquals(0, record.getWatchCount());
        assertEquals(0, record.getAlertCount());
        assertEquals(0, record.getSuspicionScore());
        assertNull(record.getStatus());
        assertNull(record.getReason());
        assertNull(record.getFirstSeen());
        assertNull(record.getLastSeen());
        assertNull(record.getBlockedAt());
        assertNull(record.getExpiresAt());
    }

    @Test
    void registerWatch_ShouldIncreaseWatchCountAndSuspicionScore() {
        // Create mitigation record
        MitigationRecord record = new MitigationRecord("10.0.0.5");

        // Register watch event
        record.registerWatch("Watch: suspicious behaviour under observation");

        // Check updated values
        assertEquals(1, record.getWatchCount());
        assertEquals(0, record.getAlertCount());
        assertEquals(1, record.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, record.getStatus());
        assertEquals("Watch: suspicious behaviour under observation", record.getReason());
        assertNotNull(record.getLastSeen());
    }

    @Test
    void registerAlert_WhenScoreReachesSix_ShouldSetSuspiciousStatus() {
        // Create mitigation record
        MitigationRecord record = new MitigationRecord("10.0.0.5");

        // Register two alerts
        record.registerAlert("Alert: anomalous behaviour detected");
        record.registerAlert("Alert: anomalous behaviour detected");

        // Check suspicious state
        assertEquals(2, record.getAlertCount());
        assertEquals(6, record.getSuspicionScore());
        assertEquals(MitigationStatus.SUSPICIOUS, record.getStatus());
        assertNull(record.getBlockedAt());
        assertNull(record.getExpiresAt());
    }

    @Test
    void registerAlert_WhenScoreReachesNine_ShouldSetBlockedStatus() {
        // Create mitigation record
        MitigationRecord record = new MitigationRecord("10.0.0.5");

        // Register three alerts
        record.registerAlert("Alert: anomalous behaviour detected");
        record.registerAlert("Alert: anomalous behaviour detected");
        record.registerAlert("Alert: anomalous behaviour detected");

        // Check blocked state
        assertEquals(3, record.getAlertCount());
        assertEquals(9, record.getSuspicionScore());
        assertEquals(MitigationStatus.BLOCKED, record.getStatus());
        assertNotNull(record.getBlockedAt());
        assertNotNull(record.getExpiresAt());
        assertTrue(record.getExpiresAt().isAfter(record.getBlockedAt()));
    }

    @Test
    void registerAlert_WhenAlreadyBlocked_ShouldKeepOriginalBlockedAtTime() {
        // Create mitigation record
        MitigationRecord record = new MitigationRecord("10.0.0.5");

        // Block record
        record.registerAlert("Alert 1");
        record.registerAlert("Alert 2");
        record.registerAlert("Alert 3");

        LocalDateTime originalBlockedAt = record.getBlockedAt();

        // Register another alert
        record.registerAlert("Alert 4");

        // Check block time not reset
        assertEquals(MitigationStatus.BLOCKED, record.getStatus());
        assertEquals(originalBlockedAt, record.getBlockedAt());
        assertEquals(12, record.getSuspicionScore());
    }
}