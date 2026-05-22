#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Ouyang Xiaojun demonstrates backend foundations: users, authentication, shared service results, and security utilities.
CONTRIBUTOR_ID="ouyang-xiaojun"
CONTRIBUTOR_LABEL="Ouyang Xiaojun"
prepare_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL"
compile_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "backend/test/OuyangXiaojunBackendTest.java"
run_backend_contributor_test "$CONTRIBUTOR_ID" "$CONTRIBUTOR_LABEL" "OuyangXiaojunBackendTest"
