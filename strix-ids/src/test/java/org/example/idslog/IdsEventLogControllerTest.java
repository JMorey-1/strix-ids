package org.example.idslog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IdsEventLogController.class)
class IdsEventLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdsEventLogService idsEventLogService;

    @Test
    void getRecentEvents_ShouldReturnEmptyEventList() throws Exception {
        // Set empty log
        when(idsEventLogService.getRecentEvents()).thenReturn(List.of());

        // Test endpoint
        mockMvc.perform(get("/api/ids-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Check service called
        verify(idsEventLogService).getRecentEvents();
    }
}