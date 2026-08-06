package app.kairo.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  private UUID id;

  @Column(name = "google_sub", nullable = false, unique = true, length = 128)
  private String googleSub;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "avatar_url", length = 1000)
  private String avatarUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected UserEntity() {}

  public UserEntity(UUID id, String googleSub, String email, String name, String avatarUrl) {
    this.id = id;
    this.googleSub = googleSub;
    this.email = email;
    this.name = name;
    this.avatarUrl = avatarUrl;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getGoogleSub() {
    return googleSub;
  }

  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void updateProfile(String email, String name, String avatarUrl) {
    this.email = email;
    this.name = name;
    this.avatarUrl = avatarUrl;
  }
}
