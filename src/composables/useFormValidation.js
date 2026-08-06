const HEX_COLOR = /^#[0-9A-Fa-f]{6}$/;

export const isBlank = (value) =>
  value == null || (typeof value === "string" && value.trim() === "");

export const requiredTrimmed = (value) => {
  if (isBlank(value)) return { ok: false };
  return { ok: true, value: String(value).trim() };
};

export const maxLen = (value, max) => {
  if (value == null) return { ok: true, value: "" };
  const text = String(value);
  if (text.length > max) return { ok: false };
  return { ok: true, value: text };
};

export const isHexColor = (value) =>
  typeof value === "string" && HEX_COLOR.test(value.trim());

export const isValidRange = (startsAt, endsAt) => {
  const start = new Date(startsAt);
  const end = new Date(endsAt);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return false;
  }
  return end >= start;
};

export const isUuidLike = (value) =>
  typeof value === "string" &&
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
    value.trim()
  );

/**
 * @param {object} form
 * @param {(key: string) => string} t
 * @returns {{ ok: true, data: object } | { ok: false, message: string }}
 */
export const validatePlanForm = (form, t) => {
  const title = requiredTrimmed(form.title);
  if (!title.ok) {
    return { ok: false, message: t("validation.titleRequired") };
  }
  const titleLen = maxLen(title.value, 160);
  if (!titleLen.ok) {
    return { ok: false, message: t("validation.titleMax") };
  }

  if (isBlank(form.typeId) || !isUuidLike(form.typeId)) {
    return { ok: false, message: t("validation.typeRequired") };
  }

  const description = form.description == null ? "" : String(form.description).trim();
  if (description.length > 4000) {
    return { ok: false, message: t("validation.descriptionMax") };
  }

  if (isBlank(form.startsAt) || isBlank(form.endsAt)) {
    return { ok: false, message: t("validation.datesRequired") };
  }

  if (!isValidRange(form.startsAtIso, form.endsAtIso)) {
    return { ok: false, message: t("validation.rangeInvalid") };
  }

  const allowedStatus = new Set(["planned", "done", "cancelled"]);
  if (form.status != null && form.status !== "" && !allowedStatus.has(form.status)) {
    return { ok: false, message: t("validation.statusInvalid") };
  }

  return {
    ok: true,
    data: {
      title: title.value,
      typeId: form.typeId.trim(),
      description,
      allDay: Boolean(form.allDay),
      startsAt: form.startsAtIso,
      endsAt: form.endsAtIso,
      ...(form.status ? { status: form.status } : {}),
    },
  };
};

/**
 * @param {object} form
 * @param {(key: string) => string} t
 */
export const validateTypeForm = (form, t) => {
  const name = requiredTrimmed(form.name);
  if (!name.ok) {
    return { ok: false, message: t("validation.typeNameRequired") };
  }
  if (name.value.length > 80) {
    return { ok: false, message: t("validation.typeNameMax") };
  }
  if (!isHexColor(form.color)) {
    return { ok: false, message: t("validation.colorInvalid") };
  }
  return {
    ok: true,
    data: {
      name: name.value,
      color: String(form.color).trim().toLowerCase(),
    },
  };
};
