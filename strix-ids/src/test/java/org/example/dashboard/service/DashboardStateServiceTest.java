package org.example.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.AnomalyDetectionService;
import org.example.dashboard.model.DashboardStatus;
import org.example.idslog.IdsEventLevel;
import org.example.idslog.IdsEventLogEntry;
import org.example.idslog.IdsEventLogService;
import org.example.mitigation.MitigationRecord;
import org.example.mitigation.MitigationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardStateServiceTest {

  @Mock private AnomalyDetectionService anomalyDetectionService;

  @Mock private IdsEventLogService idsEventLogService;

  @Mock private MitigationService mitigationService;

  private DashboardStateService dashboardStateService;

  @BeforeEach
  void setUp() {
    // Create service
    dashboardStateService =
        new DashboardStateService(
            anomalyDetectionService, idsEventLogService, mitigationService, 30);
  }

  @Test
  void getStatus_WhenModelIsNotTrained_ShouldReturnWaitingStatus() {
    // Set service state
    when(anomalyDetectionService.isCollecting()).thenReturn(false);
    when(anomalyDetectionService.isTrained()).thenReturn(false);
    when(idsEventLogService.getRecentEvents()).thenReturn(List.of(event(IdsEventLevel.SCORE)));
    when(mitigationService.getBlacklist()).thenReturn(List.of());

    DashboardStatus status = dashboardStateService.getStatus();

    // Check dashboard status
    assertEquals("WAITING", status.getMode());
    assertEquals("Not Trained", status.getEngineStatus());
    assertEquals("Waiting", status.getModelTrainingState());
    assertEquals("Unavailable", status.getModelConfidence());
    assertEquals("Isolation Forest", status.getModelAlgorithm());
    assertEquals("30s", status.getModelWindowSize());
    assertEquals(1, status.getTotalRequests());
    assertEquals(0, status.getActiveAlerts());
    assertEquals(0, status.getBlockedIps());
    assertTrue(status.getUptime().matches("\\d{2}:\\d{2}:\\d{2}"));
  }

  @Test
  void getStatus_WhenModelIsCollecting_ShouldReturnTrainingStatus() {
    // Set service state
    when(anomalyDetectionService.isCollecting()).thenReturn(true);
    when(anomalyDetectionService.isTrained()).thenReturn(false);
    when(idsEventLogService.getRecentEvents()).thenReturn(List.of());
    when(mitigationService.getBlacklist()).thenReturn(List.of());

    DashboardStatus status = dashboardStateService.getStatus();

    // Check dashboard status
    assertEquals("TRAINING", status.getMode());
    assertEquals("Collecting", status.getEngineStatus());
    assertEquals("Collecting", status.getModelTrainingState());
    assertEquals("Unavailable", status.getModelConfidence());
    assertEquals(0, status.getTotalRequests());
    assertEquals(0, status.getActiveAlerts());
    assertEquals(0, status.getBlockedIps());
  }

  @Test
  void getStatus_WhenModelIsTrainedWithNoAlerts_ShouldReturnStableStatus() {
    // Set service state
    when(anomalyDetectionService.isCollecting()).thenReturn(false);
    when(anomalyDetectionService.isTrained()).thenReturn(true);
    when(idsEventLogService.getRecentEvents())
        .thenReturn(List.of(event(IdsEventLevel.SCORE), event(IdsEventLevel.COLLECT)));
    when(mitigationService.getBlacklist()).thenReturn(List.of(new MitigationRecord("10.0.0.5")));

    DashboardStatus status = dashboardStateService.getStatus();

    // Check dashboard status
    assertEquals("MONITORING", status.getMode());
    assertEquals("Online", status.getEngineStatus());
    assertEquals("Trained", status.getModelTrainingState());
    assertEquals("Stable", status.getModelConfidence());
    assertEquals(2, status.getTotalRequests());
    assertEquals(0, status.getActiveAlerts());
    assertEquals(1, status.getBlockedIps());
  }

  @Test
  void getStatus_WhenModelIsTrainedWithWatchAndAlertEvents_ShouldReturnReviewStatus() {
    // Set service state
    when(anomalyDetectionService.isCollecting()).thenReturn(false);
    when(anomalyDetectionService.isTrained()).thenReturn(true);
    when(idsEventLogService.getRecentEvents())
        .thenReturn(
            List.of(
                event(IdsEventLevel.SCORE),
                event(IdsEventLevel.WATCH),
                event(IdsEventLevel.ALERT)));
    when(mitigationService.getBlacklist())
        .thenReturn(List.of(new MitigationRecord("10.0.0.5"), new MitigationRecord("10.0.0.6")));

    DashboardStatus status = dashboardStateService.getStatus();

    // Check dashboard status
    assertEquals("MONITORING", status.getMode());
    assertEquals("Online", status.getEngineStatus());
    assertEquals("Trained", status.getModelTrainingState());
    assertEquals("Review", status.getModelConfidence());
    assertEquals(3, status.getTotalRequests());
    assertEquals(2, status.getActiveAlerts());
    assertEquals(2, status.getBlockedIps());
  }

  private IdsEventLogEntry event(IdsEventLevel level) {
    // Create log event
    return new IdsEventLogEntry(level, "10.0.0.1", 0.50, "Test event", new double[] {1.0});
  }
}
