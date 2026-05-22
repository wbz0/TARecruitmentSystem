#!/usr/bin/env bash
set -euo pipefail

# Shared helper for all contributor test scripts.
#
# Design goals:
# - each contributor runs only their own named test script;
# - backend tests compile and run with javac/java directly, without JUnit or Maven;
# - each test uses build/contributor-tests/<contributor>/data as a temporary TA_HIRING_DATA_DIR;
# - test output consistently uses a contributor name prefix for defense demos and screenshots.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TEST_BUILD_ROOT="$PROJECT_ROOT/build/contributor-tests"

# Load local Tomcat config, mainly to locate servlet-api.jar.
# If config.sh is absent, servlet_api_jar() still tries common Homebrew paths below.
if [ -f "$PROJECT_ROOT/scripts/config.sh" ]; then
    # shellcheck disable=SC1091
    source "$PROJECT_ROOT/scripts/config.sh"
fi

# Print a section title, such as "Compile backend sources" or "Run contributor backend test".
print_section() {
    local label="$1"
    local message="$2"
    printf '\n[%s] %s\n' "$label" "$message"
}

# Print a single step success message.
pass_step() {
    local label="$1"
    local message="$2"
    printf '[%s] PASS - %s\n' "$label" "$message"
}

# Central failure exit: print an error and exit 1 so demo scripts fail clearly.
fail_step() {
    local label="$1"
    local message="$2"
    printf '[%s] FAIL - %s\n' "$label" "$message" >&2
    exit 1
}

# Check that a command required by the test exists, such as javac, java, or node.
require_command() {
    local label="$1"
    local command_name="$2"
    if ! command -v "$command_name" >/dev/null 2>&1; then
        fail_step "$label" "Missing command: $command_name"
    fi
}

# Find the Servlet API dependency.
#
# Backend sources import jakarta.servlet.*. Direct javac compilation must include
# Tomcat's servlet-api.jar on the classpath. This uses config.sh first, then tries
# common macOS/Homebrew install paths.
servlet_api_jar() {
    local candidates=()
    if [ "${TOMCAT_HOME:-}" != "" ]; then
        candidates+=("$TOMCAT_HOME/lib/servlet-api.jar")
    fi
    if [ "${CATALINA_HOME:-}" != "" ]; then
        candidates+=("$CATALINA_HOME/lib/servlet-api.jar")
    fi
    candidates+=(
        "/opt/homebrew/opt/tomcat@10/libexec/lib/servlet-api.jar"
        "/opt/homebrew/opt/tomcat/libexec/lib/servlet-api.jar"
        "/usr/local/opt/tomcat@10/libexec/lib/servlet-api.jar"
        "/usr/local/opt/tomcat/libexec/lib/servlet-api.jar"
    )

    local candidate
    for candidate in "${candidates[@]}"; do
        if [ -f "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    return 1
}

# Compile backend project sources.
#
# Each backend contributor test compiles all of backend/src first. This proves that:
# - the current source tree compiles with javac;
# - the contributor test is not compiling only its own class;
# - Servlet-related classes compile when servlet-api.jar is present.
prepare_backend_contributor_test() {
    local contributor_id="$1"
    local label="$2"
    local build_dir="$TEST_BUILD_ROOT/$contributor_id"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local source_list="$build_dir/java-sources.txt"

    # Backend tests need at least the JDK javac/java tools.
    require_command "$label" javac
    require_command "$label" java

    local servlet_jar
    if ! servlet_jar="$(servlet_api_jar)"; then
        fail_step "$label" "servlet-api.jar not found. Check TOMCAT_HOME/CATALINA_HOME in scripts/config.sh"
    fi

    # Clean the contributor build directory on each run so stale class files cannot affect results.
    rm -rf "$build_dir"
    mkdir -p "$classes_dir" "$test_classes_dir"
    find "$PROJECT_ROOT/backend/src" -name "*.java" | sort > "$source_list"

    if [ ! -s "$source_list" ]; then
        fail_step "$label" "No Java sources found under backend/src"
    fi

    print_section "$label" "Compile backend sources"
    javac -encoding UTF-8 -d "$classes_dir" -cp "$servlet_jar:$classes_dir" @"$source_list"
    pass_step "$label" "Backend sources compiled"
}

# Compile one contributor's Java test class.
#
# Test classes live under backend/test without a package and compile directly into test-classes.
compile_backend_contributor_test() {
    local contributor_id="$1"
    local label="$2"
    local test_source="$3"
    local build_dir="$TEST_BUILD_ROOT/$contributor_id"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local servlet_jar
    servlet_jar="$(servlet_api_jar)"

    print_section "$label" "Compile contributor test code"
    javac -encoding UTF-8 -d "$test_classes_dir" -cp "$servlet_jar:$classes_dir" "$PROJECT_ROOT/$test_source"
    pass_step "$label" "Contributor test code compiled"
}

# Run one backend contributor test.
#
# The key point is temporarily setting TA_HIRING_DATA_DIR:
# DAOs write CSV files into build/contributor-tests/<contributor>/data instead of touching
# the real Tomcat data directory or polluting demo accounts.
run_backend_contributor_test() {
    local contributor_id="$1"
    local label="$2"
    local main_class="$3"
    local build_dir="$TEST_BUILD_ROOT/$contributor_id"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local data_dir="$build_dir/data"
    local servlet_jar
    servlet_jar="$(servlet_api_jar)"

    rm -rf "$data_dir"
    mkdir -p "$data_dir"

    print_section "$label" "Run contributor backend test"
    TA_HIRING_DATA_DIR="$data_dir" java -cp "$servlet_jar:$classes_dir:$test_classes_dir" "$main_class"
    pass_step "$label" "Contributor backend test passed"
}

# Run frontend/architecture Node tests.
#
# Sheng Yuhan and Wang Bangzhen tests are not browser E2E tests. They are static checks for:
# - JS syntax;
# - frontend route usage rules;
# - architecture remnants and documentation paths.
run_node_contributor_test() {
    local contributor_id="$1"
    local label="$2"
    local test_source="$3"

    require_command "$label" node
    print_section "$label" "Run contributor frontend/architecture test"
    node "$PROJECT_ROOT/$test_source"
    pass_step "$label" "Contributor test passed"
}
