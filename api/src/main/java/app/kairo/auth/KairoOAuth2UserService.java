package app.kairo.auth;

import app.kairo.users.UserEntity;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class KairoOAuth2UserService extends DefaultOAuth2UserService {

  private final UserProvisioningService provisioningService;

  public KairoOAuth2UserService(UserProvisioningService provisioningService) {
    this.provisioningService = provisioningService;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauthUser = super.loadUser(userRequest);
    Map<String, Object> attributes = oauthUser.getAttributes();

    String googleSub = String.valueOf(attributes.get("sub"));
    String email = String.valueOf(attributes.getOrDefault("email", ""));
    String name = String.valueOf(attributes.getOrDefault("name", email));
    String avatar = String.valueOf(attributes.getOrDefault("picture", ""));

    UserEntity user = provisioningService.upsertFromGoogle(googleSub, email, name, avatar);
    KairoPrincipal principal = provisioningService.toPrincipal(user);

    return new DefaultOAuth2User(
        principal.authorities(),
        Map.of(
            "sub", principal.id().toString(),
            "email", principal.email(),
            "name", principal.name(),
            "avatarUrl", principal.avatarUrl() == null ? "" : principal.avatarUrl(),
            "userId", principal.id().toString()),
        "sub");
  }

  public UsernamePasswordAuthenticationToken authenticationFor(KairoPrincipal principal) {
    return new UsernamePasswordAuthenticationToken(
        principal, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
  }
}
