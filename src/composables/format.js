export const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
};

export const formatPlanWhen = (plan, locale = "es-AR") => {
  const start = new Date(plan.startsAt);
  const end = new Date(plan.endsAt);
  if (plan.allDay) {
    return start.toLocaleDateString(locale, {
      weekday: "short",
      day: "numeric",
      month: "short",
    });
  }
  const date = start.toLocaleDateString(locale, {
    weekday: "short",
    day: "numeric",
    month: "short",
  });
  const startTime = start.toLocaleTimeString(locale, {
    hour: "2-digit",
    minute: "2-digit",
  });
  const endTime = end.toLocaleTimeString(locale, {
    hour: "2-digit",
    minute: "2-digit",
  });
  return `${date} · ${startTime}–${endTime}`;
};

export const statusKey = (status) => `status.${status}`;

/** @deprecated Prefer t(statusKey(status)) with vue-i18n */
export const statusLabel = (status) =>
  ({
    planned: "Planificado",
    done: "Hecho",
    cancelled: "Cancelado",
  })[status] || status;

/** Local calendar date as YYYY-MM-DD */
export const toDateKey = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

export const startOfDay = (date) => {
  const next = new Date(date);
  next.setHours(0, 0, 0, 0);
  return next;
};

export const endOfDay = (date) => {
  const next = new Date(date);
  next.setHours(23, 59, 59, 999);
  return next;
};

export const addDays = (date, amount) => {
  const next = new Date(date);
  next.setDate(next.getDate() + amount);
  return next;
};

export const formatDayStripLabel = (date, locale = "es-AR") =>
  date.toLocaleDateString(locale, { weekday: "short" });

export const formatMonthLabel = (date, locale = "es-AR") =>
  date.toLocaleDateString(locale, { month: "long", year: "numeric" });

export const formatHourLabel = (hour, locale = "es-AR") => {
  const date = new Date();
  date.setHours(hour, 0, 0, 0);
  return date.toLocaleTimeString(locale, { hour: "2-digit", minute: "2-digit" });
};

export const planOverlapsDay = (plan, day) => {
  const dayStart = startOfDay(day).getTime();
  const dayEnd = endOfDay(day).getTime();
  const start = new Date(plan.startsAt).getTime();
  const end = new Date(plan.endsAt).getTime();
  return start <= dayEnd && end >= dayStart;
};
