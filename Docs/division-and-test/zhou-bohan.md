# Zhou Bohan Division and Current Code Files

[Back to Overview](Overview.md)

## Basic Information

| Item | Content |
| --- | --- |
| Contributor | Zhou Bohan |
| Standard commit count | 20 |
| Division overview | TA profile and file upload, data path/initialization stability, AI recommendation search and matching service |

## Division Overview

Zhou Bohan primarily took on TA profiles, resume/avatar resource upload, data storage path and demo account initialization stability, and later handled AI recommendation search backend integration, including DeepSeek configuration and client. After the old skill matching service and detail page extra analysis flow were taken offline, the AI-related backend scope converged to recommendation search interface.

## Current Corresponding Code Files

TA profile, resume, avatar, and draft data:

- `backend/src/com/example/tarecruitment/profile/model/Applicant.java`
- `backend/src/com/example/tarecruitment/profile/dao/ApplicantDao.java`
- `backend/src/com/example/tarecruitment/profile/mapper/ApplicantProfileRequestMapper.java`
- `backend/src/com/example/tarecruitment/profile/mapper/ApplicantProfileResponseMapper.java`
- `backend/src/com/example/tarecruitment/profile/service/ApplicantProfileService.java`
- `backend/src/com/example/tarecruitment/profile/service/ProfileAssetService.java`
- `backend/src/com/example/tarecruitment/profile/validator/ApplicantProfileInput.java`
- `backend/src/com/example/tarecruitment/profile/validator/ApplicantProfileValidator.java`
- `backend/src/com/example/tarecruitment/profile/validator/ProfileAssetValidator.java`
- `backend/src/com/example/tarecruitment/profile/web/ApplicantProfileServlet.java`
- `backend/src/com/example/tarecruitment/profile/web/ApplicantAssetServlet.java`

Local data path, CSV storage, and demo data:

- `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` (**Overlap note: Zhou Bohan primary; Liu Tengyi's position/config tests depend on this data path capability**)
- `backend/src/com/example/tarecruitment/common/storage/CsvCodec.java`
- `backend/src/com/example/tarecruitment/demo/DemoAccountBootstrapListener.java`
- `backend/src/com/example/tarecruitment/demo/DemoDataSeeder.java`

DeepSeek recommendation search and AI search entry:

- `backend/src/com/example/tarecruitment/ai/client/DeepSeekAiConfig.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekApplicantSearchClient.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekTaJobSearchClient.java`
- `backend/src/com/example/tarecruitment/ai/client/DeepSeekChatClient.java`
- `backend/src/com/example/tarecruitment/ai/service/MoApplicantAiSearchService.java`
- `backend/src/com/example/tarecruitment/ai/service/TaJobAiSearchService.java`
- `backend/src/com/example/tarecruitment/ai/web/MoApplicantAiSearchServlet.java`
- `backend/src/com/example/tarecruitment/ai/web/TaJobAiSearchServlet.java`
- `frontend/webapp/WEB-INF/ai/deepseek.properties.template`

## File Overlap and Defense Attribution

`backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` also appears in Liu Tengyi's documentation because position, account profile, and config tests all need to read the runtime data directory. During defense, the data path infrastructure is presented by Zhou Bohan. Liu Tengyi only explains how the position module depends on it.

## Test Presentation

Run command:

```bash
./scripts/test/test-zhou-bohan.sh
```

Test code:

- `backend/test/ZhouBohanBackendTest.java`

Test coverage points:

- `CsvCodec` correctly handles commas and quotes without breaking CSV columns.
- `StoragePaths` generates runtime directories like applicants, resumes, photos via `TA_HIRING_DATA_DIR`.
- `Applicant` CSV serialization/deserialization preserves profile fields, skills, resume, and photo paths.
- `ApplicantDao` can create profiles and reject duplicate user profiles and duplicate student IDs.
- `DeepSeekAiConfig` safely degrades to "not configured" when no real API key is present or configured as a placeholder.

For defense, you can explain:

Zhou Bohan's test focus is on TA profiles, CSV data layer, and AI recommendation configuration. The script uses a temporary data directory to simulate a real runtime environment, proving that profiles can be safely written to CSV and duplicate profiles are blocked. Meanwhile, when the AI key is not configured, it doesn't generate fake recommendation results but falls back safely.