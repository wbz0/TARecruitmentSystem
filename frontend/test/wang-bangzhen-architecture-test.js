#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");

const projectRoot = path.resolve(__dirname, "../..");
let passed = 0;

/*
 * Wang Bangzhen architecture test entry point.
 *
 * Wang Bangzhen is the project leader; the test focus is not to retest every business process,
 * but to check if the project still conforms to the final agreed lightweight architecture:
 * - API paths are centralized;
 * - No restoration of old Servlets/old root paths;
 * - Common portal shell and technical documentation exist;
 * - Backend ApiRoutes stays in sync with frontend TARecruitment.routes.
 */
function main() {
  testRequiredProjectFiles();
  testForbiddenArchitectureResidues();
  testApiRoutesAreSimpleAndShared();
  testPackageInfoAndDocs();

  console.log(`[Wang Bangzhen] PASS total=${passed}`);
}

/*
 * Check if key files maintained by the leader still exist.
 *
 * This step corresponds to directory organization, portal shell, common styles, script entry points and division documentation.
 * If these files are missing, the project structure has deviated from the final delivery state.
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
  pass("Architecture owner files for architecture, shell, scripts, and docs exist");
}

/*
 * Check for old architecture residues.
 *
 * Scans source code, frontend, scripts, README and division documentation to prevent:
 * - Old package name authlogin;
 * - Decommissioned large Servlet entries;
 * - Old AI naming;
 * - Duplicate JSON response utilities;
 * - Old API prefixes with version numbers;
 * - Old root path endpoint strings.
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

  // These files are representative of decommissioned old entries; current version should not exist.
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
 * Check if backend API constants and frontend route utility stay in sync.
 *
 * Backend ApiRoutes.java is the source of truth for API paths;
 * frontend ta-recruitment.js must contain corresponding paths so pages can call via TARecruitment.routes.
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
 * Check package-level documentation and division overview.
 *
 * package-info.java explains the current backend main package and lightweight tech stack;
 * Overview.md is used for contributors to jump to their division and test descriptions.
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
  [
    "ouyang-xiaojun",
    "zhou-bohan",
    "liu-tengyi",
    "sun-jialu",
    "sheng-yuhan",
    "wang-bangzhen"
  ].forEach((fileStem) => {
    assert(overview.includes(`[${fileStem}.md](${fileStem}.md)`), `Overview links ${fileStem}`);
  });
  pass("Package documentation and division overview reflect the current project structure");
}

/*
 * Collect text files to scan.
 *
 * Only scans source code, scripts, documentation text files; does not process images, build artifacts or binary files.
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

// Recursively walk directory for use by scanTextFiles.
function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(fullPath) : [fullPath];
  });
}

/*
 * Strip comments before residue scanning.
 *
 * This way, when documentation or code comments explain "do not use some old path", it will not be misidentified as runtime residue.
 */
function stripComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/(^|[^:])\/\/.*$/gm, "$1");
}

// Determine if a file is a text type that this test needs to scan.
function isTextFile(file) {
  return /\.(java|jsp|jspf|js|css|md|sh|bat|xml|properties|template)$/.test(file);
}

// The following are lightweight test helper functions; on failure they throw Error so contributor scripts fail directly.
function assertExists(file) {
  assert(fs.existsSync(path.join(projectRoot, file)), `${file} should exist`);
}

function relative(file) {
  return path.relative(projectRoot, file);
}

function pass(message) {
  passed += 1;
  console.log(`[Wang Bangzhen] PASS - ${message}`);
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

main();
