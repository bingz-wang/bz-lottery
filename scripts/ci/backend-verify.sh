#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/lottery-platform-backend"
MAVEN_GOALS="${1:-clean verify}"
MAVEN_ARGS="${MAVEN_ARGS:---batch-mode --errors --no-transfer-progress}"

cd "${BACKEND_DIR}"
echo "[backend] running: mvn ${MAVEN_ARGS} ${MAVEN_GOALS}"
mvn ${MAVEN_ARGS} ${MAVEN_GOALS}
