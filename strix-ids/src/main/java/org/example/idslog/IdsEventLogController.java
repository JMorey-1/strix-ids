package org.example.idslog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes recent IDS events to the dashboard.
 *
 * The dashboard uses this controller to fill the live console panel.
 */
@RestController
public class IdsEventLogController {

    private final IdsEventLogService idsEventLogService;

    public IdsEventLogController(IdsEventLogService idsEventLogService) {
        this.idsEventLogService = idsEventLogService;
    }

    @GetMapping("/api/ids-events")
    public List<IdsEventLogEntry> getRecentEvents() {
        // Used by the Live IDS Console panel.
        return idsEventLogService.getRecentEvents();
    }
}