package app.kairo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.kairo.auth.KairoPrincipal;
import app.kairo.auth.UserProvisioningService;
import app.kairo.users.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserProvisioningService provisioningService;
  @Autowired private ObjectMapper objectMapper;

  private UsernamePasswordAuthenticationToken auth;

  @BeforeEach
  void setUp() {
    UserEntity user = provisioningService.ensureMockUser();
    KairoPrincipal principal = provisioningService.toPrincipal(user);
    auth = new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
  }

  @Test
  void healthIsPublic() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void meWithoutSessionReturnsNoContent() throws Exception {
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isNoContent());
  }

  @Test
  void createsPlanAndExportsIcs() throws Exception {
    String typesJson =
        mockMvc
            .perform(get("/api/types").with(authentication(auth)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode types = objectMapper.readTree(typesJson);
    String typeId = types.get(0).get("id").asText();

    mockMvc
        .perform(
            post("/api/plans")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "Integration plan",
                      "typeId": "%s",
                      "description": "From test",
                      "startsAt": "2026-08-12T15:00:00Z",
                      "endsAt": "2026-08-12T16:00:00Z",
                      "allDay": false
                    }
                    """
                        .formatted(typeId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Integration plan"));

    mockMvc
        .perform(get("/api/plans/export.ics").with(authentication(auth)))
        .andExpect(status().isOk());
  }
}
