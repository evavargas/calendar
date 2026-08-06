# Kairo

Planner personal: landing → login con Google → planes tipados → export ICS /
Google Calendar opcional. El calendario es una vista secundaria.

Demo frontend (mock API por defecto): Vite + Vue 3.

Plan de ejecución completo: [docs/EXECUTION_PLAN.md](./docs/EXECUTION_PLAN.md)

## Stack objetivo

| Capa | Tecnología | Deploy demo |
|------|------------|-------------|
| Frontend | Vue 3 · Vite · Pinia · Vue Router | Vercel |
| Backend | Spring Boot 3 · Java 21 | Google Cloud Run |
| DB | PostgreSQL | Neon |
| Auth | Google OAuth (Spring Security) | — |

## Desarrollo sin credenciales

Podés armar y probar todo el flujo **sin Neon ni Google Cloud**:

| Modo | Cómo | Datos |
|------|------|--------|
| Solo front | `VITE_USE_MOCK_API=true` + `npm run dev` | `localStorage` |
| Front + API | `scripts/dev-no-creds.sh` o API en `:8080` + `VITE_USE_MOCK_API=false` | H2 + mock OAuth |

```sh
chmod +x scripts/dev-no-creds.sh
./scripts/dev-no-creds.sh
```

“Entrar con Google” y “Conectar Google Calendar” quedan mockeados.
ICS y CRUD son reales contra el API.

Cuando tengas keys: `KAIRO_AUTH_MOCK=false` + Neon + OAuth (ver `api/README.md`).

## Rutas UI

| Ruta | Descripción |
|------|-------------|
| `/` | Landing |
| `/auth/callback` | Callback OAuth (o mock) |
| `/app` | Lista de planes |
| `/app/plans/new` | Crear plan |
| `/app/plans/:id` | Detalle + ICS + enviar a Google |
| `/app/types` | Tipos (color semántico) |
| `/app/calendar` | Vista calendario secundaria |
| `/app/settings` | Sesión + conectar Google Calendar |

## Arquitectura front

```text
src/app          bootstrap + router
src/views        páginas
src/components   UI por dominio
src/stores       Pinia
src/services/api cliente HTTP alineado al contrato del plan
src/mocks        persistencia local mientras no hay backend
```
