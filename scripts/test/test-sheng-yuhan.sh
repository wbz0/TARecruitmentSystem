#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Sheng Yuhan demonstrates frontend page script syntax, shared API route usage, and retired legacy pages.
run_node_contributor_test "sheng-yuhan" "Sheng Yuhan" "frontend/test/sheng-yuhan-frontend-test.js"
