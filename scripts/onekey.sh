#!/usr/bin/env bash
set -euo pipefail

# ========================================
# One-key build + deploy + startup (macOS/Linux)
# ========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/build.sh"
"${SCRIPT_DIR}/deploy.sh"
"${SCRIPT_DIR}/startup.sh"
