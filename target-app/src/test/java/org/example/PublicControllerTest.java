package org.example;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.mitigation.TargetMitigationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicController.class)
class PublicControllerTest {

  @MockitoBean private TargetMitigationService targetMitigationService;

  @Autowired private MockMvc mockMvc;

  @Test
  void home_ShouldReturnWelcomeMessage() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string("Welcome"));
  }

  @Test
  void health_ShouldReturnUpStatus() throws Exception {
    mockMvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("UP"));
  }

  @Test
  void products_ShouldReturnProductList() throws Exception {
    mockMvc
        .perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0]").value("Product A"))
        .andExpect(jsonPath("$[1]").value("Product B"))
        .andExpect(jsonPath("$[2]").value("Product C"));
  }

  @Test
  void product_ShouldReturnProductWithGivenId() throws Exception {
    mockMvc
        .perform(get("/products/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("Product 1"));
  }

  @Test
  void articles_ShouldReturnArticleList() throws Exception {
    mockMvc
        .perform(get("/articles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0]").value("Article 1"))
        .andExpect(jsonPath("$[1]").value("Article 2"))
        .andExpect(jsonPath("$[2]").value("Article 3"));
  }

  @Test
  void contact_ShouldReturnMessageReceived() throws Exception {
    String requestBody =
        """
                {
                    "name": "Jamie",
                    "message": "Hello"
                }
                """;

    mockMvc
        .perform(post("/contact").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().string("Message received"));
  }

  @Test
  void register_ShouldReturnRegisteredMessage() throws Exception {
    String requestBody =
        """
                {
                    "username": "jamie",
                    "password": "password123"
                }
                """;

    mockMvc
        .perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isOk())
        .andExpect(content().string("Registered"));
  }

  @Test
  void data_ShouldReturnApiData() throws Exception {
    mockMvc
        .perform(get("/api/data"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.count").value(42));
  }
}
