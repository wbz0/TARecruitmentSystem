# 职位管理模块技术文档

## 1. 模块概述

职位管理模块支持 MO 发布、编辑、删除职位，TA 和 MO 浏览职位列表/详情，并为申请流程和 AI 推荐提供职位数据。

**核心组件**：
- `Job` - 职位模型、状态枚举和 CSV 序列化
- `JobDao` - 职位 CSV 读写、查询、搜索和统计
- `JobRequestMapper` - pathInfo、表单字段、技能列表、截止时间等请求转换
- `JobResponseMapper` - 职位列表/详情 payload 组装
- `JobValidator` - 创建和更新职位的字段校验
- `JobService` - 职位列表筛选、详情、创建、更新、删除和权限相关业务校验
- `JobServlet` - `/api/jobs` HTTP 薄入口

---

## 2. 分层职责

```text
JobServlet
    -> JobRequestMapper
    -> JobService
        -> JobValidator
        -> JobDao / ApplicationDao / UserDao
        -> JobResponseMapper
```

| 层次 | 职责 |
|------|------|
| `web` | 解析 HTTP 方法、pathInfo、当前用户和请求参数，调用 `JobService` |
| `service` | 承载筛选、权限校验、默认值、状态规则和跨 DAO 协作 |
| `dao` | 只负责 CSV 读写和查询 |
| `mapper` | 转换 HTTP 参数和响应 payload |
| `validator` | 校验必填、长度、日期、危险输入和数值范围 |
| `model` | 表达职位字段、状态枚举和 CSV 格式 |

---

## 3. 数据模型

### 3.1 Job

**路径**: `backend/src/com/example/tarecruitment/job/model/Job.java`

主要字段包括：

| 字段 | 说明 |
|------|------|
| `jobId` | 职位 ID |
| `moId` / `moName` | 发布该职位的 MO |
| `title` | 职位标题 |
| `courseCode` / `courseName` | 课程代码和课程名称 |
| `description` | 职位描述 |
| `requiredSkills` | 技能列表 |
| `positions` | 招聘人数 |
| `weeklyHours` | 每周工作小时数 |
| `workStartDate` / `workEndDate` | 工作周期 |
| `salary` | 薪资说明 |
| `deadline` | 申请截止时间 |
| `status` | `OPEN` / `CLOSED` / `FILLED` |

`getEffectiveStatus(...)` 会根据显式状态和截止时间计算展示状态：已招满优先，其次手动关闭，其次截止时间自动关闭。

### 3.2 CSV

职位数据由 `JobDao` 写入 `TA_HIRING_DATA_DIR` 下的 jobs CSV 文件。模型负责 CSV 字段序列化/反序列化，DAO 负责文件读写。

---

## 4. API

**Servlet**: `backend/src/com/example/tarecruitment/job/web/JobServlet.java`

| 功能 | Method | Path | 权限 |
|------|--------|------|------|
| 职位列表 | GET | `/api/jobs` | 公开 |
| 职位详情 | GET | `/api/jobs/{jobId}` | 公开 |
| 创建职位 | POST | `/api/jobs` | MO |
| 更新职位 | PUT | `/api/jobs/{jobId}` | 职位所属 MO |
| 删除职位 | DELETE | `/api/jobs/{jobId}` | 职位所属 MO |

列表查询参数：

| 参数 | 说明 |
|------|------|
| `keyword` | 关键词模糊搜索 |
| `courseCode` | 课程代码筛选 |
| `status` | 有效状态筛选 |
| `moId` | MO 本人职位筛选 |

创建/更新字段由 `JobRequestMapper` 白名单读取，避免未知 request 参数直接进入业务对象。

---

## 5. 业务流程

### 5.1 MO 创建职位

```text
MO 提交职位表单
  -> JobServlet 读取白名单参数
  -> JobService 校验当前用户是 MO
  -> JobValidator 校验字段
  -> JobService 组装 Job 模型和默认值
  -> JobDao 写入 CSV
  -> JobResponseMapper 返回 jobId
```

### 5.2 MO 更新职位

更新接口使用 HTTP `PUT`，但业务上采用“只覆盖请求携带字段”的局部更新风格。PUT 表单体由 `JobRequestMapper.formParameters(...)` 手动解析，以兼容部分 Servlet 容器不会自动解析 PUT 表单参数的问题。

### 5.3 删除职位

删除前先读取职位并确认 `job.moId` 等于当前 MO 的用户 ID。Admin 不通过该业务接口代删职位。

---

## 6. 前端调用方式

前端通过 `frontend/webapp/js/common/ta-recruitment.js` 生成 URL，不手写 API 字符串。

```javascript
const url = TARecruitment.routes.jobs.list({
    keyword: keyword,
    status: status,
    courseCode: courseCode
});

TARecruitment.api.request(url, {
    headers: { "X-Requested-With": "XMLHttpRequest" }
}).then(function (result) {
    renderJobs(result.payload.data);
});
```

职位详情使用：

```javascript
const detailUrl = TARecruitment.routes.jobs.detail(jobId);
```

---

## 7. 错误处理

| 场景 | 状态码 | 消息示例 |
|------|--------|----------|
| 未登录创建职位 | 401 | `Please login first` |
| 非 MO 创建职位 | 403 | `Only MO can post jobs` |
| 操作他人职位 | 403 | `You can only update your own jobs` |
| 缺少职位 ID | 400 | `Job ID is required` |
| 职位不存在 | 404 | `Job not found` |
| 字段校验失败 | 400 | 具体字段错误 |

所有 JSON 响应由 `ApiResponses` 输出 `{ success, message, data }` 结构。

---

## 8. 测试

推荐检查：

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```
