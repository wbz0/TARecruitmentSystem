#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONFIG_FILE="$ROOT_DIR/scripts/config.sh"
OUTPUT_DIR="$ROOT_DIR/docs/deliverables/technical/javadocs"

if [[ -f "$CONFIG_FILE" ]]; then
    # shellcheck disable=SC1090
    source "$CONFIG_FILE" >/dev/null 2>&1 || true
fi

if [[ -z "${TOMCAT_HOME:-}" || ! -f "$TOMCAT_HOME/lib/servlet-api.jar" ]]; then
    echo "TOMCAT_HOME must point to a Tomcat install with lib/servlet-api.jar." >&2
    echo "Copy scripts/config.example.sh to scripts/config.sh and set TOMCAT_HOME first." >&2
    exit 1
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

javadoc -quiet \
    -encoding UTF-8 \
    -charset UTF-8 \
    -docencoding UTF-8 \
    -Xdoclint:all,-missing \
    -sourcepath "$ROOT_DIR/backend/src" \
    -classpath "$TOMCAT_HOME/lib/servlet-api.jar" \
    -d "$OUTPUT_DIR" \
    -subpackages com.example.tarecruitment

echo "JavaDoc generated at: $OUTPUT_DIR/index.html"
