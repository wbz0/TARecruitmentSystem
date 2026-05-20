#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member4 展示申请流程、状态流转、通知和管理员邀请码模型。
MEMBER="member4"
prepare_backend_member_test "$MEMBER"
compile_backend_member_test "$MEMBER" "backend/test/Member4BackendTest.java"
run_backend_member_test "$MEMBER" "Member4BackendTest"
