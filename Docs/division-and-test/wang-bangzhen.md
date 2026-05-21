# Wang Bangzhen Division and Current Code Files

[Back to Overview](Overview.md)

## Basic Information

| Item | Content |
| --- | --- |
| Contributor | Wang Bangzhen |
| Standard commit count | 41 |
| Division overview | Architecture restructuring, common configuration, documentation/scripts organization, portal shell, common styles, Admin pages and full-site bilingual resources |

## Division Overview

Wang Bangzhen is the project leader and overall structure owner. Primarily responsible for directory organization, architecture restructuring, common configuration, documentation/scripts entry points, unified portal shell, full-site common styles, bilingual resources, Admin page restructuring, and later migrated backend old large entries and old package names to the current lightweight layered structure. Although business module files were batch-moved or organized in architecture restructuring commits, the defense attribution still follows each business contributor's functional division.

## Current Corresponding Code Files

Backend architecture, routing, and common infrastructure:

- `backend/src/com/example/tarecruitment/common/api/ApiRoutes.java`
- `backend/src/com/example/tarecruitment/common/service/ServiceResult.java`
- `backend/src/com/example/tarecruitment/common/storage/CsvCodec.java`
- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java`
- `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java`
- `backend/src/com/example/tarecruitment/common/util/Logger.java`
- `backend/src/com/example/tarecruitment/common/web/ApiResponses.java`
- `backend/src/com/example/tarecruitment/common/web/JsonResponseUtil.java`
- `backend/src/com/example/tarecruitment/common/web/PermissionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/SessionUtil.java`
- `backend/src/com/example/tarecruitment/common/web/WebRequests.java`
- `backend/src/com/example/tarecruitment/auth/web/AccessPolicy.java`
- `backend/src/com/example/tarecruitment/package-info.java`
- `frontend/webapp/WEB-INF/web.xml`

Backend business module architecture restructuring notes:

Wang Bangzhen organized the backend business module package structure, routing, and layered boundaries in `4abe022 refactor: unify backend module architecture and API routes`. This commit covered multiple business domain files, but these files' functional attribution are still respectively recorded in Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, and Sun Jialu's documentation. Wang Bangzhen's test presentation should focus on explaining architecture rules, common entry points, and documentation/scripts, rather than repeatedly claiming all business implementation files.

Portal shell, common styles, bilingual resources, and home page:

- `frontend/webapp/index.jsp`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-sidebar.jspf`
- `frontend/webapp/WEB-INF/jsp/fragments/portal-topbar.jspf`
- `frontend/webapp/css/common/components.css`
- `frontend/webapp/css/common/forms.css`
- `frontend/webapp/css/common/motion.css`
- `frontend/webapp/css/common/notifications.css`
- `frontend/webapp/css/common/tokens.css`
- `frontend/webapp/css/portal/portal-home.css`
- `frontend/webapp/css/portal/portal-shell.css`
- `frontend/webapp/js/common/i18n.js`
- `frontend/webapp/js/common/locale-bootstrap.js`
- `frontend/webapp/js/common/portal-i18n.js`
- `frontend/webapp/js/common/ta-recruitment.js`

Admin page overall restructuring:

- `frontend/webapp/jsp/admin/dashboard.jsp`
- `frontend/webapp/jsp/admin/invite.jsp`
- `frontend/webapp/jsp/admin/notifications.jsp`
- `frontend/webapp/css/admin/admin-dashboard.css`
- `frontend/webapp/css/admin/admin-invite-management.css`
- `frontend/webapp/js/admin/admin-dashboard.js`
- `frontend/webapp/js/admin/admin-invite-management.js`
- `frontend/webapp/js/admin/admin-notifications.js`

AI config template:

- `frontend/webapp/WEB-INF/ai/deepseek.properties.template`

Scripts, runtime configuration, and technical documentation:

- `scripts/config.example.bat`
- `scripts/config.example.sh`
- `scripts/dev.bat`
- `scripts/dev.sh`
- `scripts/javadocs.bat`
- `scripts/javadocs.sh`
- `README.md`
- `docs/deliverables/technical/index.md`
- `docs/deliverables/technical/api/servlet-api.md`
- `docs/deliverables/technical/architecture/data-architecture.md`
- `docs/deliverables/technical/architecture/security-architecture.md`
- `docs/deliverables/technical/architecture/system-architecture.md`
- `docs/deliverables/technical/deployment/deployment-guide.md`
- `docs/deliverables/technical/modules/admin-invite.md`
- `docs/deliverables/technical/modules/admin-workload.md`
- `docs/deliverables/technical/modules/ai-matching.md`
- `docs/deliverables/technical/modules/application-review.md`
- `docs/deliverables/technical/modules/authentication.md`
- `docs/deliverables/technical/modules/job-management.md`
- `docs/deliverables/technical/modules/ta-profile.md`

## Test Presentation

Run command:

```bash
./scripts/test/test-wang-bangzhen.sh
```

Test code:

- `frontend/test/wang-bangzhen-architecture-test.js`

Test coverage points:

- Architecture, portal shell, common styles, scripts, and documentation files under responsibility exist.
- No old package names, old Servlets, old API prefixes with version numbers, or old root paths remain in backend source code, frontend pages, scripts, or division documentation.
- Backend API constants in `ApiRoutes.java` remain clean `/api/...` paths.
- `frontend/webapp/js/common/ta-recruitment.js` stays synchronized with backend `ApiRoutes` API paths.
- `package-info.java` and `docs/division-and-test/Overview.md` reflect the current lightweight architecture and contributor documentation entry points.

For defense, you can explain:

Wang Bangzhen's test focus is not to repeatedly test every business function, but to verify the overall project structure has not regressed to the old architecture. The script checks whether old package names, old interfaces, old Servlets, numerous hard-coded paths, and deleted AI pages still remain. It also confirms that backend API constants and frontend common routes stay synchronized.