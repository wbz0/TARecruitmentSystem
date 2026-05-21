#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Zhou Bohan demonstrates TA profiles, CSV/path storage, and DeepSeek recommendation config fallback.
CONTRIBUTOR_ID="zhou-bohan"
CONTRIBUTOR_LABEL="Zhou Bohan"
prepare_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL"
compile_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "backend/test/ZhouBohanBackendTest.java"
run_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "ZhouBohanBackendTest"
