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
HTTP_PORT="${TOMCAT_HTTP_PORT:-8080}"
LOGIN_URL="http://localhost:${HTTP_PORT}/${APP_NAME}/login.jsp"
PORT_RELEASE_TIMEOUT_SECONDS="${PORT_RELEASE_TIMEOUT_SECONDS:-10}"
STARTUP_TIMEOUT_SECONDS="${STARTUP_TIMEOUT_SECONDS:-120}"

require_command() {
    local command_name="$1"
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "[ERROR] Missing required command: $command_name"
        exit 1
    fi
}

port_listener_pids() {
    lsof -tiTCP:"$HTTP_PORT" -sTCP:LISTEN 2>/dev/null | sort -u
}

tomcat_process_pids() {
    ps -axo pid=,command= | awk '/org\.apache\.catalina\.startup\.Bootstrap/ {print $1}'
}

is_port_listening() {
    [ -n "$(port_listener_pids)" ]
}

wait_for_port_release() {
    local deadline=$((SECONDS + PORT_RELEASE_TIMEOUT_SECONDS))
    local stable_free_count=0
    while [ "$SECONDS" -lt "$deadline" ]; do
        if ! is_port_listening; then
            stable_free_count=$((stable_free_count + 1))
            if [ "$stable_free_count" -ge 2 ]; then
                return 0
            fi
        else
            stable_free_count=0
        fi
        sleep 1
    done
    return 1
}

kill_tomcat_processes() {
    local pids
    pids="$(tomcat_process_pids)"
    if [ -z "$pids" ]; then
        return 0
    fi

    echo "  Killing old Tomcat process(es): $(echo "$pids" | tr '\n' ' ')"
    echo "$pids" | xargs kill >/dev/null 2>&1 || true
    sleep 2

    pids="$(tomcat_process_pids)"
    if [ -n "$pids" ]; then
        echo "  Force killing old Tomcat process(es): $(echo "$pids" | tr '\n' ' ')"
        echo "$pids" | xargs kill -9 >/dev/null 2>&1 || true
        sleep 1
    fi
}

kill_port_listeners() {
    local pids
    pids="$(port_listener_pids)"
    if [ -z "$pids" ]; then
        return 0
    fi

    echo "  Port $HTTP_PORT is still occupied. Killing listener process(es): $(echo "$pids" | tr '\n' ' ')"
    echo "$pids" | xargs kill >/dev/null 2>&1 || true
    sleep 2

    pids="$(port_listener_pids)"
    if [ -n "$pids" ]; then
        echo "  Port $HTTP_PORT is still occupied. Force killing listener process(es): $(echo "$pids" | tr '\n' ' ')"
        echo "$pids" | xargs kill -9 >/dev/null 2>&1 || true
        sleep 1
    fi
}

show_startup_diagnostics() {
    echo ""
    echo "[ERROR] Tomcat did not pass startup verification."
    echo "  Expected port: $HTTP_PORT"
    echo "  Expected URL: $LOGIN_URL"
    echo ""

    echo "Current port listener:"
    lsof -nP -iTCP:"$HTTP_PORT" -sTCP:LISTEN 2>/dev/null || echo "  No listener on port $HTTP_PORT"

    if [ -f "$CATALINA_HOME/logs/catalina.out" ]; then
        echo ""
        echo "Last Tomcat log lines:"
        tail -n 80 "$CATALINA_HOME/logs/catalina.out"
    fi
}

wait_for_startup_verification() {
    local deadline=$((SECONDS + STARTUP_TIMEOUT_SECONDS))
    local status

    while [ "$SECONDS" -lt "$deadline" ]; do
        if is_port_listening; then
            status="$(curl -sS -o /dev/null -w "%{http_code}" "$LOGIN_URL" 2>/dev/null || true)"
            if [ "$status" = "200" ]; then
                echo "  Port $HTTP_PORT is listening."
                echo "  $LOGIN_URL returned 200."
                return 0
            fi
            echo "  Waiting for $LOGIN_URL to return 200 (current: ${status:-no response})..."
        else
            echo "  Waiting for port $HTTP_PORT to start listening..."
        fi
        sleep 2
    done

    show_startup_diagnostics
    return 1
}

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

require_command lsof
require_command curl

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

kill_tomcat_processes

if ! wait_for_port_release; then
    kill_port_listeners
fi

if is_port_listening; then
    echo "[ERROR] Port $HTTP_PORT is still occupied after kill attempts."
    lsof -nP -iTCP:"$HTTP_PORT" -sTCP:LISTEN 2>/dev/null || true
    exit 1
fi

echo "  Port $HTTP_PORT is free."

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

if ! "$CATALINA_HOME/bin/startup.sh"; then
    echo "[ERROR] Failed to execute Tomcat startup.sh"
    exit 1
fi

if ! wait_for_startup_verification; then
    exit 1
fi

echo ""
echo "========================================"
echo "  All Done!"
echo "========================================"
echo ""
echo "Access URLs:"
echo "  - Home: http://localhost:$HTTP_PORT/$APP_NAME/"
echo "  - Login: $LOGIN_URL"
echo ""
echo "Tomcat Manager: http://localhost:$HTTP_PORT/manager/html"
echo ""

if [ -t 0 ]; then
    read -p "Press Enter to exit..."
fi
