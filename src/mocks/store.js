const STORAGE_KEY = "kairo.mock.v1";

const defaultTypes = [
  { id: "type-work", name: "Trabajo", color: "#0f7a6c", sortOrder: 1 },
  { id: "type-personal", name: "Personal", color: "#2563eb", sortOrder: 2 },
  { id: "type-health", name: "Salud", color: "#c2410c", sortOrder: 3 },
  { id: "type-study", name: "Estudio", color: "#7c3aed", sortOrder: 4 },
];

const daysFromNow = (offset, hour = 10) => {
  const date = new Date();
  date.setDate(date.getDate() + offset);
  date.setHours(hour, 0, 0, 0);
  return date.toISOString();
};

const seedPlans = () => [
  {
    id: "plan-1",
    typeId: "type-work",
    title: "Kickoff del sprint",
    description: "Definir alcance de la demo Kairo y criterios de listo.",
    startsAt: daysFromNow(1, 9),
    endsAt: daysFromNow(1, 10),
    allDay: false,
    status: "planned",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: "plan-2",
    typeId: "type-study",
    title: "Repaso Spring Security",
    description: "OAuth2 Login + JWT para el backend Java.",
    startsAt: daysFromNow(2, 18),
    endsAt: daysFromNow(2, 19),
    allDay: false,
    status: "planned",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: "plan-3",
    typeId: "type-health",
    title: "Entrenamiento",
    description: "Sesión de fuerza 45 min.",
    startsAt: daysFromNow(0, 7),
    endsAt: daysFromNow(0, 8),
    allDay: false,
    status: "done",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

const readState = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
    /* ignore */
  }
  const state = {
    user: null,
    types: defaultTypes,
    plans: seedPlans(),
    google: { connected: false, scopes: [] },
  };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  return state;
};

const writeState = (state) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
};

const delay = (value) =>
  new Promise((resolve) => {
    window.setTimeout(() => resolve(value), 180);
  });

const requireTrimmed = (value, field) => {
  if (value == null || String(value).trim() === "") {
    throw new Error(`${field}: no puede estar vacío`);
  }
  return String(value).trim();
};

const normalizeHexColor = (color) => {
  if (color == null || String(color).trim() === "") {
    throw new Error("color: no puede estar vacío");
  }
  const trimmed = String(color).trim();
  if (!/^#[0-9A-Fa-f]{6}$/.test(trimmed)) {
    throw new Error("color: debe ser un color hex (#RRGGBB)");
  }
  return trimmed.toLowerCase();
};

const assertValidRange = (startsAt, endsAt) => {
  if (!startsAt || !endsAt) {
    throw new Error("Inicio y fin son obligatorios.");
  }
  const start = new Date(startsAt);
  const end = new Date(endsAt);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    throw new Error("Fechas inválidas.");
  }
  if (end < start) {
    throw new Error("El fin no puede ser anterior al inicio.");
  }
};

export const mockAuth = {
  async me() {
    return delay(readState().user);
  },
  async loginWithGoogle() {
    const state = readState();
    state.user = {
      id: "user-demo",
      email: "eva@example.com",
      name: "Eva Estrella",
      avatarUrl: "",
      demoMode: true,
    };
    writeState(state);
    return delay(state.user);
  },
  async logout() {
    const state = readState();
    state.user = null;
    writeState(state);
    return delay(null);
  },
};

export const mockTypes = {
  async list() {
    return delay([...readState().types].sort((a, b) => a.sortOrder - b.sortOrder));
  },
  async create(payload) {
    const name = requireTrimmed(payload?.name, "name");
    if (name.length > 80) throw new Error("name: demasiado largo");
    const color = normalizeHexColor(payload?.color);
    const state = readState();
    const type = {
      id: crypto.randomUUID(),
      name,
      color,
      sortOrder: state.types.length + 1,
    };
    state.types.push(type);
    writeState(state);
    return delay(type);
  },
  async update(id, payload) {
    const state = readState();
    const index = state.types.findIndex((item) => item.id === id);
    if (index < 0) throw new Error("Tipo no encontrado");
    const next = { ...state.types[index] };
    if (payload.name != null) {
      next.name = requireTrimmed(payload.name, "name");
      if (next.name.length > 80) throw new Error("name: demasiado largo");
    }
    if (payload.color != null) {
      next.color = normalizeHexColor(payload.color);
    }
    state.types[index] = next;
    writeState(state);
    return delay(state.types[index]);
  },
  async remove(id) {
    const state = readState();
    if (state.plans.some((plan) => plan.typeId === id)) {
      throw new Error("No se puede borrar un tipo con planes asociados.");
    }
    state.types = state.types.filter((item) => item.id !== id);
    writeState(state);
    return delay(null);
  },
};

export const mockPlans = {
  async list(filters = {}) {
    let plans = [...readState().plans];
    if (filters.status) {
      plans = plans.filter((plan) => plan.status === filters.status);
    }
    if (filters.typeId) {
      plans = plans.filter((plan) => plan.typeId === filters.typeId);
    }
    if (filters.from) {
      const from = new Date(filters.from).getTime();
      plans = plans.filter((plan) => new Date(plan.startsAt).getTime() >= from);
    }
    if (filters.to) {
      const to = new Date(filters.to).getTime();
      plans = plans.filter((plan) => new Date(plan.startsAt).getTime() <= to);
    }
    plans.sort((a, b) => new Date(a.startsAt) - new Date(b.startsAt));
    return delay(plans);
  },
  async get(id) {
    const plan = readState().plans.find((item) => item.id === id);
    if (!plan) throw new Error("Plan no encontrado");
    return delay(plan);
  },
  async create(payload) {
    const state = readState();
    const title = requireTrimmed(payload?.title, "title");
    if (title.length > 160) throw new Error("title: demasiado largo");
    if (!payload?.typeId) throw new Error("typeId: obligatorio");
    if (!state.types.some((type) => type.id === payload.typeId)) {
      throw new Error("Tipo inválido");
    }
    const startsAt = payload.startsAt;
    const endsAt = payload.endsAt;
    assertValidRange(startsAt, endsAt);
    const description =
      payload.description == null ? "" : String(payload.description).trim();
    if (description.length > 4000) throw new Error("description: demasiado largo");
    const plan = {
      id: crypto.randomUUID(),
      typeId: payload.typeId,
      title,
      description,
      startsAt,
      endsAt,
      allDay: Boolean(payload.allDay),
      status: "planned",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    state.plans.push(plan);
    writeState(state);
    return delay(plan);
  },
  async update(id, payload) {
    const state = readState();
    const index = state.plans.findIndex((item) => item.id === id);
    if (index < 0) throw new Error("Plan no encontrado");
    const current = state.plans[index];
    const next = { ...current };
    if (payload.title != null) {
      next.title = requireTrimmed(payload.title, "title");
      if (next.title.length > 160) throw new Error("title: demasiado largo");
    }
    if (payload.typeId != null) {
      if (!state.types.some((type) => type.id === payload.typeId)) {
        throw new Error("Tipo inválido");
      }
      next.typeId = payload.typeId;
    }
    if (payload.description != null) {
      next.description = String(payload.description).trim();
      if (next.description.length > 4000) {
        throw new Error("description: demasiado largo");
      }
    }
    if (payload.startsAt != null) next.startsAt = payload.startsAt;
    if (payload.endsAt != null) next.endsAt = payload.endsAt;
    if (payload.allDay != null) next.allDay = Boolean(payload.allDay);
    if (payload.status != null) {
      if (!["planned", "done", "cancelled"].includes(payload.status)) {
        throw new Error("Estado inválido");
      }
      next.status = payload.status;
    }
    assertValidRange(next.startsAt, next.endsAt);
    next.updatedAt = new Date().toISOString();
    state.plans[index] = next;
    writeState(state);
    return delay(state.plans[index]);
  },
  async remove(id) {
    const state = readState();
    state.plans = state.plans.filter((item) => item.id !== id);
    writeState(state);
    return delay(null);
  },
};

export const mockGoogle = {
  async status() {
    return delay(readState().google);
  },
  async connect() {
    const state = readState();
    state.google = {
      connected: true,
      scopes: ["https://www.googleapis.com/auth/calendar.events"],
    };
    writeState(state);
    return delay(state.google);
  },
  async disconnect() {
    const state = readState();
    state.google = { connected: false, scopes: [] };
    writeState(state);
    return delay(state.google);
  },
  async pushPlan(planId) {
    const plan = readState().plans.find((item) => item.id === planId);
    if (!plan) throw new Error("Plan no encontrado");
    if (!readState().google.connected) {
      throw new Error("Conectá Google Calendar primero.");
    }
    return delay({
      planId,
      googleEventId: `mock-gcal-${planId}`,
      htmlLink: "https://calendar.google.com/",
    });
  },
};

const escapeIcs = (value) =>
  String(value || "")
    .replace(/\\/g, "\\\\")
    .replace(/\n/g, "\\n")
    .replace(/,/g, "\\,")
    .replace(/;/g, "\\;");

const toIcsDate = (iso, allDay) => {
  const date = new Date(iso);
  if (allDay) {
    return date.toISOString().slice(0, 10).replace(/-/g, "");
  }
  return date.toISOString().replace(/[-:]/g, "").replace(/\.\d{3}Z$/, "Z");
};

export const mockIcs = {
  async exportPlans(plans) {
    const lines = [
      "BEGIN:VCALENDAR",
      "VERSION:2.0",
      "PRODID:-//Kairo//Planner//ES",
      "CALSCALE:GREGORIAN",
    ];
    for (const plan of plans) {
      lines.push("BEGIN:VEVENT");
      lines.push(`UID:${plan.id}@kairo.app`);
      lines.push(`DTSTAMP:${toIcsDate(new Date().toISOString(), false)}`);
      lines.push(
        plan.allDay
          ? `DTSTART;VALUE=DATE:${toIcsDate(plan.startsAt, true)}`
          : `DTSTART:${toIcsDate(plan.startsAt, false)}`
      );
      lines.push(
        plan.allDay
          ? `DTEND;VALUE=DATE:${toIcsDate(plan.endsAt, true)}`
          : `DTEND:${toIcsDate(plan.endsAt, false)}`
      );
      lines.push(`SUMMARY:${escapeIcs(plan.title)}`);
      if (plan.description) {
        lines.push(`DESCRIPTION:${escapeIcs(plan.description)}`);
      }
      lines.push("END:VEVENT");
    }
    lines.push("END:VCALENDAR");
    return delay(new Blob([lines.join("\r\n")], { type: "text/calendar" }));
  },
};
