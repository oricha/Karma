#!/usr/bin/env bash
# Arranca el backend con el perfil local cargando credenciales desde el .env raiz.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ROOT_DIR="$(cd "${BACKEND_DIR}/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
else
  echo "WARN: no se encontro ${ENV_FILE}; usando variables de entorno actuales."
fi

cd "${BACKEND_DIR}"
exec ./gradlew bootRun
