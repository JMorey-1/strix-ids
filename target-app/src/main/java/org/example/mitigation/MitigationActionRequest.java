package org.example.mitigation;

/** Request body used when the IDS sends a mitigation action back to the target application. */
public class MitigationActionRequest {

  private String ipAddress;
  private MitigationActionType actionType;
  private String reason;
  private long timestamp;
  private long expiresInSeconds;

  public MitigationActionRequest() {
    // Required for JSON deserialisation.
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public MitigationActionType getActionType() {
    return actionType;
  }

  public void setActionType(MitigationActionType actionType) {
    this.actionType = actionType;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public void setExpiresInSeconds(long expiresInSeconds) {
    this.expiresInSeconds = expiresInSeconds;
  }
}
