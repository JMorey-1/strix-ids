package org.example.mitigation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.audit.AuditLogService;
import org.example.idslog.IdsEventLevel;
import org.example.idslog.IdsEventLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

/**
 * Sends mitigation actions from the IDS back to the target application.
 */
@Component
public class MitigationActionClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final IdsEventLogService idsEventLogService;
    private final AuditLogService auditLogService;
    private final String targetMitigationUrl;

    public MitigationActionClient(ObjectMapper objectMapper,
                                  IdsEventLogService idsEventLogService,
                                  AuditLogService auditLogService,
                                  @Value("${strix.target.mitigation-url:http://localhost:8080/internal/mitigation/actions}")
                                  String targetMitigationUrl) {
        this.objectMapper = objectMapper;
        this.idsEventLogService = idsEventLogService;
        this.auditLogService = auditLogService;
        this.targetMitigationUrl = targetMitigationUrl;
    }

    public void sendRateLimitAction(MitigationRecord record) {
        sendAction(record, MitigationActionType.RATE_LIMIT, 300);
    }

    public void sendBlacklistAction(MitigationRecord record) {
        sendAction(record, MitigationActionType.BLACKLIST, 600);
    }

    private void sendAction(MitigationRecord record, MitigationActionType actionType, long expiresInSeconds) {
        MitigationActionRequest request = new MitigationActionRequest(
                record.getIpAddress(),
                actionType,
                record.getReason(),
                Instant.now().toEpochMilli(),
                expiresInSeconds
        );

        String requestBody;

        try {
            requestBody = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            String message = "Failed to build mitigation request: " + e.getMessage();

            idsEventLogService.addEvent(
                    IdsEventLevel.SYSTEM,
                    record.getIpAddress(),
                    null,
                    message,
                    null
            );

            auditLogService.logMitigationAction(
                    record.getIpAddress(),
                    actionType.name(),
                    record.getReason(),
                    "failed to build request"
            );

            return;
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(targetMitigationUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> handleResponse(record, actionType, response))
                .exceptionally(error -> {
                    String message = "Failed to send " + actionType
                            + " action to target app: " + error.getMessage();

                    idsEventLogService.addEvent(
                            IdsEventLevel.SYSTEM,
                            record.getIpAddress(),
                            null,
                            message,
                            null
                    );

                    auditLogService.logMitigationAction(
                            record.getIpAddress(),
                            actionType.name(),
                            record.getReason(),
                            "failed to send"
                    );

                    return null;
                });
    }

    private void handleResponse(MitigationRecord record,
                                MitigationActionType actionType,
                                HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            idsEventLogService.addEvent(
                    IdsEventLevel.SYSTEM,
                    record.getIpAddress(),
                    null,
                    "[IDS][MITIGATION] action=" + actionType + " sent to target app",
                    null
            );

            auditLogService.logMitigationAction(
                    record.getIpAddress(),
                    actionType.name(),
                    record.getReason(),
                    "sent to target app"
            );

            return;
        }

        idsEventLogService.addEvent(
                IdsEventLevel.SYSTEM,
                record.getIpAddress(),
                null,
                "Target app rejected " + actionType + " action with status " + response.statusCode(),
                null
        );

        auditLogService.logMitigationAction(
                record.getIpAddress(),
                actionType.name(),
                record.getReason(),
                "rejected with status " + response.statusCode()
        );
    }
}