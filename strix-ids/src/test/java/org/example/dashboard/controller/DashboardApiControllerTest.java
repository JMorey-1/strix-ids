package org.example.dashboard.controller;

import org.example.dashboard.model.DashboardStatus;
import org.example.dashboard.service.DashboardStateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardApiController.class)
class DashboardApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardStateService dashboardStateService;

    @Test
    void getStatus_ShouldReturnDashboardStatus() throws Exception {
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

        when(dashboardStateService.getStatus()).thenReturn(status);

        // Test status endpoint
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("Detection"))
                .andExpect(jsonPath("$.engineStatus").value("Running"))
                .andExpect(jsonPath("$.uptime").value("12m 30s"))
                .andExpect(jsonPath("$.totalRequests").value(120))
                .andExpect(jsonPath("$.activeAlerts").value(4))
                .andExpect(jsonPath("$.blockedIps").value(2))
                .andExpect(jsonPath("$.modelAlgorithm").value("Isolation Forest"))
                .andExpect(jsonPath("$.modelTrainingState").value("Trained"))
                .andExpect(jsonPath("$.modelWindowSize").value("30 seconds"))
                .andExpect(jsonPath("$.modelConfidence").value("Stable"));

        // Check service called
        verify(dashboardStateService).getStatus();
    }
}