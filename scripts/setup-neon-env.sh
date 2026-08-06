#!/usr/bin/env bash
# Uso:
#   1) En Neon, copiá el Connection string completo
#   2) Corré:  ./scripts/setup-neon-env.sh
#   3) Pegá el string y Enter, luego Ctrl+D
#
# Escribe api/.env.local listo para Spring (JDBC).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/api/.env.local"

echo "Pegá el connection string de Neon (postgresql://...) y después Ctrl+D:"
URI="$(cat | tr -d '\n' | tr -d ' ')"

if [[ ! "$URI" =~ ^postgres(ql)?:// ]]; then
  echo "No parece un connection string de Postgres/Neon." >&2
  exit 1
fi

# postgresql://user:pass@host/db?sslmode=require
REST="${URI#*://}"
USERINFO="${REST%%@*}"
HOSTPATH="${REST#*@}"
USER="${USERINFO%%:*}"
PASS="${USERINFO#*:}"
HOSTPORT="${HOSTPATH%%/*}"
DBQUERY="${HOSTPATH#*/}"
DB="${DBQUERY%%\?*}"
QUERY="${DBQUERY#*\?}"
if [[ "$DBQUERY" == "$QUERY" ]]; then
  QUERY="sslmode=require"
fi

JDBC="jdbc:postgresql://${HOSTPORT}/${DB}?${QUERY}"

cat > "$OUT" <<EOF
# Generado por scripts/setup-neon-env.sh — no commitear
DATABASE_URL=${JDBC}
DATABASE_USERNAME=${USER}
DATABASE_PASSWORD=${PASS}

KAIRO_AUTH_MOCK=true
FRONTEND_URL=http://localhost:5173
CORS_ORIGINS=http://localhost:5173
COOKIE_SECURE=false
KAIRO_TOKEN_SECRET=local-dev-token-secret-change-me
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:8080/api/google/callback
EOF

chmod 600 "$OUT"
echo "Listo: $OUT"
echo "Probá con:  cd api && set -a && source .env.local && set +a && ./mvnw spring-boot:run"
