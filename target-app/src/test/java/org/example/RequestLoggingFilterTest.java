package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

  private static final String TEST_IDS_URL = "http://localhost:8081/events/request";

  @Test
  void doFilter_WithForwardedForHeader_ShouldLogForwardedIp(CapturedOutput output)
      throws Exception {
    // Create filter
    RequestLoggingFilter filter = new RequestLoggingFilter(TEST_IDS_URL);

    // Create mock request
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/products");
    request.addHeader("X-Forwarded-For", "192.168.1.50");

    // Create mock response
    MockHttpServletResponse response = new MockHttpServletResponse();

    // Simulate controller response
    FilterChain chain =
        (servletRequest, servletResponse) ->
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_OK);

    filter.doFilter(request, response, chain);

    // Check logged request
    assertThat(output).contains("[REQUEST] GET /products from 192.168.1.50 -> 200");
  }

  @Test
  void doFilter_WithoutForwardedForHeader_ShouldLogRemoteAddress(CapturedOutput output)
      throws Exception {
    // Create filter
    RequestLoggingFilter filter = new RequestLoggingFilter(TEST_IDS_URL);

    // Create mock request
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
    request.setRemoteAddr("127.0.0.1");

    // Create mock response
    MockHttpServletResponse response = new MockHttpServletResponse();

    // Simulate controller response
    FilterChain chain =
        (servletRequest, servletResponse) ->
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_OK);

    filter.doFilter(request, response, chain);

    // Check logged request
    assertThat(output).contains("[REQUEST] GET /health from 127.0.0.1 -> 200");
  }

  @Test
  void doFilter_WhenResponseIsForbidden_ShouldLogForbiddenStatus(CapturedOutput output)
      throws Exception {
    // Create filter
    RequestLoggingFilter filter = new RequestLoggingFilter(TEST_IDS_URL);

    // Create mock request
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
    request.addHeader("X-Forwarded-For", "10.0.0.25");

    // Create mock response
    MockHttpServletResponse response = new MockHttpServletResponse();

    // Simulate forbidden response
    FilterChain chain =
        (servletRequest, servletResponse) ->
            ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);

    filter.doFilter(request, response, chain);

    // Check response status
    assertThat(response.getStatus()).isEqualTo(403);

    // Check logged request
    assertThat(output).contains("[REQUEST] GET /admin from 10.0.0.25 -> 403");
  }

  @Test
  void doFilter_WhenResponseIsNotFound_ShouldLogNotFoundStatus(CapturedOutput output)
      throws Exception {
    // Create filter
    RequestLoggingFilter filter = new RequestLoggingFilter(TEST_IDS_URL);

    // Create mock request
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/unknown");
    request.addHeader("X-Forwarded-For", "10.0.0.99");

    // Create mock response
    MockHttpServletResponse response = new MockHttpServletResponse();

    // Simulate not found response
    FilterChain chain =
        (servletRequest, servletResponse) ->
            ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND);

    filter.doFilter(request, response, chain);

    // Check response status
    assertThat(response.getStatus()).isEqualTo(404);

    // Check logged request
    assertThat(output).contains("[REQUEST] GET /unknown from 10.0.0.99 -> 404");
  }
}
