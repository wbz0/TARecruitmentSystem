#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Sun Jialu demonstrates application flow, status transitions, notifications, and the admin invite-code model.
CONTRIBUTOR_ID="sun-jialu"
CONTRIBUTOR_LABEL="Sun Jialu"
prepare_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL"
compile_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "backend/test/SunJialuBackendTest.java"
run_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "SunJialuBackendTest"
