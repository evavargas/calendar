package app.kairo.common;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> handleApi(ApiException ex) {
    return ResponseEntity.status(ex.getStatus())
        .body(Map.of("error", Map.of("code", ex.getCode(), "message", ex.getMessage())));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("Datos inválidos");
    return badRequest("validation_error", message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException ex) {
    return badRequest(
        "validation_error", "Cuerpo inválido: revisá tipos de dato (fechas, UUIDs, enums).");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String name = ex.getName() == null ? "parámetro" : ex.getName();
    return badRequest("validation_error", "Valor inválido para '" + name + "'.");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
    return badRequest(
        "validation_error", "Falta el parámetro requerido '" + ex.getParameterName() + "'.");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleIntegrity(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            Map.of(
                "error",
                Map.of(
                    "code",
                    "conflict",
                    "message",
                    "Conflicto de datos (posible nombre duplicado u otra restricción).")));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", Map.of("code", "internal_error", "message", "Error interno")));
  }

  private static ResponseEntity<?> badRequest(String code, String message) {
    return ResponseEntity.badRequest().body(Map.of("error", Map.of("code", code, "message", message)));
  }
}
