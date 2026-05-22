#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# Wang Bangzhen demonstrates overall architecture constraints, centralized API routes, and project docs/shell integrity.
run_node_contributor_test "wang-bangzhen" "Wang Bangzhen" "frontend/test/wang-bangzhen-architecture-test.js"
