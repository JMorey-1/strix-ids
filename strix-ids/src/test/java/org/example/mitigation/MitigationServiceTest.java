package org.example.mitigation;

import org.example.idslog.IdsEventLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MitigationServiceTest {

    @Mock
    private MitigationRecordRepository mitigationRecordRepository;

    private MitigationService mitigationService;

    @BeforeEach
    void setUp() {
        // Create service with mock repository
        mitigationService = new MitigationService(mitigationRecordRepository);
    }

    @Test
    void processDetectionEvent_WithScoreLevel_ShouldNotUpdateMitigation() {
        // Process normal score event
        mitigationService.processDetectionEvent(
                "192.168.1.10",
                IdsEventLevel.SCORE,
                "Traffic scored within expected range"
        );

        // Check repository not used
        verifyNoInteractions(mitigationRecordRepository);
    }

    @Test
    void processDetectionEvent_WithWatchLevelAndNewIp_ShouldCreateWatchRecord() {
        // Mock no existing record
        when(mitigationRecordRepository.findByIpAddress("10.0.0.5"))
                .thenReturn(Optional.empty());

        // Process watch event
        mitigationService.processDetectionEvent(
                "10.0.0.5",
                IdsEventLevel.WATCH,
                "Watch: suspicious behaviour under observation"
        );

        // Capture saved record
        ArgumentCaptor<MitigationRecord> captor =
                ArgumentCaptor.forClass(MitigationRecord.class);

        verify(mitigationRecordRepository).save(captor.capture());

        MitigationRecord savedRecord = captor.getValue();

        // Check saved record
        assertEquals("10.0.0.5", savedRecord.getIpAddress());
        assertEquals(1, savedRecord.getWatchCount());
        assertEquals(0, savedRecord.getAlertCount());
        assertEquals(1, savedRecord.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, savedRecord.getStatus());
        assertEquals("Watch: suspicious behaviour under observation", savedRecord.getReason());
        assertNotNull(savedRecord.getLastSeen());
    }

    @Test
    void processDetectionEvent_WithAlertLevelAndExistingIp_ShouldUpdateExistingRecord() {
        // Create existing record
        MitigationRecord existingRecord = new MitigationRecord("10.0.0.8");

        when(mitigationRecordRepository.findByIpAddress("10.0.0.8"))
                .thenReturn(Optional.of(existingRecord));

        // Process alert event
        mitigationService.processDetectionEvent(
                "10.0.0.8",
                IdsEventLevel.ALERT,
                "Alert: anomalous behaviour detected"
        );

        // Check existing record saved
        verify(mitigationRecordRepository).save(existingRecord);

        // Check updated values
        assertEquals(0, existingRecord.getWatchCount());
        assertEquals(1, existingRecord.getAlertCount());
        assertEquals(3, existingRecord.getSuspicionScore());
        assertEquals(MitigationStatus.WATCH, existingRecord.getStatus());
        assertEquals("Alert: anomalous behaviour detected", existingRecord.getReason());
    }

    @Test
    void getSuspiciousRecords_ShouldReturnTrackedMitigationRecords() {
        // Create records
        List<MitigationRecord> records = List.of(
                new MitigationRecord("10.0.0.1"),
                new MitigationRecord("10.0.0.2")
        );

        when(mitigationRecordRepository.findByStatusIn(List.of(
                MitigationStatus.WATCH,
                MitigationStatus.SUSPICIOUS,
                MitigationStatus.BLOCKED
        ))).thenReturn(records);

        List<MitigationRecord> result = mitigationService.getSuspiciousRecords();

        // Check records returned
        assertEquals(2, result.size());
        assertEquals("10.0.0.1", result.get(0).getIpAddress());
        assertEquals("10.0.0.2", result.get(1).getIpAddress());
    }

    @Test
    void getBlacklist_ShouldReturnBlockedRecordsOnly() {
        // Create blocked record
        MitigationRecord blockedRecord = new MitigationRecord("10.0.0.9");
        blockedRecord.registerAlert("Alert 1");
        blockedRecord.registerAlert("Alert 2");
        blockedRecord.registerAlert("Alert 3");

        when(mitigationRecordRepository.findByStatus(MitigationStatus.BLOCKED))
                .thenReturn(List.of(blockedRecord));

        List<MitigationRecord> result = mitigationService.getBlacklist();

        // Check blacklist
        assertEquals(1, result.size());
        assertEquals("10.0.0.9", result.get(0).getIpAddress());
        assertEquals(MitigationStatus.BLOCKED, result.get(0).getStatus());
    }

    @Test
    void resetMitigationState_ShouldDeleteAllRecords() {
        // Reset mitigation state
        mitigationService.resetMitigationState();

        // Check delete called
        verify(mitigationRecordRepository).deleteAll();
    }
}