package org.example.idslog;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the most recent IDS events for the dashboard and terminal output.
 *
 * This is a lightweight in-memory event log. It keeps the dashboard console live
 * without saving every event forever so the UI stays responsive during demo runs.
 */
@Service
public class IdsEventLogService {

    private static final int MAX_EVENTS = 100;

    // Synchronized because events can be added while the dashboard is reading them.
    private final List<IdsEventLogEntry> events =
            Collections.synchronizedList(new ArrayList<>());

    public void addEvent(IdsEventLevel level, String ipAddress, Double score,
                         String message, double[] features) {
        IdsEventLogEntry entry = new IdsEventLogEntry(
                level,
                ipAddress,
                score,
                message,
                features
        );

        synchronized (events) {
            // Newest events are kept at the top for the dashboard console.
            events.add(0, entry);

            // Keep only the most recent events so the list does not grow forever.
            if (events.size() > MAX_EVENTS) {
                events.remove(events.size() - 1);
            }
        }

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