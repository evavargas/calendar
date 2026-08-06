package app.kairo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kairo")
public class KairoProperties {

  private String frontendUrl = "http://localhost:5173";
  /** 32+ chars recommended. Used to encrypt Google refresh tokens at rest. */
  private String tokenSecret = "local-dev-token-secret-32b!!";
  private final Auth auth = new Auth();
  private final Cors cors = new Cors();
  private final Google google = new Google();

  public String getFrontendUrl() {
    return frontendUrl;
  }

  public void setFrontendUrl(String frontendUrl) {
    this.frontendUrl = frontendUrl;
  }

  public String getTokenSecret() {
    return tokenSecret;
  }

  public void setTokenSecret(String tokenSecret) {
    this.tokenSecret = tokenSecret;
  }

  public Auth getAuth() {
    return auth;
  }

  public Cors getCors() {
    return cors;
  }

  public Google getGoogle() {
    return google;
  }

  public static class Auth {
    private boolean mockEnabled = true;

    public boolean isMockEnabled() {
      return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
      this.mockEnabled = mockEnabled;
    }
  }

  public static class Cors {
    private String allowedOrigins = "http://localhost:5173";

    public String getAllowedOrigins() {
      return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
      this.allowedOrigins = allowedOrigins;
    }
  }

  public static class Google {
    private String clientId = "";
    private String clientSecret = "";
    private String calendarRedirectUri = "http://localhost:8080/api/google/callback";
    private String calendarScope = "https://www.googleapis.com/auth/calendar.events";

    public String getClientId() {
      return clientId;
    }

    public void setClientId(String clientId) {
      this.clientId = clientId;
    }

    public String getClientSecret() {
      return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
    }

    public String getCalendarRedirectUri() {
      return calendarRedirectUri;
    }

    public void setCalendarRedirectUri(String calendarRedirectUri) {
      this.calendarRedirectUri = calendarRedirectUri;
    }

    public String getCalendarScope() {
      return calendarScope;
    }

    public void setCalendarScope(String calendarScope) {
      this.calendarScope = calendarScope;
    }
  }
}
