import { beforeEach, describe, expect, it } from "vitest";
import { mockAuth, mockIcs, mockPlans, mockTypes } from "../mocks/store";

describe("mock planner store", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("logs in and lists seeded plans", async () => {
    const user = await mockAuth.loginWithGoogle();
    expect(user.email).toContain("@");
    const plans = await mockPlans.list();
    expect(plans.length).toBeGreaterThan(0);
    const types = await mockTypes.list();
    expect(types.some((type) => type.name === "Trabajo")).toBe(true);
  });

  it("exports ics content", async () => {
    const plans = await mockPlans.list();
    const blob = await mockIcs.exportPlans(plans.slice(0, 1));
    const text = await blob.text();
    expect(text).toContain("BEGIN:VCALENDAR");
    expect(text).toContain("BEGIN:VEVENT");
    expect(text).toContain(plans[0].title);
  });
});
