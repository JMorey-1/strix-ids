package org.example.idslog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IdsEventLogServiceTest {

    @Test
    void addEvent_ShouldStoreEvent() {
        // Create service
        IdsEventLogService service = new IdsEventLogService();

        // Add event
        service.addEvent(
                IdsEventLevel.ALERT,
                "10.0.0.5",
                0.72,
                "Alert: endpoint scanning behaviour detected",
                new double[]{5.0, 0.2, 0.1}
        );

        List<IdsEventLogEntry> events = service.getRecentEvents();

        // Check stored event
        assertEquals(1, events.size());
        assertEquals(IdsEventLevel.ALERT, events.get(0).getLevel());
        assertEquals("10.0.0.5", events.get(0).getIpAddress());
        assertEquals(0.72, events.get(0).getScore(), 0.001);
        assertEquals("Alert: endpoint scanning behaviour detected", events.get(0).getMessage());
    }

    @Test
    void addEvent_ShouldStoreNewestEventFirst() {
        // Create service
        IdsEventLogService service = new IdsEventLogService();

        // Add older event
        service.addEvent(
                IdsEventLevel.SCORE,
                "192.168.1.1",
                0.20,
                "Traffic scored within expected range",
                new double[]{1.0}
        );

        // Add newer event
        service.addEvent(
                IdsEventLevel.WATCH,
                "10.0.0.2",
                0.58,
                "Watch: suspicious behaviour under observation",
                new double[]{2.0}
        );

        List<IdsEventLogEntry> events = service.getRecentEvents();

        // Check newest first
        assertEquals(2, events.size());
        assertEquals(IdsEventLevel.WATCH, events.get(0).getLevel());
        assertEquals("10.0.0.2", events.get(0).getIpAddress());
        assertEquals(IdsEventLevel.SCORE, events.get(1).getLevel());
        assertEquals("192.168.1.1", events.get(1).getIpAddress());
    }

    @Test
    void addEvent_WhenMoreThanMaxEvents_ShouldKeepOnlyMostRecentOneHundred() {
        // Create service
        IdsEventLogService service = new IdsEventLogService();

        // Add more than max events
        for (int i = 1; i <= 105; i++) {
            service.addEvent(
                    IdsEventLevel.SCORE,
                    "192.168.1." + i,
                    0.10,
                    "Event " + i,
                    new double[]{i}
            );
        }

        List<IdsEventLogEntry> events = service.getRecentEvents();

        // Check max size
        assertEquals(100, events.size());

        // Check newest kept
        assertEquals("Event 105", events.get(0).getMessage());

        // Check oldest removed
        assertEquals("Event 6", events.get(99).getMessage());
    }

    @Test
    void getRecentEvents_ShouldReturnCopyOfEventList() {
        // Create service
        IdsEventLogService service = new IdsEventLogService();

        // Add event
        service.addEvent(
                IdsEventLevel.SYSTEM,
                "system",
                null,
                "IDS service started",
                new double[0]
        );

        List<IdsEventLogEntry> events = service.getRecentEvents();

        // Modify returned copy
        events.clear();

        // Check internal list unchanged
        assertEquals(1, service.getRecentEvents().size());
    }

    @Test
    void clearEvents_ShouldRemoveAllEvents() {
        // Create service
        IdsEventLogService service = new IdsEventLogService();

        // Add event
        service.addEvent(
                IdsEventLevel.ALERT,
                "10.0.0.9",
                0.80,
                "Alert: anomalous behaviour detected",
                new double[]{5.0}
        );

        // Clear events
        service.clearEvents();

        // Check empty log
        assertTrue(service.getRecentEvents().isEmpty());
    }
}