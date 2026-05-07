package org.example.detection;

import org.example.idslog.IdsEventLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Turns a raw anomaly score into a clearer IDS decision.
 *
 * The ML model only gives Strix a score. This service adds the decision layer
 * on top of that score, deciding whether the traffic is normal, worth watching
 * or serious enough to raise alert. It also tries to explain the likely type
 * of suspicious behaviour using the feature vector.
 */
@Service
public class DetectionClassificationService {

    @Value("${strix.detection.watch-threshold:0.55}")
    private double watchThreshold;

    @Value("${strix.detection.alert-threshold:0.60}")
    private double alertThreshold;

    @Value("${strix.detection.min-events-for-alert:5}")
    private int minEventsForAlert;

    public DetectionResult classify(double score, double[] features) {
        IdsEventLevel level = classifyLevel(score, features);
        String message = buildMessage(level, features);

        return new DetectionResult(level, score, message, features);
    }

    private IdsEventLevel classifyLevel(double score, double[] features) {
        double requestCount = features[0];

        /*
         * A high score on one request is not enough for a full alert.
         * Wait until there is enough recent traffic from the IP to judge the pattern.
         */
        if (score >= alertThreshold && requestCount >= minEventsForAlert) {
            return IdsEventLevel.ALERT;
        }

        /*
         * WATCH is the middle state.
         * It means the behaviour is unusual but not strong enough to be a full alert.
         */
        if (score >= watchThreshold) {
            return IdsEventLevel.WATCH;
        }

        return IdsEventLevel.SCORE;
    }

    private String buildMessage(IdsEventLevel level, double[] features) {
        if (level == IdsEventLevel.SCORE) {
            return "Traffic scored within expected range";
        }

        /*
         * These checks are not the ML model itself.
         * They are explanation rules that help describe why the feature pattern looks suspicious.
         */
        if (isRepeatedFailedLogin(features)) {
            return levelPrefix(level) + " repeated failed login behaviour detected";
        }

        /*
         * Admin probing is checked before general scanning because admin probing can
         * also look like endpoint scanning in the feature vector.
         */
        if (isAdminProbing(features)) {
            return levelPrefix(level) + " admin route probing detected";
        }

        if (isEndpointScanning(features)) {
            return levelPrefix(level) + " endpoint scanning behaviour detected";
        }

        if (isHighUnauthorizedTraffic(features)) {
            return levelPrefix(level) + " high unauthorized response rate detected";
        }

        /*
         * This fallback is important.
         * Strix may still detect unusual behaviour even if it does not fit one named pattern.
         */
        return level == IdsEventLevel.ALERT
                ? "Alert: anomalous behaviour detected"
                : "Watch: suspicious behaviour under observation";
    }

    private boolean isRepeatedFailedLogin(double[] features) {
        /*
         * Feature positions used here:
         * 1 = post ratio
         * 7 = login attempt ratio
         * 8 = failed login ratio
         */
        double postRatio = features[1];
        double loginAttemptRatio = features[7];
        double failedLoginRatio = features[8];

        /*
         * Brute force traffic should mostly be POST requests,
         * mostly aimed at login and mostly unsuccessful.
         */
        return postRatio > 0.6
                && loginAttemptRatio > 0.5
                && failedLoginRatio > 0.7;
    }

    private boolean isEndpointScanning(double[] features) {
        /*
         * Feature positions used here:
         * 3 = unique endpoint ratio
         * 4 = unauthorized ratio
         * 6 = not found ratio
         */
        double uniqueEndpointRatio = features[3];
        double unauthorizedRatio = features[4];
        double notFoundRatio = features[6];

        /*
         * Scanning usually touches many different endpoints.
         * It often creates 404 responses, or sometimes 401 responses if protected routes are hit.
         */
        return uniqueEndpointRatio > 0.5
                && (notFoundRatio > 0.15 || unauthorizedRatio > 0.25);
    }

    private boolean isAdminProbing(double[] features) {
        /*
         * Feature positions used here:
         * 1 = post ratio
         * 2 = admin ratio
         * 4 = unauthorized ratio
         */
        double postRatio = features[1];
        double adminRatio = features[2];
        double unauthorizedRatio = features[4];

        /*
         * Admin probing should be heavily focused on admin routes.
         * It may also cause unauthorised responses or include POST requests.
         */
        return adminRatio > 0.5
                && (unauthorizedRatio > 0.25 || postRatio > 0.4);
    }

    private boolean isHighUnauthorizedTraffic(double[] features) {
        double unauthorizedRatio = features[4];

        // General fallback for traffic causing lots of 401 responses.
        return unauthorizedRatio > 0.5;
    }

    private String levelPrefix(IdsEventLevel level) {
        return level == IdsEventLevel.ALERT ? "Alert:" : "Watch:";
    }
}