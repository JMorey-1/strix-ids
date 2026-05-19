package org.example.mitigation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/*
 * This is a small JPA repository test rather than a pure unit test.
 * It uses @DataJpaTest with H2 to check that Spring Data can save
 * MitigationRecord entities and then run the derived query methods correctly.
 */

@DataJpaTest
class MitigationRecordRepositoryTest {

  @Autowired private MitigationRecordRepository mitigationRecordRepository;

  @Test
  void findByIpAddress_ShouldReturnMatchingRecord() {
    // Save record
    MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");
    mitigationRecordRepository.save(mitigationRecord);

    // Find by IP
    Optional<MitigationRecord> result = mitigationRecordRepository.findByIpAddress("10.0.0.5");

    // Check result
    assertTrue(result.isPresent());
    assertEquals("10.0.0.5", result.get().getIpAddress());
  }

  @Test
  void findByStatus_ShouldReturnBlockedRecordsOnly() {
    // Save watch record
    MitigationRecord watchRecord = new MitigationRecord("10.0.0.1");
    mitigationRecordRepository.save(watchRecord);

    // Save blocked record
    MitigationRecord blockedRecord = new MitigationRecord("10.0.0.2");
    blockedRecord.registerAlert("Alert 1");
    blockedRecord.registerAlert("Alert 2");
    blockedRecord.registerAlert("Alert 3");
    mitigationRecordRepository.save(blockedRecord);

    // Find blocked records
    List<MitigationRecord> result =
        mitigationRecordRepository.findByStatus(MitigationStatus.BLOCKED);

    // Check result
    assertEquals(1, result.size());
    assertEquals("10.0.0.2", result.get(0).getIpAddress());
    assertEquals(MitigationStatus.BLOCKED, result.get(0).getStatus());
  }

  @Test
  void findByStatusIn_ShouldReturnMatchingStatuses() {
    // Save watch record
    MitigationRecord watchRecord = new MitigationRecord("10.0.0.1");
    mitigationRecordRepository.save(watchRecord);

    // Save suspicious record
    MitigationRecord suspiciousRecord = new MitigationRecord("10.0.0.2");
    suspiciousRecord.registerAlert("Alert 1");
    suspiciousRecord.registerAlert("Alert 2");
    mitigationRecordRepository.save(suspiciousRecord);

    // Find watch and suspicious records
    List<MitigationRecord> result =
        mitigationRecordRepository.findByStatusIn(
            List.of(MitigationStatus.WATCH, MitigationStatus.SUSPICIOUS));

    // Check result
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(r -> r.getIpAddress().equals("10.0.0.1")));
    assertTrue(result.stream().anyMatch(r -> r.getIpAddress().equals("10.0.0.2")));
  }
}
