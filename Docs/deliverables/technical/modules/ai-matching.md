# AI Recommendation Module Technical Documentation

## 1. Module Overview

AI module only retains front-end visible recommendation search capability:

1. **MO Applicant Recommendation**: MO searches for candidates based on job and optional keywords on job management page.
2. **TA Job Recommendation**: TA searches for open jobs based on personal profile and optional keywords on job list page.

Detail page individual job/application's extra AI analysis chain has been removed; related client, configuration, local analysis fallback, and corresponding API are no longer retained.

**Core Components**:

- `DeepSeekAiConfig` - Recommendation search configuration read
- `DeepSeekApplicantSearchClient` - MO applicant recommendation client
- `DeepSeekTaJobSearchClient` - TA job recommendation client
- `MoApplicantAiSearchService` - MO applicant recommendation service
- `TaJobAiSearchService` - TA job recommendation service
- `MoApplicantAiSearchServlet` - MO recommendation search API
- `TaJobAiSearchServlet` - TA recommendation search API

---

## 2. Layered Architecture

```text
AI Recommendation Servlets
  -> AI Search Services
      -> prompt building / privacy filtering / result mapping
      -> DeepSeek...Client
  -> ApiResponses
```

| Layer | Responsibility |
|-------|----------------|
| `ai/web` | Validate current user role, read parameters, load domain objects, write unified JSON |
| `ai/service` | Build recommendation context, desensitize, organize AI return results and recommendation business rules |
| `ai/client` | Read configuration, call DeepSeek compatible API, parse returned text |
| `common/api` | Unified maintenance of `/api/...` route constants |

---

## 3. API

| Function | Method | Path | Permission |
|------|--------|------|------|
| MO applicant recommendation | POST | `/api/mo/applicant-recommendations` | MO |
| TA job recommendation | POST | `/api/ta/job-recommendations` | TA |

### 3.1 MO Applicant Recommendation

`MoApplicantAiSearchServlet` requires `jobId`, optional `query`. Backend only generates recommendation context based on jobs posted by current MO and corresponding applications.

### 3.2 TA Job Recommendation

`TaJobAiSearchServlet` optional `query`. Backend reads current TA profile, excludes non-open jobs and jobs already applied by current TA.

---

## 4. Configuration

Template file:

```text
frontend/webapp/WEB-INF/ai/deepseek.properties.template
```

Local configuration file:

```text
frontend/webapp/WEB-INF/ai/deepseek.local.properties
```

`DeepSeekAiConfig` reading priority is: local properties file, System Property, Environment Variable.

```properties
deepseek.api.key=your-actual-api-key
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
deepseek.timeout-ms=8000
```

---

## 5. Frontend Call Method

All AI API URLs are generated via `TARecruitment.routes`.

TA job recommendation:

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

MO applicant recommendation:

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

## 6. Error Handling

| Scenario | Response |
|------|------|
| Not logged in | 401 JSON |
| Role mismatch | 403 JSON |
| Missing `jobId` | 400 JSON |
| Job or profile not found | 404 JSON |
| DeepSeek recommendation unavailable | 503 JSON, no local fake recommendation returned |

---

## 7. Testing

Recommendation check:

```bash
bash -n scripts/dev.sh scripts/javadocs.sh
find frontend/webapp/js -name "*.js" -print0 | xargs -0 -n1 node --check
./scripts/javadocs.sh
```