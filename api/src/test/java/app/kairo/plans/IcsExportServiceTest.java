package app.kairo.plans;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IcsExportServiceTest {

  private final IcsExportService service = new IcsExportService();

  @Test
  void exportsValidCalendarPayload() {
    PlanEntity plan =
        new PlanEntity(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Kickoff",
            "Demo Kairo",
            Instant.parse("2026-08-10T12:00:00Z"),
            Instant.parse("2026-08-10T13:00:00Z"),
            false,
            PlanStatus.planned);

    String ics = new String(service.export(List.of(plan)));

    assertThat(ics).contains("BEGIN:VCALENDAR");
    assertThat(ics).contains("SUMMARY:Kickoff");
    assertThat(ics).contains("DESCRIPTION:Demo Kairo");
    assertThat(ics).contains("END:VCALENDAR");
  }
}
