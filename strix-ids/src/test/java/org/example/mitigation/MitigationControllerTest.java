package org.example.mitigation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MitigationController.class)
class MitigationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MitigationService mitigationService;

  @Test
  void getSuspiciousIps_ShouldReturnSuspiciousRecords() throws Exception {
    MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.5");

    when(mitigationService.getSuspiciousRecords()).thenReturn(List.of(mitigationRecord));

    mockMvc
        .perform(get("/api/suspicious-ips"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ipAddress").value("10.0.0.5"))
        .andExpect(jsonPath("$[0].status").value("WATCH"))
        .andExpect(jsonPath("$[0].watchCount").value(0))
        .andExpect(jsonPath("$[0].alertCount").value(0))
        .andExpect(jsonPath("$[0].suspicionScore").value(0));

    verify(mitigationService).getSuspiciousRecords();
  }

  @Test
  void getBlacklist_ShouldReturnBlockedRecords() throws Exception {
    MitigationRecord mitigationRecord = new MitigationRecord("10.0.0.9");
    mitigationRecord.registerAlert("Alert 1");
    mitigationRecord.registerAlert("Alert 2");
    mitigationRecord.registerAlert("Alert 3");

    when(mitigationService.getBlacklist()).thenReturn(List.of(mitigationRecord));

    mockMvc
        .perform(get("/api/blacklist"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].ipAddress").value("10.0.0.9"))
        .andExpect(jsonPath("$[0].status").value("BLOCKED"))
        .andExpect(jsonPath("$[0].alertCount").value(3))
        .andExpect(jsonPath("$[0].suspicionScore").value(9));

    verify(mitigationService).getBlacklist();
  }

  @Test
  void resetMitigationState_ShouldReturnNoContent() throws Exception {
    mockMvc
        .perform(post("/api/mitigation/reset"))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    verify(mitigationService).resetMitigationState();
  }
}
