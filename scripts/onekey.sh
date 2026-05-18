#!/usr/bin/env bash
set -euo pipefail

# ========================================
# One-key build + deploy + startup (macOS/Linux)
# ========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

if [[ -f "${SCRIPT_DIR}/config.sh" ]]; then
  # shellcheck disable=SC1091
  source "${SCRIPT_DIR}/config.sh"
else
  echo "[ERROR] Missing scripts/config.sh"
  echo "Please run:"
  echo "  cd scripts && cp config.example.sh config.sh"
  exit 1
fi

: "${TOMCAT_HOME:=${CATALINA_HOME:-}}"
: "${CATALINA_HOME:=${TOMCAT_HOME:-}}"
: "${APP_NAME:=groupproject}"

SRC_DIR="${PROJECT_ROOT}/backend/src"
WEBAPP_DIR="${PROJECT_ROOT}/frontend/webapp"
BUILD_DIR="${PROJECT_ROOT}/build"
CLASSES_DIR="${BUILD_DIR}/WEB-INF/classes"
SERVLET_API_JAR="${TOMCAT_HOME}/lib/servlet-api.jar"
TARGET_DIR="${CATALINA_HOME}/webapps/${APP_NAME}"
FRONTEND_DIR="${PROJECT_ROOT}/frontend/webapp"

# ========================================
# STEP 1: BUILD
# ========================================
echo ""
echo "========================================"
echo "  [STEP 1/3] Building..."
echo "========================================"
echo ""

if [[ -z "${TOMCAT_HOME}" || ! -d "${TOMCAT_HOME}" ]]; then
  echo "[ERROR] Tomcat not found: ${TOMCAT_HOME:-<empty>}"
  echo "Please check scripts/config.sh"
  exit 1
fi

if [[ ! -f "${SERVLET_API_JAR}" ]]; then
  echo "[ERROR] servlet-api.jar not found: ${SERVLET_API_JAR}"
  echo "Please verify TOMCAT_HOME in scripts/config.sh"
  exit 1
fi

echo "[1/3] Cleaning old build files..."
rm -rf "${BUILD_DIR}"
mkdir -p "${CLASSES_DIR}"

echo "[2/3] Compiling Java source files..."
cp_with_classes="${SERVLET_API_JAR}:${CLASSES_DIR}"

# First pass: StoragePaths (no dependencies) and all models
echo "First pass: compiling StoragePaths and models..."
javac -encoding UTF-8 -d "${CLASSES_DIR}" "${SRC_DIR}/com/example/authlogin/util/StoragePaths.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/User.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Applicant.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Job.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Application.java"

# Second pass: DAO classes
echo "Second pass: compiling DAO classes..."
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/UserDao.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/ApplicantDao.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/JobDao.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/ApplicationDao.java"

# Third pass: utility classes (SessionUtil before PermissionUtil)
echo "Third pass: compiling utility classes..."
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/util/SessionUtil.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/util/PermissionUtil.java"

# Fourth pass: filter
echo "Fourth pass: compiling filter..."
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/filter/AuthFilter.java"

# Fifth pass: servlets
echo "Fifth pass: compiling servlets..."
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/LoginServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/RegisterServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/LogoutServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/HelloServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/ApplicantServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/JobServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/ApplyServlet.java"

echo "[3/3] Copying resource files..."
cp -R "${WEBAPP_DIR}/." "${BUILD_DIR}/"

echo ""
echo "[BUILD] Complete!"
echo ""

# ========================================
# STEP 2: DEPLOY
# ========================================
echo ""
echo "========================================"
echo "  [STEP 2/3] Deploying..."
echo "========================================"
echo ""

if [[ ! -d "${BUILD_DIR}" ]]; then
  echo "[ERROR] Build directory not found. Run build.sh first."
  exit 1
fi

if [[ -z "${CATALINA_HOME}" || ! -d "${CATALINA_HOME}" ]]; then
  echo "[ERROR] Tomcat not found: ${CATALINA_HOME:-<empty>}"
  echo "Please check scripts/config.sh"
  exit 1
fi

echo "Stopping Tomcat (if running)..."
"${CATALINA_HOME}/bin/shutdown.sh" >/dev/null 2>&1 || true
sleep 2

echo "Deploying to Tomcat..."
rm -rf "${TARGET_DIR}"
mkdir -p "${TARGET_DIR}"
cp -R "${BUILD_DIR}/." "${TARGET_DIR}/"

# Safety sync for frontend static assets.
if [[ -d "${FRONTEND_DIR}/css" ]]; then
  mkdir -p "${TARGET_DIR}/css"
  cp -R "${FRONTEND_DIR}/css/." "${TARGET_DIR}/css/"
fi

if [[ -d "${FRONTEND_DIR}/js" ]]; then
  mkdir -p "${TARGET_DIR}/js"
  cp -R "${FRONTEND_DIR}/js/." "${TARGET_DIR}/js/"
fi

# Trigger context reload if server is still running.
if [[ -f "${TARGET_DIR}/WEB-INF/web.xml" ]]; then
  touch "${TARGET_DIR}/WEB-INF/web.xml"
fi

echo ""
echo "[DEPLOY] Complete!"
echo "  App path: ${TARGET_DIR}"
echo ""

# ========================================
# STEP 3: STARTUP
# ========================================
echo ""
echo "========================================"
echo "  [STEP 3/3] Starting Tomcat..."
echo "========================================"
echo ""

if [[ -z "${CATALINA_HOME}" || ! -d "${CATALINA_HOME}" ]]; then
  echo "[ERROR] Tomcat not found: ${CATALINA_HOME:-<empty>}"
  echo "Please check scripts/config.sh"
  exit 1
fi

"${CATALINA_HOME}/bin/startup.sh"

echo ""
echo "========================================"
echo "  All Done! Tomcat is starting..."
echo "========================================"
echo ""
echo "Access URLs:"
echo "  - Home: http://localhost:8080/${APP_NAME}/"
echo "  - JSP: http://localhost:8080/${APP_NAME}/jsp/welcome.jsp"
echo "  - Servlet: http://localhost:8080/${APP_NAME}/hello"
echo ""
