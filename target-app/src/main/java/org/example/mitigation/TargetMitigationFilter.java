package org.example.mitigation;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Enforces mitigation actions received from the IDS.
 */
@Component
@Order(2)
public class TargetMitigationFilter implements Filter {

    private final TargetMitigationService targetMitigationService;

    public TargetMitigationFilter(TargetMitigationService targetMitigationService) {
        this.targetMitigationService = targetMitigationService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ipAddress = extractClientIp(req);

        if (targetMitigationService.isBlacklisted(ipAddress)) {
            System.out.println("[TARGET][BLOCK] ip=" + ipAddress
                    + " uri=" + req.getRequestURI()
                    + " status=403");

            res.sendError(HttpServletResponse.SC_FORBIDDEN, "IP address is blocked");
            return;
        }

        if (targetMitigationService.isRateLimitExceeded(ipAddress)) {
            System.out.println("[TARGET][RATE_LIMIT] ip=" + ipAddress
                    + " uri=" + req.getRequestURI()
                    + " status=429");

            res.sendError(429, "Too many requests");
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No setup required.
    }

    @Override
    public void destroy() {
        // No cleanup required.
    }
}