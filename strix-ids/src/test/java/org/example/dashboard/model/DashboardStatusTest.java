package org.example.dashboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardStatusTest {

    @Test
    void constructor_ShouldSetAllFields() {
        // Create dashboard status
        DashboardStatus status = new DashboardStatus(
                "Detection",
                "Running",
                "12m 30s",
                120,
                4,
                2,
                "Isolation Forest",
                "Trained",
                "30 seconds",
                "Stable"
        );

        // Check field values
        assertEquals("Detection", status.getMode());
        assertEquals("Running", status.getEngineStatus());
        assertEquals("12m 30s", status.getUptime());
        assertEquals(120, status.getTotalRequests());
        assertEquals(4, status.getActiveAlerts());
        assertEquals(2, status.getBlockedIps());
        assertEquals("Isolation Forest", status.getModelAlgorithm());
        assertEquals("Trained", status.getModelTrainingState());
        assertEquals("30 seconds", status.getModelWindowSize());
        assertEquals("Stable", status.getModelConfidence());
    }

    @Test
    void setters_ShouldUpdateAllFields() {
        // Create empty status
        DashboardStatus status = new DashboardStatus();

        // Set field values
        status.setMode("Training");
        status.setEngineStatus("Collecting");
        status.setUptime("5m 10s");
        status.setTotalRequests(50);
        status.setActiveAlerts(1);
        status.setBlockedIps(0);
        status.setModelAlgorithm("Isolation Forest");
        status.setModelTrainingState("Collecting training data");
        status.setModelWindowSize("30 seconds");
        status.setModelConfidence("Pending");

        // Check updated values
        assertEquals("Training", status.getMode());
        assertEquals("Collecting", status.getEngineStatus());
        assertEquals("5m 10s", status.getUptime());
        assertEquals(50, status.getTotalRequests());
        assertEquals(1, status.getActiveAlerts());
        assertEquals(0, status.getBlockedIps());
        assertEquals("Isolation Forest", status.getModelAlgorithm());
        assertEquals("Collecting training data", status.getModelTrainingState());
        assertEquals("30 seconds", status.getModelWindowSize());
        assertEquals("Pending", status.getModelConfidence());
    }
}