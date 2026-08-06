package app.kairo.types;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "event_types")
public class EventTypeEntity {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(nullable = false, length = 16)
  private String color;

  @Column(length = 40)
  private String icon;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  protected EventTypeEntity() {}

  public EventTypeEntity(UUID id, UUID userId, String name, String color, int sortOrder) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.color = color;
    this.sortOrder = sortOrder;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public String getIcon() {
    return icon;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void update(String name, String color, String icon, Integer sortOrder) {
    if (name != null) this.name = name;
    if (color != null) this.color = color;
    if (icon != null) this.icon = icon;
    if (sortOrder != null) this.sortOrder = sortOrder;
  }
}
