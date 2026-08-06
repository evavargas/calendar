package app.kairo.google;

import app.kairo.config.KairoProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GoogleCalendarController {

  private final GoogleCalendarService service;
  private final KairoProperties properties;

  public GoogleCalendarController(GoogleCalendarService service, KairoProperties properties) {
    this.service = service;
    this.properties = properties;
  }

  @GetMapping("/google/status")
  public Map<String, Object> status() {
    return service.status();
  }

  @GetMapping("/google/connect")
  public void connect(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (properties.getAuth().isMockEnabled()) {
      service.connectMock();
      response.sendRedirect(properties.getFrontendUrl() + "/app/settings?google=connected");
      return;
    }
    response.sendRedirect(service.beginConnect(request.getSession(true)));
  }

  @GetMapping("/google/callback")
  public void callback(
      @RequestParam("code") String code,
      @RequestParam("state") String state,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    service.handleOAuthCallback(code, state, request.getSession(true));
    response.sendRedirect(properties.getFrontendUrl() + "/app/settings?google=connected");
  }

  @DeleteMapping("/google/connect")
  public Map<String, Object> disconnect() {
    return service.disconnect();
  }

  @PostMapping("/plans/{id}/google")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> push(@PathVariable UUID id) {
    return service.pushPlan(id);
  }
}
