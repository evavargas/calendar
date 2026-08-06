package app.kairo.plans;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class PlanEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "type_id", nullable = false)
  private UUID typeId;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(length = 4000)
  private String description;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at", nullable = false)
  private Instant endsAt;

  @Column(name = "all_day", nullable = false)
  private boolean allDay;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PlanStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PlanEntity() {}

  public PlanEntity(
      UUID id,
      UUID userId,
      UUID typeId,
      String title,
      String description,
      Instant startsAt,
      Instant endsAt,
      boolean allDay,
      PlanStatus status) {
    this.id = id;
    this.userId = userId;
    this.typeId = typeId;
    this.title = title;
    this.description = description;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.allDay = allDay;
    this.status = status;
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getTypeId() {
    return typeId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public Instant getEndsAt() {
    return endsAt;
  }

  public boolean isAllDay() {
    return allDay;
  }

  public PlanStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void apply(
      UUID typeId,
      String title,
      String description,
      Instant startsAt,
      Instant endsAt,
      Boolean allDay,
      PlanStatus status) {
    if (typeId != null) this.typeId = typeId;
    if (title != null) this.title = title;
    if (description != null) this.description = description;
    if (startsAt != null) this.startsAt = startsAt;
    if (endsAt != null) this.endsAt = endsAt;
    if (allDay != null) this.allDay = allDay;
    if (status != null) this.status = status;
    this.updatedAt = Instant.now();
  }
}
