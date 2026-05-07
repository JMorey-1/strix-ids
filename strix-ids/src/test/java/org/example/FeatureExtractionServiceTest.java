package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureExtractionServiceTest {

    private FeatureExtractionService featureExtractionService;

    @BeforeEach
    void setUp() {
        // Create service
        featureExtractionService = new FeatureExtractionService();

        // Set test window
        ReflectionTestUtils.setField(featureExtractionService, "windowSeconds", 30);
    }

    @Test
    void extractFeatures_WithSingleGetRequest_ShouldReturnBasicFeatures() {
        // Create request event
        RequestEvent event = event("192.168.1.1", "GET", "/products", 200);

        double[] features = featureExtractionService.extractFeatures("192.168.1.1", event);

        // Check feature vector
        assertArrayEquals(
                new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                features,
                0.001
        );
    }

    @Test
    void extractFeatures_WithMixedTraffic_ShouldCalculateRatios() {
        // Add mixed requests
        featureExtractionService.extractFeatures("10.0.0.1",
                event("10.0.0.1", "GET", "/", 200));

        featureExtractionService.extractFeatures("10.0.0.1",
                event("10.0.0.1", "POST", "/auth/login", 401));

        featureExtractionService.extractFeatures("10.0.0.1",
                event("10.0.0.1", "GET", "/admin", 403));

        double[] features = featureExtractionService.extractFeatures("10.0.0.1",
                event("10.0.0.1", "GET", "/missing", 404));

        // Check calculated features
        assertEquals(4.0, features[0], 0.001);      // request count
        assertEquals(0.25, features[1], 0.001);     // post ratio
        assertEquals(0.25, features[2], 0.001);     // admin ratio
        assertEquals(1.0, features[3], 0.001);      // unique endpoint ratio
        assertEquals(0.25, features[4], 0.001);     // unauthorized ratio
        assertEquals(0.25, features[5], 0.001);     // forbidden ratio
        assertEquals(0.25, features[6], 0.001);     // not found ratio
        assertEquals(0.25, features[7], 0.001);     // login attempt ratio
        assertEquals(1.0, features[8], 0.001);      // failed login ratio
    }

    @Test
    void extractFeatures_WithDifferentIps_ShouldKeepBuffersSeparate() {
        // Add first IP request
        featureExtractionService.extractFeatures("192.168.1.10",
                event("192.168.1.10", "GET", "/", 200));

        // Add second IP request
        double[] features = featureExtractionService.extractFeatures("10.0.0.10",
                event("10.0.0.10", "POST", "/auth/login", 401));

        // Check second IP only
        assertEquals(1.0, features[0], 0.001);
        assertEquals(1.0, features[1], 0.001);
        assertEquals(1.0, features[7], 0.001);
        assertEquals(1.0, features[8], 0.001);
    }

    @Test
    void extractFeatures_WithOldEvents_ShouldPruneOldEvents() {
        // Set short window
        ReflectionTestUtils.setField(featureExtractionService, "windowSeconds", 1);

        long now = System.currentTimeMillis();
        long oldTimestamp = now - 5000;

        // Add old event
        featureExtractionService.extractFeatures("172.16.0.1",
                new RequestEvent("172.16.0.1", "POST", "/auth/login", oldTimestamp, 401));

        // Add recent event
        double[] features = featureExtractionService.extractFeatures("172.16.0.1",
                new RequestEvent("172.16.0.1", "GET", "/products", now, 200));

        // Check old event removed
        assertEquals(1.0, features[0], 0.001);
        assertEquals(0.0, features[1], 0.001);
        assertEquals(0.0, features[7], 0.001);
        assertEquals(0.0, features[8], 0.001);
    }

    @Test
    void extractFeatures_WithRepeatedEndpoints_ShouldCalculateUniqueEndpointRatio() {
        // Add repeated endpoint
        featureExtractionService.extractFeatures("203.0.113.1",
                event("203.0.113.1", "GET", "/products", 200));

        featureExtractionService.extractFeatures("203.0.113.1",
                event("203.0.113.1", "GET", "/products", 200));

        double[] features = featureExtractionService.extractFeatures("203.0.113.1",
                event("203.0.113.1", "GET", "/articles", 200));

        // Check unique ratio
        assertEquals(3.0, features[0], 0.001);
        assertEquals(2.0 / 3.0, features[3], 0.001);
    }

    @Test
    void extractFeatures_WithSuccessfulAndFailedLogins_ShouldCalculateFailedLoginRatio() {
        // Add successful login
        featureExtractionService.extractFeatures("10.0.0.50",
                event("10.0.0.50", "POST", "/auth/login", 200));

        // Add failed login
        double[] features = featureExtractionService.extractFeatures("10.0.0.50",
                event("10.0.0.50", "POST", "/auth/login", 401));

        // Check login ratios
        assertEquals(2.0, features[0], 0.001);
        assertEquals(1.0, features[1], 0.001);
        assertEquals(0.5, features[4], 0.001);
        assertEquals(1.0, features[7], 0.001);
        assertEquals(0.5, features[8], 0.001);
    }

    private RequestEvent event(String ip, String method, String uri, int statusCode) {
        // Create current event
        return new RequestEvent(ip, method, uri, System.currentTimeMillis(), statusCode);
    }
}