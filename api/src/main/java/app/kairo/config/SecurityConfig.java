package app.kairo.config;

import app.kairo.auth.CookieOAuth2AuthorizationRequestRepository;
import app.kairo.auth.KairoAuthenticationFailureHandler;
import app.kairo.auth.KairoAuthenticationSuccessHandler;
import app.kairo.auth.KairoOAuth2UserService;
import app.kairo.auth.KairoOidcUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(KairoProperties.class)
public class SecurityConfig {

  private static final String SESSION_COOKIE = "KAIRO_SESSION";

  private final KairoProperties properties;
  private final KairoOAuth2UserService oAuth2UserService;
  private final KairoOidcUserService oidcUserService;
  private final KairoAuthenticationSuccessHandler successHandler;
  private final KairoAuthenticationFailureHandler failureHandler;
  private final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
  private final String sessionCookieDomain;
  private final boolean sessionCookieSecure;

  public SecurityConfig(
      KairoProperties properties,
      KairoOAuth2UserService oAuth2UserService,
      KairoOidcUserService oidcUserService,
      KairoAuthenticationSuccessHandler successHandler,
      KairoAuthenticationFailureHandler failureHandler,
      CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository,
      @Value("${server.servlet.session.cookie.domain:}") String sessionCookieDomain,
      @Value("${server.servlet.session.cookie.secure:false}") boolean sessionCookieSecure) {
    this.properties = properties;
    this.oAuth2UserService = oAuth2UserService;
    this.oidcUserService = oidcUserService;
    this.successHandler = successHandler;
    this.failureHandler = failureHandler;
    this.authorizationRequestRepository = authorizationRequestRepository;
    this.sessionCookieDomain = sessionCookieDomain;
    this.sessionCookieSecure = sessionCookieSecure;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/health",
                        "/api/health",
                        "/api/auth/google",
                        "/api/auth/me",
                        "/api/auth/logout",
                        "/api/google/callback",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .oauth2Login(
            oauth ->
                oauth
                    .authorizationEndpoint(
                        auth ->
                            auth.authorizationRequestRepository(authorizationRequestRepository))
                    .userInfoEndpoint(
                        userInfo ->
                            userInfo.userService(oAuth2UserService).oidcUserService(oidcUserService))
                    .successHandler(successHandler)
                    .failureHandler(failureHandler))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .logoutSuccessHandler(this::onLogoutSuccess));

    return http.build();
  }

  private void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    // Spring's deleteCookies() omits Domain. Prod sets KAIRO_SESSION with
    // Domain=calendar-bay-kappa.vercel.app — without matching Domain the browser
    // keeps the cookie and guestOnly routing bounces back into /app.
    ResponseCookie.ResponseCookieBuilder builder =
        ResponseCookie.from(SESSION_COOKIE, "")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .secure(sessionCookieSecure)
            .sameSite("Lax");
    if (StringUtils.hasText(sessionCookieDomain)) {
      builder.domain(sessionCookieDomain);
    }
    response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins =
        Arrays.stream(properties.getCors().getAllowedOrigins().split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
