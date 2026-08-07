# Kairo — Plan de ejecución

Planner personal (no calendario-first): landing → Google login → planes tipados → ICS / Google Calendar.

**Stack**

| Capa | Tecnología | Deploy |
|------|------------|--------|
| Frontend | Vue 3.5 + Vite 8 + Pinia + Vue Router + vue-i18n | Vercel (`calendar-bay-kappa.vercel.app`) |
| Backend | Spring Boot 3 · Java 21 | Cloud Run (`kairo-api-…us-east1.run.app`) |
| Base de datos | PostgreSQL | Neon (H2 en local / mock) |
| Auth | Google OAuth 2.0 (Spring Security) | Same-origin rewrites Vercel → Cloud Run |
| ICS | ical4j | — |
| Google Calendar | Client Java (push); sync opcional | — |

Producto: **Kairo** (marca teal + Syne/Manrope).

---

## Fases

### Fase 0 — Fundación frontend — hecha

- [x] Contrato de API y modelo de dominio
- [x] Router, stores, `services/api`, layout
- [x] Landing (marca primero)
- [x] Login Google (redirect / mock local)
- [x] Planner: lista, detalle, crear/editar, tipos, Hoy
- [x] Calendario como vista secundaria
- [x] UI export ICS + Google Calendar
- [x] Sin Firebase / calendar-first legacy

### Fase 1 — Backend + DB (`api/`) — hecha

- [x] Spring Boot en monorepo + Flyway + CRUD tipado
- [x] OAuth2 Login + sesión; mock local (`KAIRO_AUTH_MOCK`)
- [x] ICS (`/api/plans/{id}/ics`, `/api/plans/export.ics`)
- [x] Dockerfile Cloud Run + tests JUnit/MockMvc
- [x] Specs JPA para filtros de planes (fix null Instant en Postgres)
- [x] Validación Bean Validation + `ApiExceptionHandler` (400 tipados)

### Fase 2 — Google Calendar light — hecha

- [x] Consent incremental `calendar.events`
- [x] Refresh token cifrado (`KAIRO_TOKEN_SECRET`)
- [x] `POST /api/plans/{id}/google` + link `plan_id` ↔ `google_event_id`

### Fase 2.5 — Producto UI / design system — hecha

- [x] Tokens semánticos + componentes `Ui*` + light/dark (`data-theme`)
- [x] i18n ES/EN (`vue-i18n`); tema e idioma en nav (`NavPrefs`)
- [x] Validación de formularios front (`useFormValidation`) alineada al API
- [x] OAuth prod: cookies + rewrites same-origin; OIDC Google endurecido

### Fase 3 — Sync (opcional / pendiente)

1. `syncToken` + reconciliación.
2. Push notifications de Google (si el free tier lo permite).
3. Conflictos last-write-wins documentado.

### Fase 4 — Pulido demo (parcial)

1. [x] README + plan de ejecución actualizados
2. [x] CI: `.github/workflows/ci.yml` (lint/test/build + `mvnw test package`)
3. [x] Variables documentadas; sin secrets en repo
4. [ ] Capturas / polish UX restante (skeletons, edit tipo, tabs mobile, etc.)

---

## Modelo de dominio

```text
User 1──* EventType 1──* Plan
User 1──0..1 GoogleConnection
Plan  1──0..1 GoogleEventLink
```

| Entidad | Campos clave |
|---------|----------------|
| `users` | `id`, `google_sub`, `email`, `name`, `avatar_url`, `created_at` |
| `event_types` | `id`, `user_id`, `name`, `color` (#RRGGBB), `icon?`, `sort_order` |
| `plans` | `id`, `user_id`, `type_id`, `title` (≤160), `description` (≤4000), `starts_at`, `ends_at`, `all_day`, `status` (`planned`\|`done`\|`cancelled`) |
| `google_connections` | `user_id`, `refresh_token_enc`, `scopes`, `connected_at` |
| `google_event_links` | `plan_id`, `google_event_id`, `calendar_id` |

Tipos seed al primer login: Trabajo, Personal, Salud, Estudio.

---

## Contrato de API (v1)

En **producción** el front usa same-origin (`/api`, `/oauth2`, `/login/oauth2` reescritos a Cloud Run). Local: proxy Vite o `VITE_API_BASE_URL`.

Auth: cookie de sesión (SameSite / Secure / domain según host).

### Auth

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/oauth2/authorization/google` | Inicio OAuth (también vía `/api/auth/google`) |
| `GET` | `/api/auth/me` | Usuario actual o sin sesión |
| `POST` | `/api/auth/logout` | Cierra sesión |

### Event types

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/types` | Listar |
| `POST` | `/api/types` | Crear (`name` NotBlank ≤80, `color` hex) |
| `PATCH` | `/api/types/{id}` | Actualizar |
| `DELETE` | `/api/types/{id}` | Borrar si no hay planes asociados |

### Plans

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/plans` | Query: `status`, `from`, `to`, `typeId` |
| `GET` | `/api/plans/{id}` | Detalle |
| `POST` | `/api/plans` | Crear (title, typeId, fechas obligatorias) |
| `PATCH` | `/api/plans/{id}` | Actualizar |
| `DELETE` | `/api/plans/{id}` | Borrar |
| `GET` | `/api/plans/{id}/ics` | Un plan `.ics` |
| `GET` | `/api/plans/export.ics` | Export filtro |

### Google Calendar

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/google/status` | ¿Conectado? |
| `GET` | `/api/google/connect` | OAuth incremental |
| `DELETE` | `/api/google/connect` | Desconectar |
| `POST` | `/api/plans/{id}/google` | Push → Calendar |

Errores: `{ "error": { "code", "message" } }` + HTTP 4xx/5xx.  
Validación / JSON ilegible / type mismatch → **400** `validation_error`. Integridad → **409**.

---

## Arquitectura frontend

```text
src/
  app/              # App + router
  views/            # landing, today, planner, detail, form, types, calendar, settings
  components/
    ui/             # UiButton, UiChip, UiBadge, …
    layout/         # AppShell, NavPrefs
    plans/          # PlanCard
  stores/           # auth, plans, types, google
  services/api/     # client + módulos
  composables/      # useTheme, useLocale, useFormValidation, format
  i18n/             # es / en
  mocks/            # VITE_USE_MOCK_API (mismas reglas de validación)
  styles/           # tokens · ui · domains
```

- Front no habla con Postgres ni Calendar directo.
- `VITE_USE_MOCK_API=true` → demo en `localStorage`.
- Rutas protegidas: `requiresAuth`.
- Tema / idioma: nav (`NavPrefs`); Ajustes = sesión + Google.

## Arquitectura backend

```text
api/src/main/java/app/kairo/
  auth/       OAuth Google + OIDC + sesión cookie
  users/
  types/      CRUD + HexColor
  plans/      CRUD + ICS + Specifications filter
  google/     connect + push
  validation/ HexColor annotation
  common/     ApiException + ApiExceptionHandler
  config/     Security, CORS, cookies
```

Capas: Controller (`@Valid`) → Service (trim / reglas) → Repository.

---

## Orden de trabajo siguiente

1. Fase 4 restante: CI estable + capturas / polish UX (skeletons, edit tipo, tabs).
2. Fase 3 sync Google solo si hace falta para la demo.
3. Mantener validación front/back al agregar campos nuevos.
