#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member6 展示整体架构约束、API 路径集中化和项目文档/壳层文件完整性。
run_node_member_test "member6" "frontend/test/member6-architecture-test.js"
