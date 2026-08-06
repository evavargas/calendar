package app.kairo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HexColorValidator implements ConstraintValidator<HexColor, String> {

  private static final String PATTERN = "^#[0-9A-Fa-f]{6}$";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // use @NotBlank when required
    }
    return value.matches(PATTERN);
  }
}
