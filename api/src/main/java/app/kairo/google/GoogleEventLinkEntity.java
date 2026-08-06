package app.kairo.google;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "google_event_links")
public class GoogleEventLinkEntity {

  @Id
  @Column(name = "plan_id")
  private UUID planId;

  @Column(name = "google_event_id", nullable = false, length = 256)
  private String googleEventId;

  @Column(name = "calendar_id", nullable = false, length = 256)
  private String calendarId;

  protected GoogleEventLinkEntity() {}

  public GoogleEventLinkEntity(UUID planId, String googleEventId, String calendarId) {
    this.planId = planId;
    this.googleEventId = googleEventId;
    this.calendarId = calendarId;
  }

  public UUID getPlanId() {
    return planId;
  }

  public String getGoogleEventId() {
    return googleEventId;
  }

  public String getCalendarId() {
    return calendarId;
  }
}
