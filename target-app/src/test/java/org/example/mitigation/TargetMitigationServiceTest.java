package org.example.mitigation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TargetMitigationServiceTest {

    @Test
    void applyMitigation_WithBlacklistAction_ShouldBlacklistIp() {
        TargetMitigationService service = new TargetMitigationService();

        MitigationActionRequest request = new MitigationActionRequest();
        request.setIpAddress("10.0.0.5");
        request.setActionType(MitigationActionType.BLACKLIST);
        request.setReason("Repeated anomalous behaviour");
        request.setExpiresInSeconds(300);

        service.applyMitigation(request);

        assertTrue(service.isBlacklisted("10.0.0.5"));
    }

    @Test
    void applyMitigation_WithRateLimitAction_ShouldRateLimitAfterTooManyRequests() {
        TargetMitigationService service = new TargetMitigationService();

        MitigationActionRequest request = new MitigationActionRequest();
        request.setIpAddress("10.0.0.6");
        request.setActionType(MitigationActionType.RATE_LIMIT);
        request.setReason("Suspicious behaviour");
        request.setExpiresInSeconds(300);

        service.applyMitigation(request);

        assertFalse(service.isRateLimitExceeded("10.0.0.6"));
        assertFalse(service.isRateLimitExceeded("10.0.0.6"));
        assertFalse(service.isRateLimitExceeded("10.0.0.6"));
        assertFalse(service.isRateLimitExceeded("10.0.0.6"));
        assertFalse(service.isRateLimitExceeded("10.0.0.6"));

        assertTrue(service.isRateLimitExceeded("10.0.0.6"));
    }

    @Test
    void applyMitigation_WhenIpIsBlacklisted_ShouldNotDowngradeToRateLimit() {
        TargetMitigationService service = new TargetMitigationService();

        MitigationActionRequest blacklistRequest = new MitigationActionRequest();
        blacklistRequest.setIpAddress("10.0.0.7");
        blacklistRequest.setActionType(MitigationActionType.BLACKLIST);
        blacklistRequest.setReason("Persistent attack behaviour");
        blacklistRequest.setExpiresInSeconds(300);

        service.applyMitigation(blacklistRequest);

        MitigationActionRequest rateLimitRequest = new MitigationActionRequest();
        rateLimitRequest.setIpAddress("10.0.0.7");
        rateLimitRequest.setActionType(MitigationActionType.RATE_LIMIT);
        rateLimitRequest.setReason("Lower severity event");
        rateLimitRequest.setExpiresInSeconds(300);

        service.applyMitigation(rateLimitRequest);

        assertTrue(service.isBlacklisted("10.0.0.7"));
    }

    @Test
    void clearMitigations_ShouldRemoveBlacklistAndRateLimitState() {
        TargetMitigationService service = new TargetMitigationService();

        MitigationActionRequest blacklistRequest = new MitigationActionRequest();
        blacklistRequest.setIpAddress("10.0.0.8");
        blacklistRequest.setActionType(MitigationActionType.BLACKLIST);
        blacklistRequest.setReason("Test blacklist");
        blacklistRequest.setExpiresInSeconds(300);

        MitigationActionRequest rateLimitRequest = new MitigationActionRequest();
        rateLimitRequest.setIpAddress("10.0.0.9");
        rateLimitRequest.setActionType(MitigationActionType.RATE_LIMIT);
        rateLimitRequest.setReason("Test rate limit");
        rateLimitRequest.setExpiresInSeconds(300);

        service.applyMitigation(blacklistRequest);
        service.applyMitigation(rateLimitRequest);

        assertTrue(service.isBlacklisted("10.0.0.8"));

        service.clearMitigations();

        assertFalse(service.isBlacklisted("10.0.0.8"));
        assertFalse(service.isRateLimitExceeded("10.0.0.9"));
    }

    @Test
    void applyMitigation_WithMissingDetails_ShouldIgnoreRequest() {
        TargetMitigationService service = new TargetMitigationService();

        MitigationActionRequest request = new MitigationActionRequest();

        assertDoesNotThrow(() -> service.applyMitigation(request));
    }
}