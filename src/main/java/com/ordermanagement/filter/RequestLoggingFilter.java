package com.ordermanagement.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter for comprehensive request logging with correlation IDs.
 *
 * Features:
 * - Adds correlation ID (X-Correlation-ID) to each request
 * - Logs request/response details
 * - Tracks request duration
 * - Adds MDC context for tracing
 * - Logs HTTP method, URI, status code, duration
 *
 * Design Pattern: Filter Pattern (Chain of Responsibility)
 * SOLID Principle: Single Responsibility - Only handles request logging
 */
@Component
@Slf4j
public class RequestLoggingFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Generate or retrieve correlation ID
            String correlationId = getOrGenerateCorrelationId(httpRequest);

            // Add to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Add to response headers
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Record start time
            long startTime = System.currentTimeMillis();
            request.setAttribute(REQUEST_START_TIME, startTime);

            // Log incoming request
            logRequest(httpRequest, correlationId);

            // Continue with the filter chain
            chain.doFilter(request, response);

            // Log outgoing response
            logResponse(httpRequest, httpResponse, correlationId, startTime);

        } finally {
            // Clean up MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Get correlation ID from request header or generate new one.
     */
    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Log incoming request details.
     */
    private void logRequest(HttpServletRequest request, String correlationId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        log.info(">>> INCOMING REQUEST | CorrelationId={} | Method={} | URI={} | RemoteAddr={} | UserAgent={}",
                correlationId, method, fullUrl, remoteAddr, userAgent);

        // Log headers in debug mode
        if (log.isDebugEnabled()) {
            StringBuilder headers = new StringBuilder();
            var headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.append(headerName).append("=").append(headerValue).append("; ");
            }
            log.debug("Request Headers: {}", headers);
        }
    }

    /**
     * Log outgoing response details with duration.
     */
    private void logResponse(HttpServletRequest request, HttpServletResponse response,
                            String correlationId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Use different log levels based on status code
        if (status >= 500) {
            log.error("<<< OUTGOING RESPONSE | CorrelationId={} | Method={} | URI={} | Status={} | Duration={}ms | SEVERE ERROR",
                    correlationId, method, uri, status, duration);
        } else if (status >= 400) {
            log.warn("<<< OUTGOING RESPONSE | CorrelationId={} | Method={} | URI={} | Status={} | Duration={}ms | CLIENT ERROR",
                    correlationId, method, uri, status, duration);
        } else {
            log.info("<<< OUTGOING RESPONSE | CorrelationId={} | Method={} | URI={} | Status={} | Duration={}ms | SUCCESS",
                    correlationId, method, uri, status, duration);
        }

        // Log slow requests (> 1 second)
        if (duration > 1000) {
            log.warn("SLOW REQUEST DETECTED | CorrelationId={} | Method={} | URI={} | Duration={}ms",
                    correlationId, method, uri, duration);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("RequestLoggingFilter initialized - Correlation ID tracking enabled");
    }

    @Override
    public void destroy() {
        log.info("RequestLoggingFilter destroyed");
    }
}
