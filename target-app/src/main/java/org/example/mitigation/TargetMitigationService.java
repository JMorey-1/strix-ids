package org.example.mitigation;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores mitigation actions that the target application must enforce.
 *
 * The IDS decides what action should be taken, but the target application is
 * responsible for enforcing that action against future requests.
 */
@Service
public class TargetMitigationService {

    private static final long DEFAULT_RATE_LIMIT_SECONDS = 300;
    private static final long DEFAULT_BLACKLIST_SECONDS = 600;
    private static final long RATE_LIMIT_WINDOW_MILLIS = 30_000;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private final Map<String, ActiveMitigation> activeMitigations = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> recentRequestsByIp = new ConcurrentHashMap<>();

    public void applyMitigation(MitigationActionRequest request) {
        if (request == null || request.getIpAddress() == null || request.getActionType() == null) {
            return;
        }

        String ipAddress = request.getIpAddress();
        MitigationActionType actionType = request.getActionType();

        ActiveMitigation existing = activeMitigations.get(ipAddress);

        /*
         * Do not downgrade an IP from BLACKLIST to RATE_LIMIT while the blacklist
         * is still active. Blocking is treated as the stronger mitigation.
         */
        if (existing != null
                && existing.actionType == MitigationActionType.BLACKLIST
                && actionType == MitigationActionType.RATE_LIMIT
                && !existing.isExpired()) {
            return;
        }

        long durationSeconds = request.getExpiresInSeconds();

        /*
         * If the IDS does not send an expiry time, use sensible defaults so
         * mitigations do not remain active forever during demos or repeated tests.
         */
        if (durationSeconds <= 0 && actionType == MitigationActionType.RATE_LIMIT) {
            durationSeconds = DEFAULT_RATE_LIMIT_SECONDS;
        }

        if (durationSeconds <= 0 && actionType == MitigationActionType.BLACKLIST) {
            durationSeconds = DEFAULT_BLACKLIST_SECONDS;
        }

        long expiresAtMillis = Instant.now().toEpochMilli() + (durationSeconds * 1000);

        activeMitigations.put(
                ipAddress,
                new ActiveMitigation(actionType, expiresAtMillis)
        );

        System.out.println("[TARGET][MITIGATION] action=" + actionType
                + " ip=" + ipAddress
                + " reason=" + request.getReason()
                + " expiresInSeconds=" + durationSeconds);
    }

    public boolean isBlacklisted(String ipAddress) {
        ActiveMitigation mitigation = getActiveMitigation(ipAddress);

        // A blacklisted IP should be rejected immediately by the filter.
        return mitigation != null && mitigation.actionType == MitigationActionType.BLACKLIST;
    }

    public boolean isRateLimitExceeded(String ipAddress) {
        ActiveMitigation mitigation = getActiveMitigation(ipAddress);

        /*
         * Rate limiting is only checked for IPs that currently have an active
         * RATE_LIMIT mitigation. Other IPs pass through normally.
         */
        if (mitigation == null || mitigation.actionType != MitigationActionType.RATE_LIMIT) {
            return false;
        }

        long now = Instant.now().toEpochMilli();
        long cutoff = now - RATE_LIMIT_WINDOW_MILLIS;

        Deque<Long> recentRequests = recentRequestsByIp.computeIfAbsent(ipAddress, key -> new ArrayDeque<>());

        synchronized (recentRequests) {
            /*
             * Remove old request timestamps so the count only reflects requests
             * inside the current rate-limit window.
             */
            while (!recentRequests.isEmpty() && recentRequests.peekFirst() < cutoff) {
                recentRequests.removeFirst();
            }

            recentRequests.addLast(now);

            /*
             * If the IP has made too many requests inside the time window, the
             * filter will return 429 Too Many Requests.
             */
            return recentRequests.size() > MAX_REQUESTS_PER_WINDOW;
        }
    }

    public void clearMitigations() {
        /*
         * Used before a fresh generator run so old demo state does not affect
         * the next warm-up, training or detection phase.
         */
        activeMitigations.clear();
        recentRequestsByIp.clear();

        System.out.println("[TARGET][MITIGATION] cleared active mitigations");
    }

    private ActiveMitigation getActiveMitigation(String ipAddress) {
        ActiveMitigation mitigation = activeMitigations.get(ipAddress);

        if (mitigation == null) {
            return null;
        }

        /*
         * Expired mitigations are removed lazily when the IP is checked again.
         * This avoids needing a scheduled cleanup task for the prototype.
         */
        if (mitigation.isExpired()) {
            activeMitigations.remove(ipAddress);
            recentRequestsByIp.remove(ipAddress);
            return null;
        }

        return mitigation;
    }

    /**
     * Represents one active mitigation rule for a single IP address.
     */
    private static class ActiveMitigation {

        private final MitigationActionType actionType;
        private final long expiresAtMillis;

        private ActiveMitigation(MitigationActionType actionType, long expiresAtMillis) {
            this.actionType = actionType;
            this.expiresAtMillis = expiresAtMillis;
        }

        private boolean isExpired() {
            return Instant.now().toEpochMilli() > expiresAtMillis;
        }
    }
}