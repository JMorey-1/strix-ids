
package org.example.mitigation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MitigationRecordTest {

    @Test
    void constructor_ShouldCreateWatchRecordForIpAddress() {
        // Create mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

        // Check initial values
        assertNull(mitigationRecord.getId());
        assertEquals("10.0.0.5", mitigationRecord.getIpAddress());
        assertEquals(0, mitigationRecord.getWatchCount());
        assertEquals(0, mitigationRecord.getAlertCount());
        assertEquals(0, mitigationRecord.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, mitigationRecord.getStatus());
        assertEquals("Initial suspicious activity observed", mitigationRecord.getReason());
        assertNotNull(mitigationRecord.getFirstSeen());
        assertNotNull(mitigationRecord.getLastSeen());
        assertNull(mitigationRecord.getBlockedAt());
        assertNull(mitigationRecord.getExpiresAt());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyRecord() {
        // Create empty mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord();

        // Check default values
        assertNull(mitigationRecord.getId());
        assertNull(mitigationRecord.getIpAddress());
        assertEquals(0, mitigationRecord.getWatchCount());
        assertEquals(0, mitigationRecord.getAlertCount());
        assertEquals(0, mitigationRecord.getSuspicionScore());
        assertNull(mitigationRecord.getStatus());
        assertNull(mitigationRecord.getReason());
        assertNull(mitigationRecord.getFirstSeen());
        assertNull(mitigationRecord.getLastSeen());
        assertNull(mitigationRecord.getBlockedAt());
        assertNull(mitigationRecord.getExpiresAt());
    }

    @Test
    void registerWatch_ShouldIncreaseWatchCountAndSuspicionScore() {
        // Create mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

        // Register watch event
        mitigationRecord.registerWatch("Watch: suspicious behaviour under observation");

        // Check updated values
        assertEquals(1, mitigationRecord.getWatchCount());
        assertEquals(0, mitigationRecord.getAlertCount());
        assertEquals(1, mitigationRecord.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, mitigationRecord.getStatus());
        assertEquals("Watch: suspicious behaviour under observation", mitigationRecord.getReason());
        assertNotNull(mitigationRecord.getLastSeen());
    }

    @Test
    void registerAlert_WhenScoreReachesSix_ShouldSetSuspiciousStatus() {
        // Create mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

        // Register two alerts
        mitigationRecord.registerAlert("Alert: anomalous behaviour detected");
        mitigationRecord.registerAlert("Alert: anomalous behaviour detected");

        // Check suspicious state
        assertEquals(2, mitigationRecord.getAlertCount());
        assertEquals(6, mitigationRecord.getSuspicionScore());
        assertEquals(MitigationStatus.SUSPICIOUS, mitigationRecord.getStatus());
        assertNull(mitigationRecord.getBlockedAt());
        assertNull(mitigationRecord.getExpiresAt());
    }

    @Test
    void registerAlert_WhenScoreReachesNine_ShouldSetBlockedStatus() {
        // Create mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

        // Register three alerts
        mitigationRecord.registerAlert("Alert: anomalous behaviour detected");
        mitigationRecord.registerAlert("Alert: anomalous behaviour detected");
        mitigationRecord.registerAlert("Alert: anomalous behaviour detected");

        // Check blocked state
        assertEquals(3, mitigationRecord.getAlertCount());
        assertEquals(9, mitigationRecord.getSuspicionScore());
        assertEquals(MitigationStatus.BLOCKED, mitigationRecord.getStatus());
        assertNotNull(mitigationRecord.getBlockedAt());
        assertNotNull(mitigationRecord.getExpiresAt());
        assertTrue(mitigationRecord.getExpiresAt().isAfter(mitigationRecord.getBlockedAt()));
    }

    @Test
    void registerAlert_WhenAlreadyBlocked_ShouldKeepOriginalBlockedAtTime() {
        // Create mitigation record
        MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

        // Block mitigation record
        mitigationRecord.registerAlert("Alert 1");
        mitigationRecord.registerAlert("Alert 2");
        mitigationRecord.registerAlert("Alert 3");

        LocalDateTime originalBlockedAt = mitigationRecord.getBlockedAt();

        // Register another alert
        mitigationRecord.registerAlert("Alert 4");

        // Check block time not reset
        assertEquals(MitigationStatus.BLOCKED, mitigationRecord.getStatus());
        assertEquals(originalBlockedAt, mitigationRecord.getBlockedAt());
        assertEquals(12, mitigationRecord.getSuspicionScore());
    }
}