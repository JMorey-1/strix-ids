package org.example.mitigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.example.idslog.IdsEventLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MitigationServiceTest {

  @Mock private MitigationRecordRepository mitigationRecordRepository;

  @Mock private MitigationActionClient mitigationActionClient;

  private MitigationService mitigationService;

  @BeforeEach
  void setUp() {
    // Create service with mock repository and mock mitigation client.
    mitigationService = new MitigationService(mitigationRecordRepository, mitigationActionClient);
  }

  @Test
  void processDetectionEvent_WithScoreLevel_ShouldNotUpdateMitigation() {
    // Process normal score event.
    mitigationService.processDetectionEvent(
        "192.168.1.10", IdsEventLevel.SCORE, "Traffic scored within expected range");

    // Check repository and mitigation client are not used.
    verifyNoInteractions(mitigationRecordRepository);
    verifyNoInteractions(mitigationActionClient);
  }

  @Test
  void processDetectionEvent_WithWatchLevelAndNewIp_ShouldCreateWatchRecord() {
    // Mock no existing record.
    when(mitigationRecordRepository.findByIpAddress("10.0.0.5")).thenReturn(Optional.empty());

    // Process watch event.
    mitigationService.processDetectionEvent(
        "10.0.0.5", IdsEventLevel.WATCH, "Watch: suspicious behaviour under observation");

    // Capture saved record.
    ArgumentCaptor<MitigationRecord> captor = ArgumentCaptor.forClass(MitigationRecord.class);

    verify(mitigationRecordRepository).save(captor.capture());

    MitigationRecord savedRecord = captor.getValue();

    // Check saved record.
    assertEquals("10.0.0.5", savedRecord.getIpAddress());
    assertEquals(1, savedRecord.getWatchCount());
    assertEquals(0, savedRecord.getAlertCount());
    assertEquals(1, savedRecord.getSuspicionScore());
    assertEquals(MitigationStatus.WATCH, savedRecord.getStatus());
    assertEquals("Watch: suspicious behaviour under observation", savedRecord.getReason());
    assertNotNull(savedRecord.getLastSeen());

    // WATCH should update dashboard state only, not send a target app action.
    verifyNoInteractions(mitigationActionClient);
  }

  @Test
  void processDetectionEvent_WithAlertLevelAndExistingIp_ShouldUpdateExistingRecord() {
    // Create existing record.
    MitigationRecord existingRecord = new MitigationRecord("10.0.0.8");

    when(mitigationRecordRepository.findByIpAddress("10.0.0.8"))
        .thenReturn(Optional.of(existingRecord));

    // Process alert event.
    mitigationService.processDetectionEvent(
        "10.0.0.8", IdsEventLevel.ALERT, "Alert: anomalous behaviour detected");

    // Check existing record saved.
    verify(mitigationRecordRepository).save(existingRecord);

    // Check updated values.
    assertEquals(0, existingRecord.getWatchCount());
    assertEquals(1, existingRecord.getAlertCount());
    assertEquals(3, existingRecord.getSuspicionScore());
    assertEquals(MitigationStatus.WATCH, existingRecord.getStatus());
    assertEquals("Alert: anomalous behaviour detected", existingRecord.getReason());

    // This first alert does not yet escalate far enough to contact the target app.
    verifyNoInteractions(mitigationActionClient);
  }

  @Test
  void processDetectionEvent_WhenIpBecomesSuspicious_ShouldSendRateLimitAction() {
    // Create an existing record close to the suspicious threshold.
    MitigationRecord existingRecord = new MitigationRecord("10.0.0.6");
    existingRecord.registerAlert("Previous alert");

    when(mitigationRecordRepository.findByIpAddress("10.0.0.6"))
        .thenReturn(Optional.of(existingRecord));

    // Process another alert so the status moves from WATCH to SUSPICIOUS.
    mitigationService.processDetectionEvent(
        "10.0.0.6", IdsEventLevel.ALERT, "Alert: repeated suspicious behaviour");

    verify(mitigationRecordRepository).save(existingRecord);
    assertEquals(MitigationStatus.SUSPICIOUS, existingRecord.getStatus());

    verify(mitigationActionClient).sendRateLimitAction(existingRecord);
    verify(mitigationActionClient, never()).sendBlacklistAction(any());
  }

  @Test
  void processDetectionEvent_WhenIpBecomesBlocked_ShouldSendBlacklistAction() {
    // Create an existing record close to the blocked threshold.
    MitigationRecord existingRecord = new MitigationRecord("10.0.0.9");
    existingRecord.registerAlert("Previous alert 1");
    existingRecord.registerAlert("Previous alert 2");

    when(mitigationRecordRepository.findByIpAddress("10.0.0.9"))
        .thenReturn(Optional.of(existingRecord));

    // Process another alert so the status moves from SUSPICIOUS to BLOCKED.
    mitigationService.processDetectionEvent(
        "10.0.0.9", IdsEventLevel.ALERT, "Alert: persistent anomalous behaviour");

    verify(mitigationRecordRepository).save(existingRecord);
    assertEquals(MitigationStatus.BLOCKED, existingRecord.getStatus());

    verify(mitigationActionClient).sendBlacklistAction(existingRecord);
    verify(mitigationActionClient, never()).sendRateLimitAction(any());
  }

  @Test
  void getSuspiciousRecords_ShouldReturnTrackedMitigationRecords() {
    // Create records.
    List<MitigationRecord> records =
        List.of(new MitigationRecord("10.0.0.1"), new MitigationRecord("10.0.0.2"));

    when(mitigationRecordRepository.findByStatusIn(
            List.of(MitigationStatus.WATCH, MitigationStatus.SUSPICIOUS, MitigationStatus.BLOCKED)))
        .thenReturn(records);

    List<MitigationRecord> result = mitigationService.getSuspiciousRecords();

    // Check records returned.
    assertEquals(2, result.size());
    assertEquals("10.0.0.1", result.get(0).getIpAddress());
    assertEquals("10.0.0.2", result.get(1).getIpAddress());
  }

  @Test
  void getBlacklist_ShouldReturnBlockedRecordsOnly() {
    // Create blocked record.
    MitigationRecord blockedRecord = new MitigationRecord("10.0.0.9");
    blockedRecord.registerAlert("Alert 1");
    blockedRecord.registerAlert("Alert 2");
    blockedRecord.registerAlert("Alert 3");

    when(mitigationRecordRepository.findByStatus(MitigationStatus.BLOCKED))
        .thenReturn(List.of(blockedRecord));

    List<MitigationRecord> result = mitigationService.getBlacklist();

    // Check blacklist.
    assertEquals(1, result.size());
    assertEquals("10.0.0.9", result.get(0).getIpAddress());
    assertEquals(MitigationStatus.BLOCKED, result.get(0).getStatus());
  }

  @Test
  void resetMitigationState_ShouldDeleteAllRecords() {
    // Reset mitigation state.
    mitigationService.resetMitigationState();

    // Check delete called.
    verify(mitigationRecordRepository).deleteAll();
    verifyNoInteractions(mitigationActionClient);
  }
}
