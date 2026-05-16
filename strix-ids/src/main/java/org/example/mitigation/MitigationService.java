package org.example.mitigation;

import org.example.idslog.IdsEventLevel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles the mitigation state for suspicious IP addresses.
 * This service takes WATCH and ALERT events from the IDS and builds up a
 * suspicion score for each IP. The records are saved through the repository,
 * so the dashboard can show suspicious IPs and blocked IPs from the database.
 */
@Service
public class MitigationService {

    private final MitigationRecordRepository mitigationRecordRepository;
    private final MitigationActionClient mitigationActionClient;

    public MitigationService(MitigationRecordRepository mitigationRecordRepository,
                             MitigationActionClient mitigationActionClient) {
        this.mitigationRecordRepository = mitigationRecordRepository;
        this.mitigationActionClient = mitigationActionClient;
    }

    public synchronized void processDetectionEvent(String ipAddress, IdsEventLevel level, String reason) {
        // Only suspicious or alert-level events should affect mitigation.
        if (level != IdsEventLevel.WATCH && level != IdsEventLevel.ALERT) {
            return;
        }

        // Reuse the existing record for this IP, or create one if it is new.
        MitigationRecord record = mitigationRecordRepository.findByIpAddress(ipAddress)
                .orElseGet(() -> new MitigationRecord(ipAddress));

        MitigationStatus previousStatus = record.getStatus();

        if (level == IdsEventLevel.WATCH) {
            record.registerWatch(reason);
        }

        if (level == IdsEventLevel.ALERT) {
            record.registerAlert(reason);
        }

        // Save the updated score, status and reason back to H2.
        mitigationRecordRepository.save(record);

        sendMitigationActionIfStatusChanged(record, previousStatus);
    }

    private void sendMitigationActionIfStatusChanged(MitigationRecord record,
                                                     MitigationStatus previousStatus) {
        MitigationStatus currentStatus = record.getStatus();

        if (previousStatus == currentStatus) {
            return;
        }

        if (currentStatus == MitigationStatus.SUSPICIOUS) {
            mitigationActionClient.sendRateLimitAction(record);
            return;
        }

        if (currentStatus == MitigationStatus.BLOCKED) {
            mitigationActionClient.sendBlacklistAction(record);
        }
    }

    public List<MitigationRecord> getSuspiciousRecords() {
        // The suspicious IP panel shows every IP currently being tracked.
        return mitigationRecordRepository.findByStatusIn(List.of(
                MitigationStatus.WATCH,
                MitigationStatus.SUSPICIOUS,
                MitigationStatus.BLOCKED
        ));
    }

    public List<MitigationRecord> getBlacklist() {
        // The blacklist panel only shows IPs that reached the blocked state.
        return mitigationRecordRepository.findByStatus(MitigationStatus.BLOCKED);
    }

    public void resetMitigationState() {
        // Useful before demos or test runs so old mitigation data does not interfere.
        mitigationRecordRepository.deleteAll();
    }
}