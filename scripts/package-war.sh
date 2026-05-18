#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

if ! command -v mvn >/dev/null 2>&1; then
  echo "[ERROR] Maven (mvn) not found. Please install Maven 3.9+ first."
  exit 1
fi

echo "========================================"
echo "   Maven WAR Package Script"
echo "========================================"
echo

cd "${PROJECT_ROOT}"
mvn -DskipTests clean package

echo
echo "========================================"
echo "  WAR package generated successfully"
echo "  Output: ${PROJECT_ROOT}/target/groupproject.war"
echo "========================================"
