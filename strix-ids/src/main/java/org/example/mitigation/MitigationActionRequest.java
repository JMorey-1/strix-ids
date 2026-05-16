package org.example.mitigation;

/**
 * Request body sent from the IDS to the target application when Strix wants
 * the target app to enforce a mitigation action.
 */
public class MitigationActionRequest {

    private String ipAddress;
    private MitigationActionType actionType;
    private String reason;
    private long timestamp;
    private long expiresInSeconds;

    public MitigationActionRequest(String ipAddress,
                                   MitigationActionType actionType,
                                   String reason,
                                   long timestamp,
                                   long expiresInSeconds) {
        this.ipAddress = ipAddress;
        this.actionType = actionType;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public MitigationActionType getActionType() {
        return actionType;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }
}