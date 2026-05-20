#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member1 展示后端基础能力：用户、认证、公共 service 结果和安全工具。
MEMBER="member1"
prepare_backend_member_test "$MEMBER"
compile_backend_member_test "$MEMBER" "backend/test/Member1BackendTest.java"
run_backend_member_test "$MEMBER" "Member1BackendTest"
