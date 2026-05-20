#!/usr/bin/env bash
# ========================================
# Dev Script - Build + Deploy + Start in one
# ========================================

# Auto-add execute permission if needed
if [ ! -x "$0" ]; then
    chmod +x "$0"
    exec "$0" "$@"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# ==== Load config ====
source "$SCRIPT_DIR/config.sh"

# ==== CONFIG ====
SRC_DIR="$PROJECT_ROOT/backend/src"
WEBAPP_DIR="$PROJECT_ROOT/frontend/webapp"
BUILD_DIR="$PROJECT_ROOT/build"
TARGET_DIR="$CATALINA_HOME/webapps/$APP_NAME"
FRONTEND_DIR="$PROJECT_ROOT/frontend/webapp"

echo "========================================"
echo "  Dev Script - All in One"
echo "========================================"
echo ""

# ========================================
# STEP 1: BUILD
# ========================================

echo "[1/3] Building..."
echo ""

# Clean old build directory
if [ -d "$BUILD_DIR" ]; then
    echo "  Cleaning old build files..."
    rm -rf "$BUILD_DIR"
fi

# Create output directory
mkdir -p "$BUILD_DIR/WEB-INF/classes"

# Check Tomcat path
if [ ! -d "$TOMCAT_HOME" ]; then
    echo "[ERROR] Tomcat not found: $TOMCAT_HOME"
    echo "Please check config.sh"
    exit 1
fi

CLASSPATH="$TOMCAT_HOME/lib/servlet-api.jar:$BUILD_DIR/WEB-INF/classes"

SOURCE_LIST="$BUILD_DIR/java-sources.txt"
find "$SRC_DIR" -name "*.java" | sort > "$SOURCE_LIST"

if [ ! -s "$SOURCE_LIST" ]; then
    echo "[ERROR] No Java source files found under $SRC_DIR"
    exit 1
fi

SOURCE_COUNT="$(wc -l < "$SOURCE_LIST" | tr -d ' ')"
echo "  Compiling $SOURCE_COUNT Java source files..."
javac -encoding UTF-8 -d "$BUILD_DIR/WEB-INF/classes" -cp "$CLASSPATH" @"$SOURCE_LIST"

echo "  Copying resource files..."
if [ -d "$WEBAPP_DIR" ]; then
    cp -r "$WEBAPP_DIR/"* "$BUILD_DIR/"
fi

echo "  Build Complete!"
echo ""

# ========================================
# STEP 2: DEPLOY
# ========================================

echo "[2/3] Deploying..."
echo ""

# Check build directory
if [ ! -d "$BUILD_DIR" ]; then
    echo "[ERROR] Build directory not found after build step."
    exit 1
fi

# Check Tomcat directory
if [ ! -d "$CATALINA_HOME" ]; then
    echo "[ERROR] Tomcat not found: $CATALINA_HOME"
    echo "Please check config.sh"
    exit 1
fi

echo "  Stopping Tomcat (if running)..."
if ! "$CATALINA_HOME/bin/shutdown.sh" >/dev/null 2>&1; then
    echo "  Tomcat was not running."
fi

sleep 2

echo "  Deploying to Tomcat..."

# Delete old version
if [ -d "$TARGET_DIR" ]; then
    echo "  Removing old version..."
    rm -rf "$TARGET_DIR"
fi

mkdir -p "$TARGET_DIR"

# Copy build to Tomcat
cp -r "$BUILD_DIR/"* "$TARGET_DIR/"

if [ $? -ne 0 ]; then
    echo "[ERROR] Failed to copy build artifacts to Tomcat webapps."
    exit 1
fi

# Safety sync for frontend static assets
if [ -d "$FRONTEND_DIR/css" ]; then
    cp -r "$FRONTEND_DIR/css/"* "$TARGET_DIR/css/"
fi

if [ -d "$FRONTEND_DIR/js" ]; then
    cp -r "$FRONTEND_DIR/js/"* "$TARGET_DIR/js/"
fi

# Touch web.xml to trigger reload
if [ -f "$TARGET_DIR/WEB-INF/web.xml" ]; then
    touch "$TARGET_DIR/WEB-INF/web.xml"
fi

echo "  Deploy Complete!"
echo ""

# ========================================
# STEP 3: START
# ========================================

echo "[3/3] Starting Tomcat..."
echo ""

if [ ! -d "$CATALINA_HOME" ]; then
    echo "[ERROR] Tomcat not found: $CATALINA_HOME"
    echo "Please check config.sh"
    exit 1
fi

"$CATALINA_HOME/bin/startup.sh"

echo ""
echo "========================================"
echo "  All Done!"
echo "========================================"
echo ""
echo "Access URLs:"
echo "  - Home: http://localhost:8080/$APP_NAME/"
echo "  - Login: http://localhost:8080/$APP_NAME/login.jsp"
echo ""
echo "Tomcat Manager: http://localhost:8080/manager/html"
echo ""

read -p "Press Enter to exit..."
