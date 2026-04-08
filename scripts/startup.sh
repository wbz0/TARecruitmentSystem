#!/usr/bin/env bash
set -euo pipefail

# ========================================
# Start Tomcat Script (macOS/Linux)
# ========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/config.sh" ]]; then
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/config.sh"
else
  echo "[ERROR] Missing scripts/config.sh"
  echo "Please run:"
  echo "  cd scripts && cp config.example.sh config.sh"
  exit 1
fi

: "${CATALINA_HOME:=${TOMCAT_HOME:-}}"
: "${APP_NAME:=groupproject}"

echo "========================================"
echo "  Start Tomcat (macOS/Linux)"
echo "========================================"
echo

if [[ -z "${CATALINA_HOME}" || ! -d "${CATALINA_HOME}" ]]; then
  echo "[ERROR] Tomcat not found: ${CATALINA_HOME:-<empty>}"
  echo "Please check scripts/config.sh"
  exit 1
fi

"${CATALINA_HOME}/bin/startup.sh"

echo
echo "========================================"
echo "  Tomcat Started!"
echo "========================================"
echo
echo "Access URLs:"
echo "  - Home: http://localhost:8080/${APP_NAME}/"
echo "  - JSP: http://localhost:8080/${APP_NAME}/jsp/welcome.jsp"
echo "  - Servlet: http://localhost:8080/${APP_NAME}/hello"
echo
