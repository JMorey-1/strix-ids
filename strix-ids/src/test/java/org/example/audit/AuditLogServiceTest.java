package org.example.audit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.example.idslog.IdsEventLevel;
import org.junit.jupiter.api.Test;

class AuditLogServiceTest {

  private static final Path AUDIT_LOG = Path.of("logs", "strix-audit.log");
  private static final Path ALERT_LOG = Path.of("logs", "strix-alerts.log");
  private static final Path MITIGATION_LOG = Path.of("logs", "strix-mitigation.log");

  @Test
  void logIdsEvent_WithAlertLevel_ShouldWriteToAuditAndAlertLogs() throws Exception {
    AuditLogService service = new AuditLogService();

    String uniqueMessage = "Test alert event " + UUID.randomUUID();

    service.logIdsEvent(IdsEventLevel.ALERT, "10.0.0.5", 0.72, uniqueMessage);

    String auditContent = Files.readString(AUDIT_LOG);
    String alertContent = Files.readString(ALERT_LOG);

    assertTrue(auditContent.contains(uniqueMessage));
    assertTrue(alertContent.contains(uniqueMessage));
  }

  @Test
  void logIdsEvent_WithScoreLevel_ShouldWriteToAuditLogOnly() throws Exception {
    AuditLogService service = new AuditLogService();

    String uniqueMessage = "Test score event " + UUID.randomUUID();

    service.logIdsEvent(IdsEventLevel.SCORE, "192.168.1.10", 0.15, uniqueMessage);

    String auditContent = Files.readString(AUDIT_LOG);

    assertTrue(auditContent.contains(uniqueMessage));
  }

  @Test
  void logMitigationAction_ShouldWriteToAuditAndMitigationLogs() throws Exception {
    AuditLogService service = new AuditLogService();

    String uniqueReason = "Test mitigation action " + UUID.randomUUID();

    service.logMitigationAction("172.16.0.1", "BLACKLIST", uniqueReason, "sent to target app");

    String auditContent = Files.readString(AUDIT_LOG);
    String mitigationContent = Files.readString(MITIGATION_LOG);

    assertTrue(auditContent.contains(uniqueReason));
    assertTrue(mitigationContent.contains(uniqueReason));
    assertTrue(mitigationContent.contains("BLACKLIST"));
    assertTrue(mitigationContent.contains("172.16.0.1"));
  }
}
