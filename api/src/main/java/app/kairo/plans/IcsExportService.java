package app.kairo.plans;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IcsExportService {

  private static final DateTimeFormatter UTC =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
  private static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

  public byte[] export(List<PlanEntity> plans) {
    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\r\n");
    sb.append("VERSION:2.0\r\n");
    sb.append("PRODID:-//Kairo//Planner//ES\r\n");
    sb.append("CALSCALE:GREGORIAN\r\n");
    for (PlanEntity plan : plans) {
      sb.append("BEGIN:VEVENT\r\n");
      sb.append("UID:").append(plan.getId()).append("@kairo.app\r\n");
      sb.append("DTSTAMP:").append(UTC.format(Instant.now())).append("\r\n");
      if (plan.isAllDay()) {
        sb.append("DTSTART;VALUE=DATE:").append(DATE.format(plan.getStartsAt())).append("\r\n");
        sb.append("DTEND;VALUE=DATE:").append(DATE.format(plan.getEndsAt())).append("\r\n");
      } else {
        sb.append("DTSTART:").append(UTC.format(plan.getStartsAt())).append("\r\n");
        sb.append("DTEND:").append(UTC.format(plan.getEndsAt())).append("\r\n");
      }
      sb.append("SUMMARY:").append(escape(plan.getTitle())).append("\r\n");
      if (plan.getDescription() != null && !plan.getDescription().isBlank()) {
        sb.append("DESCRIPTION:").append(escape(plan.getDescription())).append("\r\n");
      }
      sb.append("END:VEVENT\r\n");
    }
    sb.append("END:VCALENDAR\r\n");
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private String escape(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;");
  }
}
