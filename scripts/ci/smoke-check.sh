#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:9008}"
FRONTEND_URL="${2:-http://localhost:9010}"

check() {
  local label="$1"
  local url="$2"
  echo "[smoke] checking ${label}: ${url}"
  curl --fail --silent --show-error "${url}" >/dev/null
}

check "gateway swagger" "${BASE_URL}/swagger-ui.html"
check "user ping" "${BASE_URL}/lottery-user/api/user/ping"
check "activity ping" "${BASE_URL}/lottery-activity/api/activity/ping"
check "lottery ping" "${BASE_URL}/lottery-lottery/api/lottery/ping"
check "award ping" "${BASE_URL}/lottery-award/api/award/ping"
check "pay ping" "${BASE_URL}/lottery-pay/api/pay/ping"
check "workflow ping" "${BASE_URL}/lottery-workflow/api/workflow/ping"
check "file ping" "${BASE_URL}/lottery-file/api/file/ping"
check "monitor ping" "${BASE_URL}/lottery-monitor/api/monitor/ping"
check "frontend entry" "${FRONTEND_URL}/"

echo "[smoke] all checks passed"
