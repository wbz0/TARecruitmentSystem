#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");

const projectRoot = path.resolve(__dirname, "../..");
const jsRoot = path.join(projectRoot, "frontend/webapp/js");

let passed = 0;

/*
 * Sheng Yuhan frontend test entry point.
 *
 * Sheng Yuhan is mainly responsible for pages, interactions, styles and frontend API call methods.
 * This script does not launch a browser, but performs static checks easy to demonstrate during defense:
 * 1. All page JS passes Node syntax check at least;
 * 2. Page scripts cannot bypass TARecruitment.routes to hardcode API addresses;
 * 3. Key JSP/JS assets exist;
 * 4. Removed old MO pages and old admin registration explanation page cannot reappear.
 */
function main() {
  const jsFiles = walk(jsRoot).filter((file) => file.endsWith(".js"));
  assert(jsFiles.length > 0, "frontend JS files exist");

  testJavaScriptSyntax(jsFiles);
  testPageScriptsUseSharedRoutes(jsFiles);
  testRolePageAssetsExist();
  testRemovedLegacyPagesStayRemoved();

  console.log(`[Sheng Yuhan] PASS total=${passed}`);
}

/*
 * Use node --check for syntax validation.
 *
 * This step does not execute browser APIs, only checks if JS files have syntax errors.
 * For defense, it quickly proves page scripts are not "broken JS that cannot open".
 */
function testJavaScriptSyntax(jsFiles) {
  jsFiles.forEach((file) => {
    const result = spawnSync(process.execPath, ["--check", file], { encoding: "utf8" });
    if (result.status !== 0) {
      throw new Error(`node --check failed for ${relative(file)}\n${result.stderr || result.stdout}`);
    }
  });
  pass("All frontend JavaScript files pass node --check");
}

/*
 * Check if page scripts generate API URLs through the shared route utility.
 *
 * When the project is deployed under a context path like /groupproject, hardcoded /api/... easily breaks.
 * So ordinary page scripts cannot directly write "/api/..." but must call TARecruitment.routes
 * from common/ta-recruitment.js.
 *
 * common/ta-recruitment.js itself is the central place for API path definitions, so it is excluded from page script checks.
 */
function testPageScriptsUseSharedRoutes(jsFiles) {
  const sharedRouteFile = path.join(jsRoot, "common/ta-recruitment.js");
  const pageScripts = jsFiles.filter((file) => file !== sharedRouteFile);
  const directApiLiteral = /(["'`])\/api\/[^"'`]*\1/;
  const legacyRootLiteral = /(["'`])\/(?:jobs|apply|applicant|check-available|logout)\1/;

  pageScripts.forEach((file) => {
    const source = stripComments(fs.readFileSync(file, "utf8"));
    assert(!directApiLiteral.test(source), `${relative(file)} should not hard-code /api/... strings`);
    assert(!legacyRootLiteral.test(source), `${relative(file)} should not use old root API strings`);
  });

  const sharedSource = fs.readFileSync(sharedRouteFile, "utf8");
  [
    "auth",
    "jobs",
    "applications",
    "me",
    "admin",
    "mo",
    "ta",
    "notifications"
  ].forEach((routeGroup) => {
    assert(sharedSource.includes(`${routeGroup}:`) || sharedSource.includes(`${routeGroup}: function`),
      `shared routes expose ${routeGroup}`);
  });
  pass("Page scripts use TARecruitment.routes instead of hard-coded API paths");
}

/*
 * Check if key page assets maintained by Sheng Yuhan exist.
 *
 * This does not check what pages look like, only confirms main pages for login/register, TA, MO, Admin
 * and shared route file were not lost during directory organization.
 */
function testRolePageAssetsExist() {
  [
    "frontend/webapp/login.jsp",
    "frontend/webapp/register.jsp",
    "frontend/webapp/jsp/ta/job-list.jsp",
    "frontend/webapp/jsp/ta/job-detail.jsp",
    "frontend/webapp/jsp/ta/application-status.jsp",
    "frontend/webapp/jsp/mo/dashboard.jsp",
    "frontend/webapp/jsp/admin/dashboard.jsp",
    "frontend/webapp/jsp/admin/invite.jsp",
    "frontend/webapp/js/common/ta-recruitment.js"
  ].forEach((file) => assertExists(file));
  pass("Role pages and shared frontend route helper exist");
}

/*
 * Check if old pages remain offline.
 *
 * applicant-selection has been merged into MO dashboard sub-view, old skill match page is also
 * replaced by subsequent AI recommendation search flow. admin-register is an old explanation page;
 * currently only /admin-invite.jsp short invite code registration entry is kept.
 */
function testRemovedLegacyPagesStayRemoved() {
  [
    "frontend/webapp/jsp/mo/ai-skill-match.jsp",
    "frontend/webapp/js/mo/mo-ai-skill-match.js",
    "frontend/webapp/css/mo/mo-ai-skill-match.css",
    "frontend/webapp/jsp/mo/applicant-selection.jsp",
    "frontend/webapp/js/mo/mo-applicant-selection.js",
    "frontend/webapp/css/mo/mo-applicant-selection.css",
    "frontend/webapp/admin-register.jsp"
  ].forEach((file) => {
    assert(!fs.existsSync(path.join(projectRoot, file)), `${file} should stay removed`);
  });
  pass("Removed legacy frontend page assets are not present");
}

/*
 * Recursively collect files in a directory.
 * Node standard library does not directly provide walk; a small local implementation is kept here.
 */
function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(fullPath) : [fullPath];
  });
}

/*
 * Remove comments before searching for path literals.
 *
 * This way, when API paths are mentioned in code comments, they will not be reported as false positives;
 * what is really checked is the runtime string.
 */
function stripComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/(^|[^:])\/\/.*$/gm, "$1");
}

// The following are lightweight test helper functions; on failure they throw Error so shell script returns non-0.
function assertExists(file) {
  assert(fs.existsSync(path.join(projectRoot, file)), `${file} should exist`);
}

function relative(file) {
  return path.relative(projectRoot, file);
}

function pass(message) {
  passed += 1;
  console.log(`[Sheng Yuhan] PASS - ${message}`);
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

main();