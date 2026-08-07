# Kairo

Planner personal: landing → login con Google → planes tipados → export ICS /
Google Calendar opcional. El calendario es una vista secundaria.

- **Demo:** [calendar-bay-kappa.vercel.app](https://calendar-bay-kappa.vercel.app)
- **API:** Cloud Run (proxied same-origin desde Vercel en prod)
- Plan de ejecución: [docs/EXECUTION_PLAN.md](./docs/EXECUTION_PLAN.md)
- API local: [api/README.md](./api/README.md)

## Stack

| Capa | Tecnología | Deploy |
|------|------------|--------|
| Frontend | Vue 3 · Vite · Pinia · Vue Router · vue-i18n | Vercel |
| Backend | Spring Boot 3 · Java 21 | Google Cloud Run |
| DB | PostgreSQL (Neon) / H2 local | — |
| Auth | Google OAuth (Spring Security) | — |
| UI | Tokens semánticos + `Ui*` · light/dark · ES/EN | — |

## Desarrollo sin credenciales

| Modo | Cómo | Datos |
|------|------|--------|
| Solo front | `VITE_USE_MOCK_API=true` + `npm run dev` | `localStorage` |
| Front + API | `./scripts/dev-no-creds.sh` o API `:8080` + `VITE_USE_MOCK_API=false` | H2 + mock OAuth |

```sh
chmod +x scripts/dev-no-creds.sh
./scripts/dev-no-creds.sh
```

“Entrar con Google” y “Conectar Google Calendar” quedan mockeados.
ICS y CRUD son reales contra el API.

Con keys reales: `KAIRO_AUTH_MOCK=false` + Neon + OAuth (ver `api/README.md`).

## Scripts

```sh
npm run dev          # front
npm run lint && npm test && npm run build
npm run api:dev      # Spring Boot (desde raíz)
npm run api:test
```

## Rutas UI

| Ruta | Descripción |
|------|-------------|
| `/` | Landing |
| `/auth/callback` | Callback OAuth (o mock) |
| `/app` | Hoy / planes |
| `/app/plans/new` | Crear plan |
| `/app/plans/:id` | Detalle + ICS + enviar a Google |
| `/app/types` | Tipos (color hex) |
| `/app/calendar` | Vista calendario secundaria |
| `/app/settings` | Sesión + Google Calendar |

Tema (claro/oscuro) e idioma (ES/EN) están en la **nav**, no en Ajustes.

## Validación

- **Front:** `src/composables/useFormValidation.js` (trim, vacíos, maxlength, UUID, fechas, hex). Sin librería de forms.
- **Back:** Bean Validation (`@NotBlank`, `@Size`, `@HexColor`, …) + trim en services; cuerpos/tipos inválidos → **400**.
- Mock API (`src/mocks/store.js`) aplica las mismas reglas.

## Arquitectura front

```text
src/app            bootstrap + router
src/views          páginas
src/components/ui  primitivos Ui*
src/components/layout  AppShell, NavPrefs
src/stores         Pinia
src/services/api   cliente HTTP
src/composables    theme, locale, form validation
src/i18n           es / en
src/mocks          localStorage si VITE_USE_MOCK_API
src/styles         tokens · ui · domains
```
