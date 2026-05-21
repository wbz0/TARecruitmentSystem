#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");
const { spawnSync } = require("child_process");

const projectRoot = path.resolve(__dirname, "../..");
const jsRoot = path.join(projectRoot, "frontend/webapp/js");

let passed = 0;

/*
 * member5 前端测试入口。
 *
 * member5 主要负责页面、交互、样式和前端 API 调用方式。
 * 这个脚本不启动浏览器，而是做答辩时容易展示的静态检查：
 * 1. 所有页面 JS 至少能通过 Node 的语法检查；
 * 2. 页面脚本不能绕过 TARecruitment.routes 直接手写 API 地址；
 * 3. 关键 JSP/JS 资产存在；
 * 4. 已下线的旧 MO 页面和旧 admin 注册说明页不能重新出现。
 */
function main() {
  const jsFiles = walk(jsRoot).filter((file) => file.endsWith(".js"));
  assert(jsFiles.length > 0, "frontend JS files exist");

  testJavaScriptSyntax(jsFiles);
  testPageScriptsUseSharedRoutes(jsFiles);
  testRolePageAssetsExist();
  testRemovedLegacyPagesStayRemoved();

  console.log(`[member5] PASS total=${passed}`);
}

/*
 * 使用 node --check 做语法检查。
 *
 * 这一步不会执行浏览器 API，只检查 JS 文件是否存在语法错误。
 * 对答辩来说，它能快速证明页面脚本不是“打不开的坏 JS”。
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
 * 检查页面脚本是否通过公共路由工具生成 API URL。
 *
 * 项目部署在 /groupproject 这类 context path 下时，硬编码 /api/... 容易失效。
 * 所以普通页面脚本不能直接写 "/api/..."，而要调用 common/ta-recruitment.js
 * 里的 TARecruitment.routes。
 *
 * common/ta-recruitment.js 本身是集中定义 API 路径的地方，所以它被排除在页面脚本检查之外。
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
 * 检查 member5 负责展示的关键页面资产是否存在。
 *
 * 这里不判断页面长什么样，只确认登录/注册、TA、MO、Admin 的主要页面和公共路由文件
 * 没有在整理目录时丢失。
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
 * 检查旧页面是否保持下线状态。
 *
 * applicant-selection 已并入 MO dashboard 子视图，旧技能匹配页面也被
 * 后续 AI 推荐搜索流程替代。admin-register 是旧说明页，当前只保留
 * /admin-invite.jsp 的短邀请码注册入口。
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
 * 递归收集目录下的文件。
 * Node 标准库没有直接提供 walk，这里保持一个很小的本地实现。
 */
function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    return entry.isDirectory() ? walk(fullPath) : [fullPath];
  });
}

/*
 * 去掉注释后再搜索路径字面量。
 *
 * 这样代码注释里提到 API 路径时不会误报，真正被检查的是运行时字符串。
 */
function stripComments(source) {
  return source
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/(^|[^:])\/\/.*$/gm, "$1");
}

// 以下是轻量测试辅助函数，失败时抛 Error，使 shell 脚本返回非 0。
function assertExists(file) {
  assert(fs.existsSync(path.join(projectRoot, file)), `${file} should exist`);
}

function relative(file) {
  return path.relative(projectRoot, file);
}

function pass(message) {
  passed += 1;
  console.log(`[member5] PASS - ${message}`);
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

main();
