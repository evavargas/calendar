package app.kairo.auth;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record KairoPrincipal(UUID id, String email, String name, String avatarUrl)
    implements AuthenticatedPrincipal {

  @Override
  public String getName() {
    return id.toString();
  }

  public Collection<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
