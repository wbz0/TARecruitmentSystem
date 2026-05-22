#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Liu Tengyi demonstrates job validation, the job DAO, and account profile validation.
CONTRIBUTOR_ID="liu-tengyi"
CONTRIBUTOR_LABEL="Liu Tengyi"
prepare_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL"
compile_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "backend/test/LiuTengyiBackendTest.java"
run_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "LiuTengyiBackendTest"
