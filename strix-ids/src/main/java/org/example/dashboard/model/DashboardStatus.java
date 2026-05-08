package org.example.dashboard.model;

/**
 * Represents the high-level status values shown on the dashboard.
 *
 * This includes the top system summary cards and the model status panel.
 */
public class DashboardStatus {

    private String mode;
    private String engineStatus;
    private String uptime;
    private int totalRequests;
    private int activeAlerts;
    private int blockedIps;

    private String modelAlgorithm;
    private String modelTrainingState;
    private String modelWindowSize;
    private String modelConfidence;

    // Needed for JSON serialisation.
    public DashboardStatus() {
    }

    public DashboardStatus(String mode,
                           String engineStatus,
                           String uptime,
                           int totalRequests,
                           int activeAlerts,
                           int blockedIps,
                           String modelAlgorithm,
                           String modelTrainingState,
                           String modelWindowSize,
                           String modelConfidence) {
        this.mode = mode;
        this.engineStatus = engineStatus;
        this.uptime = uptime;
        this.totalRequests = totalRequests;
        this.activeAlerts = activeAlerts;
        this.blockedIps = blockedIps;
        this.modelAlgorithm = modelAlgorithm;
        this.modelTrainingState = modelTrainingState;
        this.modelWindowSize = modelWindowSize;
        this.modelConfidence = modelConfidence;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(String engineStatus) {
        this.engineStatus = engineStatus;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(int activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public int getBlockedIps() {
        return blockedIps;
    }

    public void setBlockedIps(int blockedIps) {
        this.blockedIps = blockedIps;
    }

    public String getModelAlgorithm() {
        return modelAlgorithm;
    }

    public void setModelAlgorithm(String modelAlgorithm) {
        this.modelAlgorithm = modelAlgorithm;
    }

    public String getModelTrainingState() {
        return modelTrainingState;
    }

    public void setModelTrainingState(String modelTrainingState) {
        this.modelTrainingState = modelTrainingState;
    }

    public String getModelWindowSize() {
        return modelWindowSize;
    }

    public void setModelWindowSize(String modelWindowSize) {
        this.modelWindowSize = modelWindowSize;
    }

    public String getModelConfidence() {
        return modelConfidence;
    }

    public void setModelConfidence(String modelConfidence) {
        this.modelConfidence = modelConfidence;
    }
}