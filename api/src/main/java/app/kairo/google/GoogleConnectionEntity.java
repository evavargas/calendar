package app.kairo.google;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "google_connections")
public class GoogleConnectionEntity {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "refresh_token_enc", nullable = false, length = 4000)
  private String refreshTokenEnc;

  @Column(nullable = false, length = 1000)
  private String scopes;

  @Column(name = "connected_at", nullable = false)
  private Instant connectedAt;

  protected GoogleConnectionEntity() {}

  public GoogleConnectionEntity(UUID userId, String refreshTokenEnc, String scopes) {
    this.userId = userId;
    this.refreshTokenEnc = refreshTokenEnc;
    this.scopes = scopes;
    this.connectedAt = Instant.now();
  }

  public UUID getUserId() {
    return userId;
  }

  public String getRefreshTokenEnc() {
    return refreshTokenEnc;
  }

  public String getScopes() {
    return scopes;
  }

  public Instant getConnectedAt() {
    return connectedAt;
  }

  public void reconnect(String refreshTokenEnc, String scopes) {
    this.refreshTokenEnc = refreshTokenEnc;
    this.scopes = scopes;
    this.connectedAt = Instant.now();
  }
}
