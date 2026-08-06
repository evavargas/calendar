package app.kairo.plans;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

  private final PlanService service;

  public PlanController(PlanService service) {
    this.service = service;
  }

  @GetMapping
  public List<PlanService.PlanResponse> list(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) UUID typeId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    return service.list(status, typeId, from, to);
  }

  @GetMapping("/export.ics")
  public ResponseEntity<byte[]> exportIcs(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) UUID typeId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    byte[] body = service.exportFilteredIcs(status, typeId, from, to);
    return icsResponse(body, "kairo-planes.ics");
  }

  @GetMapping("/{id}")
  public PlanService.PlanResponse get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping("/{id}/ics")
  public ResponseEntity<byte[]> exportOne(@PathVariable UUID id) {
    return icsResponse(service.exportOneIcs(id), "kairo-" + id + ".ics");
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PlanService.PlanResponse create(@Valid @RequestBody PlanService.CreatePlanRequest request) {
    return service.create(request);
  }

  @PatchMapping("/{id}")
  public PlanService.PlanResponse update(
      @PathVariable UUID id, @Valid @RequestBody PlanService.UpdatePlanRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  private ResponseEntity<byte[]> icsResponse(byte[] body, String filename) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(MediaType.parseMediaType("text/calendar"))
        .body(body);
  }
}
