package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModelController.class)
class ModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnomalyDetectionService anomalyDetectionService;

    @Test
    void startCollecting_ShouldStartCollectionAndReturnMessage() throws Exception {
        // Test collect endpoint
        mockMvc.perform(get("/model/collect"))
                .andExpect(status().isOk())
                .andExpect(content().string("Collecting training data"));

        // Check service called
        verify(anomalyDetectionService).startCollecting();
    }

    @Test
    void train_ShouldTrainModelAndReturnMessage() throws Exception {
        // Test train endpoint
        mockMvc.perform(get("/model/train"))
                .andExpect(status().isOk())
                .andExpect(content().string("Model trained on real traffic"));

        // Check service called
        verify(anomalyDetectionService).trainOnCollected();
    }

    @Test
    void status_WhenModelIsTrainedAndCollecting_ShouldReturnStatus() throws Exception {
        // Set mock values
        when(anomalyDetectionService.isTrained()).thenReturn(true);
        when(anomalyDetectionService.isCollecting()).thenReturn(true);

        // Test status endpoint
        mockMvc.perform(get("/model/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trained").value(true))
                .andExpect(jsonPath("$.collecting").value(true));
    }

    @Test
    void status_WhenModelIsNotTrainedAndNotCollecting_ShouldReturnStatus() throws Exception {
        // Set mock values
        when(anomalyDetectionService.isTrained()).thenReturn(false);
        when(anomalyDetectionService.isCollecting()).thenReturn(false);

        // Test status endpoint
        mockMvc.perform(get("/model/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trained").value(false))
                .andExpect(jsonPath("$.collecting").value(false));
    }
}