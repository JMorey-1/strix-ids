package org.example.idslog;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IdsEventLogController.class)
class IdsEventLogControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private IdsEventLogService idsEventLogService;

  @Test
  void getRecentEvents_ShouldReturnEmptyEventList() throws Exception {
    when(idsEventLogService.getRecentEvents()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/ids-events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(idsEventLogService).getRecentEvents();
  }
}
