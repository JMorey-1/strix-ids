package org.example.mitigation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access layer for mitigation records.
 *
 * <p>Spring Data JPA creates the implementation automatically. I use this repository to look up IP
 * records, find blocked IPs and grab the records needed for the dashboard panels.
 */
public interface MitigationRecordRepository extends JpaRepository<MitigationRecord, Long> {

  Optional<MitigationRecord> findByIpAddress(String ipAddress);

  List<MitigationRecord> findByStatus(MitigationStatus status);

  List<MitigationRecord> findByStatusIn(List<MitigationStatus> statuses);
}
