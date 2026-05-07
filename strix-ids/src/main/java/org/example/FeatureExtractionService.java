package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the behavioural feature vector used by the IDS.
 *
 * This service keeps a short rolling window of request events for each IP address.
 * Instead of judging a single request on its own, Strix looks at recent behaviour
 * from the same IP and turns that behaviour into numbers for the ML model.
 */
@Service
public class FeatureExtractionService {

    // The number of seconds of recent activity to keep for each IP.
    @Value("${strix.window.seconds:30}")
    private int windowSeconds;

    /*
     * Stores recent request events per IP address.
     *
     * Example:
     * 192.168.1.5 -> [request1, request2, request3]
     * 10.0.0.1    -> [request1, request2, request3]
     */
    private final ConcurrentHashMap<String, List<RequestEvent>> eventBuffer = new ConcurrentHashMap<>();

    public double[] extractFeatures(String ip, RequestEvent incoming) {
        // Create or retrieve the event list for this IP.
        List<RequestEvent> events = eventBuffer.computeIfAbsent(ip, key -> new ArrayList<>());

        /*
         * The map itself is thread-safe, but the ArrayList inside it is not.
         * I lock the list while adding and reading events for this IP.
         */
        synchronized (events) {
            events.add(incoming);

            // Keep only recent events inside the sliding window.
            pruneOldEvents(events);

            // Convert the recent events into the numeric feature vector.
            return calculateFeatures(events);
        }
    }

    private void pruneOldEvents(List<RequestEvent> events) {
        long cutoff = System.currentTimeMillis() - (windowSeconds * 1000L);

        /*
         * Any event older than the cutoff is removed.
         * This keeps the model focused on recent behaviour not old history.
         */
        events.removeIf(e -> e.getTimestamp() < cutoff);
    }

    /*
     * Converts the recent request history for one IP into a numeric feature vector.
     * The model does not understand raw HTTP events, so I summarise the behaviour
     * into counts and ratios like request volume, POST usage, admin access,
     * failed login activity and error responses.
     */
    private double[] calculateFeatures(List<RequestEvent> events) {
        int total = events.size();

        // Safety fallback. In normal use this should rarely happen (I hope).
        if (total == 0) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        }

        /*
         * Count how many recent requests were POST requests.
         * This helps detect behaviour like repeated login attempts.
         */
        long postCount = events.stream()
                .filter(e -> "POST".equalsIgnoreCase(e.getMethod()))
                .count();

        /*
         * Count how much traffic is aimed at admin endpoints.
         * Admin-heavy traffic can suggest probing or unauthorised access attempts.
         */
        long adminCount = events.stream()
                .filter(e -> e.getUri() != null && e.getUri().startsWith("/admin"))
                .count();

        /*
         * Count how many different endpoints this IP has touched.
         * A high value can suggest scanning behaviour.
         */
        long uniqueEndpoints = events.stream()
                .map(RequestEvent::getUri)
                .filter(uri -> uri != null)
                .distinct()
                .count();

        // HTTP 401 usually means unauthorised.
        long unauthorizedCount = events.stream()
                .filter(e -> e.getStatusCode() == 401)
                .count();

        // HTTP 403 means forbidden.
        long forbiddenCount = events.stream()
                .filter(e -> e.getStatusCode() == 403)
                .count();

        // HTTP 404 can be useful for spotting endpoint scanning.
        long notFoundCount = events.stream()
                .filter(e -> e.getStatusCode() == 404)
                .count();

        // Count login attempts specifically.
        long loginAttemptCount = events.stream()
                .filter(e -> "/auth/login".equals(e.getUri()))
                .count();

        // Count failed login attempts specifically.
        long failedLoginCount = events.stream()
                .filter(e -> "/auth/login".equals(e.getUri()))
                .filter(e -> e.getStatusCode() == 401 || e.getStatusCode() == 403)
                .count();

        /*
         * Most features are ratios rather than raw counts.
         * This makes behaviour easier to compare between IPs with different request totals.
         */
        double requestCount = total;
        double postRatio = (double) postCount / total;
        double adminRatio = (double) adminCount / total;
        double uniqueEndpointRatio = (double) uniqueEndpoints / total;
        double unauthorizedRatio = (double) unauthorizedCount / total;
        double forbiddenRatio = (double) forbiddenCount / total;
        double notFoundRatio = (double) notFoundCount / total;
        double loginAttemptRatio = (double) loginAttemptCount / total;

        /*
         * This ratio is slightly different.
         * It asks: out of the login attempts, how many failed?
         */
        double failedLoginRatio = loginAttemptCount == 0
                ? 0.0
                : (double) failedLoginCount / loginAttemptCount;

        /*
         * Feature order matters.
         * DetectionClassificationService expects these values in this exact order.
         */
        return new double[]{
                requestCount,
                postRatio,
                adminRatio,
                uniqueEndpointRatio,
                unauthorizedRatio,
                forbiddenRatio,
                notFoundRatio,
                loginAttemptRatio,
                failedLoginRatio
        };
    }
}