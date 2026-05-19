package org.example.mitigation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores the mitigation state for one IP address.
 *
 * <p>Each record tracks how often an IP has produced WATCH or ALERT events, builds a suspicion
 * score over time and moves the IP through the mitigation states used by Strix.
 */
@Entity
public class MitigationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Each IP should only have one mitigation record.
  @Column(unique = true, nullable = false)
  private String ipAddress;

  private int watchCount;
  private int alertCount;
  private int suspicionScore;

  @Enumerated(EnumType.STRING)
  private MitigationStatus status;

  private String reason;

  private LocalDateTime firstSeen;
  private LocalDateTime lastSeen;
  private LocalDateTime blockedAt;
  private LocalDateTime expiresAt;

  // Required by JPA.
  public MitigationRecord() {}

  public MitigationRecord(String ipAddress) {
    this.ipAddress = ipAddress;
    this.watchCount = 0;
    this.alertCount = 0;
    this.suspicionScore = 0;
    this.status = MitigationStatus.WATCH;
    this.reason = "Initial suspicious activity observed";
    this.firstSeen = LocalDateTime.now();
    this.lastSeen = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public int getWatchCount() {
    return watchCount;
  }

  public int getAlertCount() {
    return alertCount;
  }

  public int getSuspicionScore() {
    return suspicionScore;
  }

  public MitigationStatus getStatus() {
    return status;
  }

  public String getReason() {
    return reason;
  }

  public LocalDateTime getFirstSeen() {
    return firstSeen;
  }

  public LocalDateTime getLastSeen() {
    return lastSeen;
  }

  public LocalDateTime getBlockedAt() {
    return blockedAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void registerWatch(String reason) {
    watchCount++;
    suspicionScore += 1;

    this.reason = reason;
    this.lastSeen = LocalDateTime.now();

    updateStatus();
  }

  public void registerAlert(String reason) {
    alertCount++;
    suspicionScore += 3;

    this.reason = reason;
    this.lastSeen = LocalDateTime.now();

    updateStatus();
  }

  private void updateStatus() {
    /*
     * The thresholds are deliberately simple for this prototype.
     * WATCH events increase suspicion slowly, while ALERT events increase it faster.
     */
    if (suspicionScore >= 9) {
      status = MitigationStatus.BLOCKED;

      /*
       * I only set the block time once.
       * This stops the expiry time being pushed forward on every later alert.
       */
      if (blockedAt == null) {
        blockedAt = LocalDateTime.now();
        expiresAt = blockedAt.plusMinutes(5);
      }

      return;
    }

    if (suspicionScore >= 6) {
      status = MitigationStatus.SUSPICIOUS;
      return;
    }

    status = MitigationStatus.WATCH;
  }
}
