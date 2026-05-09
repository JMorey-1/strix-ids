package org.example;

import org.example.detection.DetectionClassificationService;
import org.example.detection.DetectionResult;
import org.example.idslog.IdsEventLevel;
import org.example.idslog.IdsEventLogService;
import org.example.mitigation.MitigationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureExtractionService featureExtractionService;

    @MockBean
    private AnomalyDetectionService anomalyDetectionService;

    @MockBean
    private DetectionClassificationService detectionClassificationService;

    @MockBean
    private IdsEventLogService idsEventLogService;

    @MockBean
    private MitigationService mitigationService;

    @Test
    void receiveEvent_WhenModelIsCollecting_ShouldCollectFeaturesAndLogTrainingSample()
            throws Exception {
        // Create feature vector
        double[] features = {1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        // Set service state
        when(featureExtractionService.extractFeatures(eq("192.168.1.5"), any(RequestEvent.class)))
                .thenReturn(features);
        when(anomalyDetectionService.isCollecting()).thenReturn(true);

        // Test incoming request event
        mockMvc.perform(post("/events/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("192.168.1.5", "GET", "/products", 200)))
                .andExpect(status().isOk());

        // Check features collected
        verify(anomalyDetectionService).collect(same(features));

        // Check collect log entry
        verify(idsEventLogService).addEvent(
                eq(IdsEventLevel.COLLECT),
                eq("192.168.1.5"),
                isNull(),
                eq("Collecting training sample"),
                same(features)
        );

        // Check detection path not used
        verify(anomalyDetectionService, never()).score(any());
        verifyNoInteractions(detectionClassificationService);
        verifyNoInteractions(mitigationService);
    }

    @Test
    void receiveEvent_WhenModelIsTrained_ShouldScoreClassifyLogAndProcessMitigation()
            throws Exception {
        // Create feature vector
        double[] features = {8.0, 0.8, 0.0, 0.4, 0.7, 0.0, 0.0, 0.8, 0.9};

        // Create detection result
        DetectionResult result = new DetectionResult(
                IdsEventLevel.WATCH,
                0.58,
                "Watch: repeated failed login behaviour detected",
                features
        );

        // Set service state
        when(featureExtractionService.extractFeatures(eq("10.0.0.5"), any(RequestEvent.class)))
                .thenReturn(features);
        when(anomalyDetectionService.isCollecting()).thenReturn(false);
        when(anomalyDetectionService.isTrained()).thenReturn(true);
        when(anomalyDetectionService.score(features)).thenReturn(0.58);
        when(detectionClassificationService.classify(0.58, features)).thenReturn(result);

        // Test incoming request event
        mockMvc.perform(post("/events/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("10.0.0.5", "POST", "/auth/login", 401)))
                .andExpect(status().isOk());

        // Check scoring and classification
        verify(anomalyDetectionService).score(same(features));
        verify(detectionClassificationService).classify(0.58, features);

        // Check IDS event logged
        verify(idsEventLogService).addEvent(
                eq(IdsEventLevel.WATCH),
                eq("10.0.0.5"),
                eq(0.58),
                eq("Watch: repeated failed login behaviour detected"),
                same(features)
        );

        // Check mitigation updated
        verify(mitigationService).processDetectionEvent(
                "10.0.0.5",
                IdsEventLevel.WATCH,
                "Watch: repeated failed login behaviour detected"
        );
    }

    @Test
    void receiveEvent_WhenModelIsNotTrained_ShouldLogWaitingEvent()
            throws Exception {
        // Create feature vector
        double[] features = {1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        // Set service state
        when(featureExtractionService.extractFeatures(eq("192.168.1.10"), any(RequestEvent.class)))
                .thenReturn(features);
        when(anomalyDetectionService.isCollecting()).thenReturn(false);
        when(anomalyDetectionService.isTrained()).thenReturn(false);

        // Test incoming request event
        mockMvc.perform(post("/events/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("192.168.1.10", "GET", "/health", 200)))
                .andExpect(status().isOk());

        // Check waiting log entry
        verify(idsEventLogService).addEvent(
                eq(IdsEventLevel.WAITING),
                eq("192.168.1.10"),
                isNull(),
                eq("Event received but model is not trained"),
                same(features)
        );

        // Check model path not used
        verify(anomalyDetectionService, never()).collect(any());
        verify(anomalyDetectionService, never()).score(any());
        verifyNoInteractions(detectionClassificationService);
        verifyNoInteractions(mitigationService);
    }

    private String requestJson(String ip, String method, String uri, int statusCode) {
        // Create request event JSON
        return """
                {
                    "ip": "%s",
                    "method": "%s",
                    "uri": "%s",
                    "timestamp": 123456789,
                    "statusCode": %d
                }
                """.formatted(ip, method, uri, statusCode);
    }
}