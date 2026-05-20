# AI 推荐模块技术文档

## 1. 模块概述

AI 模块只保留前端正常可见的推荐搜索能力：

1. **MO 申请人推荐**：MO 在岗位管理页中根据岗位和可选关键词搜索候选人。
2. **TA 职位推荐**：TA 在职位列表页中根据个人档案和可选关键词搜索开放职位。

详情页单个岗位/申请的额外 AI 分析链路已删除，不再保留相关客户端、配置、本地分析兜底或对应 API。

**核心组件**：

- `DeepSeekAiConfig` - 推荐搜索配置读取
- `DeepSeekApplicantSearchClient` - MO 申请人推荐客户端
- `DeepSeekTaJobSearchClient` - TA 职位推荐客户端
- `MoApplicantAiSearchService` - MO 申请人推荐服务
- `TaJobAiSearchService` - TA 职位推荐服务
- `MoApplicantAiSearchServlet` - MO 推荐搜索 API
- `TaJobAiSearchServlet` - TA 推荐搜索 API

---

## 2. 分层架构

```text
AI Recommendation Servlets
  -> AI Search Services
      -> prompt building / privacy filtering / result mapping
      -> DeepSeek...Client
  -> ApiResponses
```

| 层次 | 职责 |
|------|------|
| `ai/web` | 校验当前用户角色、读取参数、加载领域对象、写统一 JSON |
| `ai/service` | 构造推荐上下文、脱敏、整理 AI 返回结果和推荐业务规则 |
| `ai/client` | 读取配置、调用 DeepSeek 兼容 API、解析返回文本 |
| `common/api` | 统一维护 `/api/...` 路由常量 |

---

## 3. API

| 功能 | Method | Path | 权限 |
|------|--------|------|------|
| MO 申请人推荐 | POST | `/api/mo/applicant-recommendations` | MO |
| TA 职位推荐 | POST | `/api/ta/job-recommendations` | TA |

### 3.1 MO 申请人推荐

`MoApplicantAiSearchServlet` 需要 `jobId`，可选 `query`。后端只会基于当前 MO 自己发布的职位和对应申请生成推荐上下文。

### 3.2 TA 职位推荐

`TaJobAiSearchServlet` 可选 `query`。后端会读取当前 TA 档案，并排除非开放职位和当前 TA 已申请的职位。

---

## 4. 配置

模板文件：

```text
frontend/webapp/WEB-INF/ai/deepseek.properties.template
```

本地配置文件：

```text
frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

`DeepSeekAiConfig` 的读取优先级是：本地 properties 文件、System Property、Environment Variable。

```properties
deepseek.api.key=your-actual-api-key
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
deepseek.timeout-ms=8000
```

---

## 5. 前端调用方式

所有 AI API URL 都通过 `TARecruitment.routes` 生成。

TA 职位推荐：

```javascript
TARecruitment.api.request(TARecruitment.routes.ta.jobRecommendations(), {
    method: "POST",
    headers: {
        "X-Requested-With": "XMLHttpRequest",
        "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({ query: query }).toString()
});
```

MO 申请人推荐：

```javascript
TARecruitment.api.request(TARecruitment.routes.mo.applicantRecommendations(), {
    method: "POST",
    headers: {
        "X-Requested-With": "XMLHttpRequest",
        "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({ jobId: jobId, query: query }).toString()
});
```

---

## 6. 错误处理

| 场景 | 响应 |
|------|------|
| 未登录 | 401 JSON |
| 角色不匹配 | 403 JSON |
| 缺少 `jobId` | 400 JSON |
| 职位或档案不存在 | 404 JSON |
| DeepSeek 推荐不可用 | 503 JSON，不返回本地假推荐 |

---

## 7. 测试

推荐检查：

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```
