package org.example.dashboard.controller;

import org.example.dashboard.model.DashboardStatus;
import org.example.dashboard.service.DashboardStateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides dashboard API endpoints for the Strix frontend.
 *
 * <p>Most of the dashboard data now comes from live IDS event and mitigation endpoints. This
 * controller only handles the general status summary used by the top dashboard cards.
 */
@RestController
public class DashboardApiController {

  private final DashboardStateService dashboardStateService;

  public DashboardApiController(DashboardStateService dashboardStateService) {
    this.dashboardStateService = dashboardStateService;
  }

  @GetMapping("/api/status")
  public DashboardStatus getStatus() {
    // Used by the top dashboard status cards.
    return dashboardStateService.getStatus();
  }
}
