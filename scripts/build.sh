#!/usr/bin/env bash
set -euo pipefail

# ========================================
# Build Script - Compile Java Servlet (macOS/Linux)
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
: "${APP_NAME:=groupproject}"

SRC_DIR="${PROJECT_ROOT}/backend/src"
WEBAPP_DIR="${PROJECT_ROOT}/frontend/webapp"
BUILD_DIR="${PROJECT_ROOT}/build"
CLASSES_DIR="${BUILD_DIR}/WEB-INF/classes"
SERVLET_API_JAR="${TOMCAT_HOME}/lib/servlet-api.jar"

echo "========================================"
echo "  Servlet/JSP Build Script (macOS/Linux)"
echo "========================================"
echo

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

# First pass: model and dao
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/User.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Applicant.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}:${CLASSES_DIR}" "${SRC_DIR}/com/example/authlogin/util/StoragePaths.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/UserDao.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/ApplicantDao.java"

# util, model/dao
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/util/SessionUtil.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Job.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/JobDao.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${SERVLET_API_JAR}" "${SRC_DIR}/com/example/authlogin/model/Application.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/dao/ApplicationDao.java"

# filter
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/filter/AuthFilter.java"

# servlets
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/LoginServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/RegisterServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/LogoutServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/HelloServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/ApplicantServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/JobServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/ApplyServlet.java"
javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${cp_with_classes}" "${SRC_DIR}/com/example/authlogin/util/PermissionUtil.java"

echo "[3/3] Copying resource files..."
cp -R "${WEBAPP_DIR}/." "${BUILD_DIR}/"

echo
echo "========================================"
echo "  Build Complete!"
echo "  Output: ${BUILD_DIR}"
echo "========================================"
echo
