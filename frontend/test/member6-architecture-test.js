#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");

const projectRoot = path.resolve(__dirname, "../..");
let passed = 0;

/*
 * member6 架构测试入口。
 *
 * member6 是项目 leader，测试重点不是重复测每个业务流程，
 * 而是检查项目是否仍符合最终约定的轻量架构：
 * - API 路径集中；
 * - 不恢复旧 Servlet/旧根路径；
 * - 公共门户壳层和技术文档存在；
 * - 后端 ApiRoutes 与前端 TARecruitment.routes 保持同步。
 */
function main() {
  testRequiredProjectFiles();
  testForbiddenArchitectureResidues();
  testApiRoutesAreSimpleAndShared();
  testPackageInfoAndDocs();

  console.log(`[member6] PASS total=${passed}`);
}

/*
 * 检查 leader 负责维护的关键文件是否还在。
 *
 * 这一步对应目录整理、门户壳层、公共样式、脚本入口和分工文档。
 * 如果这些文件缺失，说明项目结构已经偏离最终交付形态。
 */
function testRequiredProjectFiles() {
  [
    "backend/src/com/example/tarecruitment/package-info.java",
    "backend/src/com/example/tarecruitment/common/api/ApiRoutes.java",
    "frontend/webapp/WEB-INF/web.xml",
    "frontend/webapp/WEB-INF/jsp/fragments/portal-sidebar.jspf",
    "frontend/webapp/WEB-INF/jsp/fragments/portal-topbar.jspf",
    "frontend/webapp/css/common/components.css",
    "frontend/webapp/css/common/forms.css",
    "frontend/webapp/css/common/tokens.css",
    "frontend/webapp/css/portal/portal-shell.css",
    "frontend/webapp/js/common/i18n.js",
    "frontend/webapp/js/common/portal-i18n.js",
    "frontend/webapp/js/common/ta-recruitment.js",
    "scripts/dev.sh",
    "scripts/dev.bat",
    "scripts/config.example.sh",
    "scripts/config.example.bat",
    "docs/division-and-test/Overview.md"
  ].forEach((file) => assertExists(file));
  pass("Project leader files for architecture, shell, scripts, and docs exist");
}

/*
 * 检查旧架构残留。
 *
 * 这里扫描源码、前端、脚本、README 和分工文档，避免以下内容回流：
 * - 旧包名 authlogin；
 * - 已下线的大 Servlet 入口；
 * - 旧 AI 命名；
 * - 重复 JSON 响应工具；
 * - 带版本号的旧 API 前缀；
 * - 旧根路径接口字符串。
 */
function testForbiddenArchitectureResidues() {
  const targets = [
    "backend/src",
    "frontend/webapp",
    "scripts",
    "README.md",
    "docs/division-and-test"
  ];
  const forbiddenPatterns = [
    /com\.example\.authlogin|authlogin/,
    /ApplyServlet|ApplicantServlet|ApplicantAccessServlet/,
    /TongyiXiaomiAnalysisClient|TaJobMatchAiConfig|ta-job-match\.properties|ta\.job\.match\.ai|TA_JOB_MATCH_AI/,
    /DashScopeAnalysisClient|MatchAnalysisAiConfig|TaJobMatchAnalysisService|TaJobMatchAnalysisServlet|MoApplicationMatchAnalysisServlet/,
    /match-analysis\.properties|match\.analysis\.ai|DASHSCOPE_API_KEY|dashscope\.api\.key/,
    /\/api\/(?:ta\/job-match-analyses|mo\/application-match-analyses)|applicationMatchAnalyses|jobMatchAnalyses/,
    /JsonResponses/,
    /\/api\/v1/,
    /["']\/(?:jobs|apply|applicant|check-available|logout)["']/
  ];

  // 这些文件是已下线旧入口的代表文件，当前版本应当不存在。
  const obsoleteFiles = [
    "backend/src/com/example/tarecruitment/ai/service/SkillMatchService.java",
    "backend/src/com/example/tarecruitment/ai/web/SkillMatchServlet.java",
    "backend/src/com/example/tarecruitment/admin/dao/AdminInviteDao.java",
    "backend/src/com/example/tarecruitment/admin/model/AdminInvite.java",
    "backend/src/com/example/tarecruitment/admin/service/AdminInviteEmailService.java",
    "backend/src/com/example/tarecruitment/admin/web/AdminInviteServlet.java",
    "backend/src/com/example/tarecruitment/common/util/SecurityTokenUtil.java",
    "backend/src/com/example/tarecruitment/ai/client/DashScopeAnalysisClient.java",
    "backend/src/com/example/tarecruitment/ai/client/MatchAnalysisAiConfig.java",
    "backend/src/com/example/tarecruitment/ai/service/TaJobMatchAnalysisService.java",
    "backend/src/com/example/tarecruitment/ai/web/TaJobMatchAnalysisServlet.java",
    "backend/src/com/example/tarecruitment/ai/web/MoApplicationMatchAnalysisServlet.java",
    "frontend/webapp/WEB-INF/ai/match-analysis.properties.template",
    "frontend/webapp/jsp/mo/ai-skill-match.jsp",
    "frontend/webapp/jsp/mo/applicant-selection.jsp",
    "frontend/webapp/js/mo/mo-applicant-selection.js",
    "frontend/webapp/css/mo/mo-applicant-selection.css",
    "frontend/webapp/admin-register.jsp"
  ];
  obsoleteFiles.forEach((file) => {
    assert(!fs.existsSync(path.join(projectRoot, file)), `${file} should stay removed`);
  });

  scanTextFiles(targets).forEach((file) => {
    const source = stripComments(fs.readFileSync(file, "utf8"));
    forbiddenPatterns.forEach((pattern) => {
      assert(!pattern.test(source), `${relative(file)} contains forbidden architecture residue: ${pattern}`);
    });
  });
  pass("Old package names, old servlet entries, /api/v1, and removed AI/match-analysis residues are absent");
}

/*
 * 检查后端 API 常量和前端路由工具是否同步。
 *
 * 后端 ApiRoutes.java 是 API 路径事实来源；
 * 前端 ta-recruitment.js 必须包含对应路径，页面才能通过 TARecruitment.routes 调用。
 */
function testApiRoutesAreSimpleAndShared() {
  const apiRoutesFile = path.join(projectRoot, "backend/src/com/example/tarecruitment/common/api/ApiRoutes.java");
  const frontendRoutesFile = path.join(projectRoot, "frontend/webapp/js/common/ta-recruitment.js");
  const apiRoutesSource = fs.readFileSync(apiRoutesFile, "utf8");
  const frontendRoutesSource = fs.readFileSync(frontendRoutesFile, "utf8");

  const routeValues = [...apiRoutesSource.matchAll(/public static final String\s+\w+\s*=\s*"([^"]+)";/g)]
    .map((match) => match[1]);
  assert(routeValues.length >= 15, "ApiRoutes exposes expected API constants");

  routeValues.forEach((route) => {
    assert(route.startsWith("/api/"), `ApiRoutes value should start with /api/: ${route}`);
    assert(!route.startsWith("/api/v1/"), `ApiRoutes value should not use /api/v1: ${route}`);
    assert(frontendRoutesSource.includes(`"${route}"`), `frontend routes should include ${route}`);
  });
  pass("Backend ApiRoutes values are simple /api paths and mirrored by frontend route helper");
}

/*
 * 检查包级说明和分工总览。
 *
 * package-info.java 用来说明当前后端主包和轻量技术栈；
 * Overview.md 用来给答辩成员跳转到各自分工与测试说明。
 */
function testPackageInfoAndDocs() {
  const packageInfo = fs.readFileSync(
    path.join(projectRoot, "backend/src/com/example/tarecruitment/package-info.java"),
    "utf8"
  );
  assert(packageInfo.includes("TA Hiring System"), "package-info names TA Hiring System");
  assert(packageInfo.includes("Servlet") && packageInfo.includes("JSP") && packageInfo.includes("CSV"),
    "package-info documents lightweight stack");

  const overview = fs.readFileSync(path.join(projectRoot, "docs/division-and-test/Overview.md"), "utf8");
  ["member1", "member2", "member3", "member4", "member5", "member6"].forEach((member) => {
    assert(overview.includes(`[${member}.md](${member}.md)`), `Overview links ${member}`);
  });
  pass("Package documentation and division overview reflect the current project structure");
}

/*
 * 收集要扫描的文本文件。
 *
 * 只扫描源码、脚本、文档这类文本文件，不处理图片、构建产物或二进制文件。
 */
function scanTextFiles(targets) {
  const result = [];
  targets.forEach((target) => {
    const fullPath = path.join(projectRoot, target);
    if (!fs.existsSync(fullPath)) {
      return;
    }
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      walk(fullPath).forEach((file) => {
        if (isTextFile(file)) {
          result.push(file);
        }
      });
    } else if (isTextFile(fullPath)) {
      result.push(fullPath);
    }
  });
  return result;
}

// 递归遍历目录，供 scanTextFiles 使用。
function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(fullPath) : [fullPath];
  });
}

/*
 * 去掉注释后再做残留扫描。
 *
 * 这样文档或代码注释中解释“不要使用某旧路径”时，不会被误判为运行时残留。
 */
function stripComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/(^|[^:])\/\/.*$/gm, "$1");
}

// 判断一个文件是否属于本测试需要扫描的文本类型。
function isTextFile(file) {
  return /\.(java|jsp|jspf|js|css|md|sh|bat|xml|properties|template)$/.test(file);
}

// 以下是轻量测试辅助函数，失败时抛 Error，使成员脚本直接失败。
function assertExists(file) {
  assert(fs.existsSync(path.join(projectRoot, file)), `${file} should exist`);
}

function relative(file) {
  return path.relative(projectRoot, file);
}

function pass(message) {
  passed += 1;
  console.log(`[member6] PASS - ${message}`);
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

main();
