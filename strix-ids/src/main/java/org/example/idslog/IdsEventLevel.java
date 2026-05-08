package org.example.idslog;

/**
 * Represents the different types of events Strix can show in the IDS log.
 *
 * These levels are used by the backend event log and the dashboard console.
 * They make it easier to separate normal scoring, suspicious behaviour,
 * alerts and system-level messages.
 */
public enum IdsEventLevel {
    COLLECT,
    SCORE,
    WATCH,
    ALERT,
    WAITING,
    SYSTEM
}