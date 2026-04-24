#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
FRONTEND_DIR="${ROOT_DIR}/lottery-platform-frontend"

cd "${FRONTEND_DIR}"
echo "[frontend] installing dependencies"
npm ci
echo "[frontend] building application"
npm run build
