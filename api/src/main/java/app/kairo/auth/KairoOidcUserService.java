package app.kairo.auth;

import app.kairo.users.UserEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Google login uses the {@code openid} scope, so Spring calls the OIDC user service — not {@link
 * KairoOAuth2UserService}. Provision the local user here and expose {@code userId} for the success
 * handler.
 */
@Service
public class KairoOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

  private final OidcUserService delegate = new OidcUserService();
  private final UserProvisioningService provisioningService;

  public KairoOidcUserService(UserProvisioningService provisioningService) {
    this.provisioningService = provisioningService;
  }

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    OidcUser oidcUser = delegate.loadUser(userRequest);
    Map<String, Object> attributes = oidcUser.getAttributes();

    String googleSub = String.valueOf(attributes.get("sub"));
    String email = String.valueOf(attributes.getOrDefault("email", ""));
    String name = String.valueOf(attributes.getOrDefault("name", email));
    String avatar = String.valueOf(attributes.getOrDefault("picture", ""));

    UserEntity user = provisioningService.upsertFromGoogle(googleSub, email, name, avatar);
    KairoPrincipal principal = provisioningService.toPrincipal(user);

    Map<String, Object> infoClaims = new HashMap<>();
    if (oidcUser.getUserInfo() != null) {
      infoClaims.putAll(oidcUser.getUserInfo().getClaims());
    }
    infoClaims.put("email", principal.email());
    infoClaims.put("name", principal.name());
    infoClaims.put("avatarUrl", principal.avatarUrl() == null ? "" : principal.avatarUrl());
    infoClaims.put("userId", principal.id().toString());

    return new DefaultOidcUser(
        principal.authorities(),
        oidcUser.getIdToken(),
        new OidcUserInfo(infoClaims),
        "sub");
  }
}
