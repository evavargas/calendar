# Kairo API

Spring Boot 3 · Java 21 · PostgreSQL (Neon en prod, H2 en local) · Google OAuth.

## Arranque local

```sh
cd api
./mvnw spring-boot:run
```

Por defecto:
- H2 en memoria
- `KAIRO_AUTH_MOCK=true` → `GET /api/auth/google` crea sesión demo sin Google
- CORS: `http://localhost:5173`
- Puerto: `8080`

El frontend puede proxyar `/api` a este servicio (ver `vite.config.js`).

## Variables

| Variable | Descripción |
|----------|-------------|
| `DATABASE_URL` | JDBC URL Postgres (Neon) |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciales DB |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | OAuth login |
| `FRONTEND_URL` | Redirect post-login (ej. `http://localhost:5173`) |
| `CORS_ORIGINS` | Orígenes permitidos, separados por coma |
| `KAIRO_AUTH_MOCK` | `true` en local; `false` en prod |
| `COOKIE_SECURE` | `true` detrás de HTTPS |

Perfil prod: `--spring.profiles.active=prod`

## Endpoints

Ver [docs/EXECUTION_PLAN.md](../docs/EXECUTION_PLAN.md).

## Tests

```sh
./mvnw test
```

## Google Calendar (Fase 2)

1. En Google Cloud Console creá OAuth client (Web).
2. Authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google` (login)
   - `http://localhost:8080/api/google/callback` (Calendar incremental)
3. Set `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `KAIRO_AUTH_MOCK=false`.
4. `KAIRO_TOKEN_SECRET` cifra refresh tokens en DB.
5. Flujo: Ajustes → Conectar Google Calendar → consent → push desde detalle del plan.

Con `KAIRO_AUTH_MOCK=true` el connect/push quedan mockeados (útil sin credenciales).

## Postgres local

```sh
docker compose up -d db
cd api && SPRING_PROFILES_ACTIVE=docker ./mvnw spring-boot:run
```

## Deploy (Cloud Run)

1. `docker build -t kairo-api ./api` (Dockerfile multi-stage)
2. Push a Artifact Registry
3. Env: Neon + Google OAuth + `FRONTEND_URL` + `CORS_ORIGINS`
4. `KAIRO_AUTH_MOCK=false`, `COOKIE_SECURE=true`, `SPRING_PROFILES_ACTIVE=prod`
