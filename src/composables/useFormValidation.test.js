import { describe, expect, it } from "vitest";
import {
  isHexColor,
  isUuidLike,
  isValidRange,
  requiredTrimmed,
  validatePlanForm,
  validateTypeForm,
} from "../composables/useFormValidation";

const t = (key) => key;

describe("useFormValidation", () => {
  it("rejects blank titles", () => {
    expect(requiredTrimmed("   ").ok).toBe(false);
    expect(requiredTrimmed("Hola").value).toBe("Hola");
  });

  it("validates hex colors", () => {
    expect(isHexColor("#0f7a6c")).toBe(true);
    expect(isHexColor("#fff")).toBe(false);
    expect(isHexColor("red")).toBe(false);
  });

  it("validates uuid-like ids", () => {
    expect(isUuidLike("550e8400-e29b-41d4-a716-446655440000")).toBe(true);
    expect(isUuidLike("nope")).toBe(false);
  });

  it("validates date ranges", () => {
    expect(
      isValidRange("2026-08-06T12:00:00.000Z", "2026-08-06T13:00:00.000Z")
    ).toBe(true);
    expect(
      isValidRange("2026-08-06T14:00:00.000Z", "2026-08-06T13:00:00.000Z")
    ).toBe(false);
  });

  it("validates plan form payload", () => {
    const result = validatePlanForm(
      {
        title: "  Kickoff ",
        typeId: "550e8400-e29b-41d4-a716-446655440000",
        description: "x",
        allDay: false,
        startsAt: "2026-08-06T10:00",
        endsAt: "2026-08-06T11:00",
        startsAtIso: "2026-08-06T13:00:00.000Z",
        endsAtIso: "2026-08-06T14:00:00.000Z",
      },
      t
    );
    expect(result.ok).toBe(true);
    expect(result.data.title).toBe("Kickoff");
  });

  it("rejects type form with bad color", () => {
    const result = validateTypeForm({ name: "Trabajo", color: "teal" }, t);
    expect(result.ok).toBe(false);
    expect(result.message).toBe("validation.colorInvalid");
  });
});
