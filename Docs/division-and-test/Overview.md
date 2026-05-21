# Team Division and Current Code Files

[Back to Overview](Overview.md)

## Overall Division of Labor

| Contributor | File | Standard Commit Count | Division Overview |
| --- | --- | --- | --- |
| Ouyang Xiaojun | [ouyang-xiaojun.md](ouyang-xiaojun.md) | 18 | Backend foundational capabilities, authentication flow, API response and logging utilities, early skill matching implementation, partial testing/stats contribution |
| Zhou Bohan | [zhou-bohan.md](zhou-bohan.md) | 20 | TA profile and file upload, data path/initialization stability, AI recommendation search and matching service |
| Liu Tengyi | [liu-tengyi.md](liu-tengyi.md) | 19 | Position posting/query/validation, workload statistics interface, account profile sync, AI config templates |
| Sun Jialu | [sun-jialu.md](sun-jialu.md) | 23 | Application flow, status transitions, TA withdrawal, MO selection, notifications and invite code service, integration testing/early user manual work |
| Sheng Yuhan | [sheng-yuhan.md](sheng-yuhan.md) | 34 | Frontend pages and interactions, covering login/register, TA/MO/Admin pages, frontend API routing unification |
| Wang Bangzhen | [wang-bangzhen.md](wang-bangzhen.md) | 41 | Project leader / architecture restructuring, common configuration, documentation/scripts organization, portal shell, common styles, Admin pages and full-site bilingual resources |

## Individual Test Presentation Entry

Each contributor only needs to run their own script during the defense, no need to run the unified total test.

For overlapping files and defense attribution, see: [overlap-and-defense-ownership.md](overlap-and-defense-ownership.md).

| Contributor | Test Command | Test Code |
| --- | --- | --- |
| Ouyang Xiaojun | `./scripts/test/test-ouyang-xiaojun.sh` | `backend/test/OuyangXiaojunBackendTest.java` |
| Zhou Bohan | `./scripts/test/test-zhou-bohan.sh` | `backend/test/ZhouBohanBackendTest.java` |
| Liu Tengyi | `./scripts/test/test-liu-tengyi.sh` | `backend/test/LiuTengyiBackendTest.java` |
| Sun Jialu | `./scripts/test/test-sun-jialu.sh` | `backend/test/SunJialuBackendTest.java` |
| Sheng Yuhan | `./scripts/test/test-sheng-yuhan.sh` | `frontend/test/sheng-yuhan-frontend-test.js` |
| Wang Bangzhen | `./scripts/test/test-wang-bangzhen.sh` | `frontend/test/wang-bangzhen-architecture-test.js` |
