package org.example.mitigation;

/**
 * Represents the current mitigation state for an IP address.
 *
 * Strix does not immediately block every unusual request. An IP can move from
 * watch, suspicious or blocked as more alert-level behaviour builds up.
 */
public enum MitigationStatus {
    WATCH,
    SUSPICIOUS,
    BLOCKED
}