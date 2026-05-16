package org.example.mitigation;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TargetMitigationController.class)
class TargetMitigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TargetMitigationService targetMitigationService;

    @Test
    void receiveMitigationAction_WithValidRateLimitAction_ShouldApplyMitigation() throws Exception {
        // Send a valid RATE_LIMIT action from the IDS to the target app.
        mockMvc.perform(post("/internal/mitigation/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ipAddress": "10.0.0.5",
                                  "actionType": "RATE_LIMIT",
                                  "reason": "Suspicious login behaviour",
                                  "timestamp": 1710000000000,
                                  "expiresInSeconds": 300
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Mitigation action applied"));

        // Capture the request passed into the service.
        ArgumentCaptor<MitigationActionRequest> captor =
                ArgumentCaptor.forClass(MitigationActionRequest.class);

        verify(targetMitigationService).applyMitigation(captor.capture());

        MitigationActionRequest request = captor.getValue();

        assertEquals("10.0.0.5", request.getIpAddress());
        assertEquals(MitigationActionType.RATE_LIMIT, request.getActionType());
        assertEquals("Suspicious login behaviour", request.getReason());
        assertEquals(300, request.getExpiresInSeconds());
    }

    @Test
    void receiveMitigationAction_WithValidBlacklistAction_ShouldApplyMitigation() throws Exception {
        // Send a valid BLACKLIST action from the IDS to the target app.
        mockMvc.perform(post("/internal/mitigation/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ipAddress": "10.0.0.9",
                                  "actionType": "BLACKLIST",
                                  "reason": "Persistent anomalous behaviour",
                                  "timestamp": 1710000000000,
                                  "expiresInSeconds": 600
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Mitigation action applied"));

        ArgumentCaptor<MitigationActionRequest> captor =
                ArgumentCaptor.forClass(MitigationActionRequest.class);

        verify(targetMitigationService).applyMitigation(captor.capture());

        MitigationActionRequest request = captor.getValue();

        assertEquals("10.0.0.9", request.getIpAddress());
        assertEquals(MitigationActionType.BLACKLIST, request.getActionType());
        assertEquals("Persistent anomalous behaviour", request.getReason());
        assertEquals(600, request.getExpiresInSeconds());
    }

    @Test
    void receiveMitigationAction_WithMissingIpAddress_ShouldReturnBadRequest() throws Exception {
        // IP address is required because mitigation is enforced per IP.
        mockMvc.perform(post("/internal/mitigation/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actionType": "BLACKLIST",
                                  "reason": "Missing IP test",
                                  "timestamp": 1710000000000,
                                  "expiresInSeconds": 600
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing mitigation action details"));
    }

    @Test
    void receiveMitigationAction_WithMissingActionType_ShouldReturnBadRequest() throws Exception {
        // Action type is required so the target app knows what to enforce.
        mockMvc.perform(post("/internal/mitigation/actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ipAddress": "10.0.0.10",
                                  "reason": "Missing action type test",
                                  "timestamp": 1710000000000,
                                  "expiresInSeconds": 600
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing mitigation action details"));
    }

    @Test
    void resetMitigations_ShouldClearTargetMitigationState() throws Exception {
        // Reset endpoint is used before a fresh demo or generator run.
        mockMvc.perform(post("/internal/mitigation/reset"))
                .andExpect(status().isOk())
                .andExpect(content().string("Target mitigation state reset"));

        verify(targetMitigationService).clearMitigations();
    }
}