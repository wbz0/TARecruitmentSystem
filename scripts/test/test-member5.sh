#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member5 展示前端页面脚本语法、公共 API 路由调用和旧页面下线状态。
run_node_member_test "member5" "frontend/test/member5-frontend-test.js"
