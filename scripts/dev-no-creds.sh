#!/usr/bin/env bash
# Local full stack without Neon / Google credentials.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env.local ]]; then
  cat > .env.local <<'EOF'
VITE_USE_MOCK_API=false
VITE_API_BASE_URL=
EOF
  echo "Created .env.local (API mode via Vite proxy)"
fi

export PATH="$(brew --prefix openjdk@21 2>/dev/null)/bin:${PATH:-}"
export JAVA_HOME="$(brew --prefix openjdk@21 2>/dev/null || true)"
export KAIRO_AUTH_MOCK=true
export FRONTEND_URL=http://localhost:5173
export CORS_ORIGINS=http://localhost:5173

echo "Starting API on :8080 (H2 + mock auth)..."
(cd api && ./mvnw -q spring-boot:run) &
API_PID=$!

cleanup() {
  kill "$API_PID" 2>/dev/null || true
}
trap cleanup EXIT

for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/api/health >/dev/null; then
    echo "API is up."
    break
  fi
  sleep 1
  if [[ "$i" -eq 60 ]]; then
    echo "API failed to start" >&2
    exit 1
  fi
done

echo "Starting Vite on :5173 ..."
echo "Open http://localhost:5173 — Entrar con Google usa mock del backend."
npm run dev
