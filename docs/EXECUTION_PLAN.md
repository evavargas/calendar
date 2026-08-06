# Kairo — Plan de ejecución

Planner personal (no calendario-first): landing → Google login → planes tipados → ICS / Google Calendar.

**Stack objetivo**

| Capa | Tecnología | Deploy (demo / free tier) |
|------|------------|---------------------------|
| Frontend | Vue 3.5 + Vite 8 + Pinia + Vue Router | Vercel |
| Backend | Spring Boot 3 · Java 21 | Google Cloud Run |
| Base de datos | PostgreSQL | Neon (o Supabase solo como Postgres) |
| Auth | Google OAuth 2.0 (Spring Security) | — |
| ICS | ical4j en el backend | — |
| Google Calendar | Client oficial Java (push primero; sync después) | — |

Producto: **Kairo**.

---

## Fases

### Fase 0 — Fundación (frontend — hecha)

- [x] Contrato de API y modelo de dominio
- [x] Reconstruir frontend: router, stores, `services/api`, layout
- [x] Landing tuneada (marca primero)
- [x] Flujo login Google (redirect al backend; mock local)
- [x] Vistas planner: lista, detalle, crear/editar, tipos
- [x] Calendario como vista secundaria
- [x] UI export ICS + conectar Google (stubs hasta backend)
- [x] Eliminar stack legacy (Firebase repo, calendar-first)

### Fase 1 — Backend + DB (`api/`)

1. [x] Módulo `api/` Spring Boot en el monorepo
2. [x] Postgres local via docker-compose (Neon en prod — credenciales tuyas)
3. [x] Flyway: `users`, `event_types`, `plans`, `google_connections`, `google_event_links`
4. [x] Spring Security OAuth2 Login (Google) + sesión; mock local
5. [x] CRUD tipado: types + plans (ownership por `user_id`)
6. [x] `GET /api/plans/{id}/ics` y `GET /api/plans/export.ics`
7. [x] Dockerfile multi-stage listo para Cloud Run
8. [x] Tests JUnit + integración MockMvc

### Fase 2 — Google Calendar light

1. [x] Consent incremental (`calendar.events`) + state en sesión
2. [x] Refresh token cifrado (AES-GCM / `KAIRO_TOKEN_SECRET`)
3. [x] `POST /api/plans/{id}/google` → Calendar API (mock si `KAIRO_AUTH_MOCK`)
4. [x] Link `plan_id` ↔ `google_event_id`

### Fase 3 — Sync (opcional)

1. `syncToken` + reconciliación.
2. Push notifications de Google (si el free tier lo permite).
3. Resolución simple de conflictos (last-write-wins documentado).

### Fase 4 — Pulido demo

1. README de arquitectura + capturas.
2. CI: front (lint/test/build) + backend (test/package).
3. Variables de entorno documentadas; sin secrets en repo.

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
| `event_types` | `id`, `user_id`, `name`, `color`, `icon?`, `sort_order` |
| `plans` | `id`, `user_id`, `type_id`, `title`, `description`, `starts_at`, `ends_at`, `all_day`, `status` (`planned`\|`done`\|`cancelled`), `created_at`, `updated_at` |
| `google_connections` | `user_id`, `refresh_token_enc`, `scopes`, `connected_at` |
| `google_event_links` | `plan_id`, `google_event_id`, `calendar_id` |

Tipos seed por usuario al primer login: Trabajo, Personal, Salud, Estudio (colores fijos del sistema de diseño).

---

## Contrato de API (v1)

Base: `VITE_API_BASE_URL` (ej. `https://kairo-api-xxxxx.run.app`).  
Auth: cookie/sesión o `Authorization: Bearer <jwt>` tras el callback OAuth.

### Auth

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/auth/google` | Redirect a Google |
| `GET` | `/api/auth/google/callback` | Callback; setea sesión; redirect al front |
| `GET` | `/api/auth/me` | Usuario actual o `401` |
| `POST` | `/api/auth/logout` | Cierra sesión |

### Event types

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/types` | Listar tipos del usuario |
| `POST` | `/api/types` | Crear tipo |
| `PATCH` | `/api/types/{id}` | Actualizar |
| `DELETE` | `/api/types/{id}` | Borrar (solo si no hay planes, o reasignar) |

### Plans

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/plans` | Query: `status`, `from`, `to`, `typeId` |
| `GET` | `/api/plans/{id}` | Detalle |
| `POST` | `/api/plans` | Crear |
| `PATCH` | `/api/plans/{id}` | Actualizar |
| `DELETE` | `/api/plans/{id}` | Borrar |
| `GET` | `/api/plans/{id}/ics` | Descargar un plan `.ics` |
| `GET` | `/api/plans/export.ics` | Exportar filtro actual |

### Google Calendar

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/google/status` | ¿Conectado? scopes |
| `GET` | `/api/google/connect` | OAuth incremental Calendar |
| `DELETE` | `/api/google/connect` | Desconectar |
| `POST` | `/api/plans/{id}/google` | Push plan → Google Calendar |

Errores: JSON `{ "error": { "code", "message" } }` + HTTP 4xx/5xx.

---

## Arquitectura frontend

```text
src/
  app/           # bootstrap, App shell, router
  views/         # páginas (landing, planner, detail, types, calendar, settings)
  components/    # UI por dominio (layout, plans, types, auth)
  stores/        # Pinia: auth, plans, types
  services/api/  # cliente HTTP + módulos por recurso
  composables/   # hooks de UI
  mocks/         # datos locales si VITE_USE_MOCK_API=true
  styles/        # tokens + base
```

- El front **nunca** habla con Postgres ni con Google Calendar directo para datos.
- Con `VITE_USE_MOCK_API=true` se puede demear sin Cloud Run.
- Rutas protegidas: meta `requiresAuth`; guard redirige a landing/login.

## Arquitectura backend (Fase 1 — lista en `api/`)

```text
api/
  auth/            # OAuth Google + mock local + /me + logout
  users/
  types/
  plans/           # CRUD + IcsExportService
  google/          # status/connect/disconnect + push stub (Fase 2 real)
  common/          # ApiException
  config/          # Security, CORS, properties
  db/migration/    # Flyway V1
```

Capas: Controller → Service → Repository (Spring Data JPA).

---

## Orden de trabajo inmediato

1. Reconstruir front según este contrato (mock API).
2. Scaffold Spring Boot + Neon + Auth Google.
3. Apuntar `VITE_API_BASE_URL` al API real; apagar mocks.
4. ICS real → push Google → (opcional) sync.
