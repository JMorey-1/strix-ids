package org.example.mitigation;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetMitigationFilterTest {

    @Mock
    private TargetMitigationService targetMitigationService;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilter_WhenIpIsBlacklisted_ShouldReturnForbidden() throws Exception {
        // Create request from a simulated attacker IP.
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/users");
        request.addHeader("X-Forwarded-For", "10.0.0.5");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(targetMitigationService.isBlacklisted("10.0.0.5")).thenReturn(true);

        TargetMitigationFilter filter = new TargetMitigationFilter(targetMitigationService);

        filter.doFilter(request, response, filterChain);

        // Blacklisted IPs should be blocked before reaching the controller.
        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_WhenRateLimitExceeded_ShouldReturnTooManyRequests() throws Exception {
        // Create request from a rate-limited IP.
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        request.addHeader("X-Forwarded-For", "10.0.0.6");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(targetMitigationService.isBlacklisted("10.0.0.6")).thenReturn(false);
        when(targetMitigationService.isRateLimitExceeded("10.0.0.6")).thenReturn(true);

        TargetMitigationFilter filter = new TargetMitigationFilter(targetMitigationService);

        filter.doFilter(request, response, filterChain);

        // Rate-limited IPs should receive 429 Too Many Requests.
        assertEquals(429, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilter_WhenIpIsNotMitigated_ShouldContinueFilterChain() throws Exception {
        // Create request from a normal IP.
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.addHeader("X-Forwarded-For", "192.168.1.20");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(targetMitigationService.isBlacklisted("192.168.1.20")).thenReturn(false);
        when(targetMitigationService.isRateLimitExceeded("192.168.1.20")).thenReturn(false);

        TargetMitigationFilter filter = new TargetMitigationFilter(targetMitigationService);

        filter.doFilter(request, response, filterChain);

        // Normal traffic should continue through the application.
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_WithMultipleForwardedIps_ShouldUseFirstIpAddress() throws Exception {
        // X-Forwarded-For can contain multiple IPs, but the first one represents the client.
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.addHeader("X-Forwarded-For", "10.0.0.8, 127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(targetMitigationService.isBlacklisted("10.0.0.8")).thenReturn(false);
        when(targetMitigationService.isRateLimitExceeded("10.0.0.8")).thenReturn(false);

        TargetMitigationFilter filter = new TargetMitigationFilter(targetMitigationService);

        filter.doFilter(request, response, filterChain);

        // The filter should check the simulated client IP, not the proxy/local IP.
        verify(targetMitigationService).isBlacklisted("10.0.0.8");
        verify(targetMitigationService).isRateLimitExceeded("10.0.0.8");
        verify(filterChain).doFilter(request, response);
    }
}