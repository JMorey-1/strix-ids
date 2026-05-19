package org.example.idslog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.example.audit.AuditLogService;
import org.springframework.stereotype.Service;

/**
 * Stores the most recent IDS events for the dashboard and terminal output.
 *
 * <p>This is a lightweight in-memory event log. It keeps the dashboard console live without saving
 * every event forever so the UI stays responsive during demo runs.
 */
@Service
public class IdsEventLogService {

  private static final int MAX_EVENTS = 100;

  private final AuditLogService auditLogService;

  // Synchronized because events can be added while the dashboard is reading them.
  private final List<IdsEventLogEntry> events = Collections.synchronizedList(new ArrayList<>());

  public IdsEventLogService(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  public void addEvent(
      IdsEventLevel level, String ipAddress, Double score, String message, double[] features) {
    IdsEventLogEntry entry = new IdsEventLogEntry(level, ipAddress, score, message, features);

    synchronized (events) {
      // Newest events are kept at the top for the dashboard console.
      events.add(0, entry);

      // Keep only the most recent events so the list does not grow forever.
      if (events.size() > MAX_EVENTS) {
        events.remove(events.size() - 1);
      }
    }

    /*
     * Save important IDS events to lightweight text logs as well as showing
     * them in the dashboard. This gives me persistent evidence for testing
     * without moving the event log into the database.
     */
    auditLogService.logIdsEvent(level, ipAddress, score, message);

    // Still print to the terminal as this is useful while developing.
    System.out.println(entry.toConsoleString());
  }

  public List<IdsEventLogEntry> getRecentEvents() {
    synchronized (events) {
      // Return a copy so callers cannot modify the internal event list.
      return new ArrayList<>(events);
    }
  }

  public void clearEvents() {
    synchronized (events) {
      events.clear();
    }
  }
}
