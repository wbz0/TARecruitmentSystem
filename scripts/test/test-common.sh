#!/usr/bin/env bash
set -euo pipefail

# 所有成员测试脚本共享这一个公共文件。
#
# 设计目标：
# - 每个成员只运行自己的 test-memberX.sh；
# - 后端测试统一用 javac/java 编译运行，不引入 JUnit/Maven；
# - 每次测试都使用 build/member-tests/<member>/data 作为临时 TA_HIRING_DATA_DIR；
# - 测试输出统一带 [memberX] 前缀，方便答辩展示和截图。
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TEST_BUILD_ROOT="$PROJECT_ROOT/build/member-tests"

# 读取本机 Tomcat 配置，主要是为了找到 servlet-api.jar。
# 如果 config.sh 不存在，下面的 servlet_api_jar() 仍会尝试 Homebrew 常见路径。
if [ -f "$PROJECT_ROOT/scripts/config.sh" ]; then
    # shellcheck disable=SC1091
    source "$PROJECT_ROOT/scripts/config.sh"
fi

# 打印阶段标题，例如“编译后端源码”“运行成员后端测试”。
print_section() {
    local member="$1"
    local message="$2"
    printf '\n[%s] %s\n' "$member" "$message"
}

# 打印单个步骤通过信息。
pass_step() {
    local member="$1"
    local message="$2"
    printf '[%s] PASS - %s\n' "$member" "$message"
}

# 统一失败出口：打印错误并 exit 1，让答辩脚本明确显示失败。
fail_step() {
    local member="$1"
    local message="$2"
    printf '[%s] FAIL - %s\n' "$member" "$message" >&2
    exit 1
}

# 检查运行测试需要的命令是否存在，例如 javac、java、node。
require_command() {
    local member="$1"
    local command_name="$2"
    if ! command -v "$command_name" >/dev/null 2>&1; then
        fail_step "$member" "缺少命令：$command_name"
    fi
}

# 查找 Servlet API 依赖。
#
# 后端源码引用 jakarta.servlet.*，直接 javac 编译时必须把 Tomcat 的
# servlet-api.jar 放进 classpath。这里先用 config.sh，再尝试 macOS/Homebrew
# 常见安装路径。
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

# 编译后端项目源码。
#
# 每个后端成员测试都先完整编译 backend/src，这样可以证明：
# - 当前源码整体能通过 javac；
# - 成员测试不是只编译自己那一个类；
# - Servlet 相关类也能在 servlet-api.jar 帮助下通过编译。
prepare_backend_member_test() {
    local member="$1"
    local build_dir="$TEST_BUILD_ROOT/$member"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local source_list="$build_dir/java-sources.txt"

    # 后端测试至少需要 JDK 的 javac/java。
    require_command "$member" javac
    require_command "$member" java

    local servlet_jar
    if ! servlet_jar="$(servlet_api_jar)"; then
        fail_step "$member" "找不到 servlet-api.jar，请检查 scripts/config.sh 里的 TOMCAT_HOME/CATALINA_HOME"
    fi

    # 每次运行都清理该成员自己的构建目录，避免旧 class 文件影响结果。
    rm -rf "$build_dir"
    mkdir -p "$classes_dir" "$test_classes_dir"
    find "$PROJECT_ROOT/backend/src" -name "*.java" | sort > "$source_list"

    if [ ! -s "$source_list" ]; then
        fail_step "$member" "backend/src 下没有 Java 源码"
    fi

    print_section "$member" "编译后端源码"
    javac -encoding UTF-8 -d "$classes_dir" -cp "$servlet_jar:$classes_dir" @"$source_list"
    pass_step "$member" "后端源码编译通过"
}

# 编译某个成员自己的 Java 测试类。
#
# 测试类放在 backend/test 下，没有 package，编译后直接进入 test-classes。
compile_backend_member_test() {
    local member="$1"
    local test_source="$2"
    local build_dir="$TEST_BUILD_ROOT/$member"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local servlet_jar
    servlet_jar="$(servlet_api_jar)"

    print_section "$member" "编译成员测试代码"
    javac -encoding UTF-8 -d "$test_classes_dir" -cp "$servlet_jar:$classes_dir" "$PROJECT_ROOT/$test_source"
    pass_step "$member" "成员测试代码编译通过"
}

# 运行某个后端成员测试。
#
# 关键点是临时设置 TA_HIRING_DATA_DIR：
# DAO 会把 CSV 写入 build/member-tests/<member>/data，
# 不会碰真实 Tomcat 数据目录，也不会污染演示账号。
run_backend_member_test() {
    local member="$1"
    local main_class="$2"
    local build_dir="$TEST_BUILD_ROOT/$member"
    local classes_dir="$build_dir/classes"
    local test_classes_dir="$build_dir/test-classes"
    local data_dir="$build_dir/data"
    local servlet_jar
    servlet_jar="$(servlet_api_jar)"

    rm -rf "$data_dir"
    mkdir -p "$data_dir"

    print_section "$member" "运行成员后端测试"
    TA_HIRING_DATA_DIR="$data_dir" java -cp "$servlet_jar:$classes_dir:$test_classes_dir" "$main_class"
    pass_step "$member" "成员后端测试通过"
}

# 运行前端/架构 Node 测试。
#
# member5/member6 的测试不是浏览器端 E2E，而是静态检查：
# - JS 语法；
# - 前端路由调用规范；
# - 架构残留和文档路径。
run_node_member_test() {
    local member="$1"
    local test_source="$2"

    require_command "$member" node
    print_section "$member" "运行成员前端/架构测试"
    node "$PROJECT_ROOT/$test_source"
    pass_step "$member" "成员测试通过"
}
