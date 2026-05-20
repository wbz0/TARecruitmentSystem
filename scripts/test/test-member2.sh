#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member2 展示 TA 档案、CSV/路径存储和 DeepSeek 推荐配置降级。
MEMBER="member2"
prepare_backend_member_test "$MEMBER"
compile_backend_member_test "$MEMBER" "backend/test/Member2BackendTest.java"
run_backend_member_test "$MEMBER" "Member2BackendTest"
