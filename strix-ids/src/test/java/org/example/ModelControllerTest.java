package org.example;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.mitigation.MitigationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ModelController.class)
class ModelControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AnomalyDetectionService anomalyDetectionService;

  @MockitoBean private MitigationService mitigationService;

  @Test
  void startCollecting_ShouldStartCollectionAndReturnMessage() throws Exception {
    mockMvc
        .perform(get("/model/collect"))
        .andExpect(status().isOk())
        .andExpect(content().string("Collecting training data"));

    verify(anomalyDetectionService).startCollecting();
    verify(mitigationService).resetMitigationState();
  }

  @Test
  void train_ShouldTrainModelAndReturnMessage() throws Exception {
    mockMvc
        .perform(get("/model/train"))
        .andExpect(status().isOk())
        .andExpect(content().string("Model trained on real traffic"));

    verify(anomalyDetectionService).trainOnCollected();
  }

  @Test
  void status_WhenModelIsTrainedAndCollecting_ShouldReturnStatus() throws Exception {
    when(anomalyDetectionService.isTrained()).thenReturn(true);
    when(anomalyDetectionService.isCollecting()).thenReturn(true);

    mockMvc
        .perform(get("/model/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.trained").value(true))
        .andExpect(jsonPath("$.collecting").value(true));
  }

  @Test
  void status_WhenModelIsNotTrainedAndNotCollecting_ShouldReturnStatus() throws Exception {
    when(anomalyDetectionService.isTrained()).thenReturn(false);
    when(anomalyDetectionService.isCollecting()).thenReturn(false);

    mockMvc
        .perform(get("/model/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.trained").value(false))
        .andExpect(jsonPath("$.collecting").value(false));
  }
}
