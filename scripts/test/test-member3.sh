#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/test-common.sh"

# member3 展示职位校验、职位 DAO 和账号资料校验。
MEMBER="member3"
prepare_backend_member_test "$MEMBER"
compile_backend_member_test "$MEMBER" "backend/test/Member3BackendTest.java"
run_backend_member_test "$MEMBER" "Member3BackendTest"
