# Overlapping Files and Defense Attribution

[Back to Overview](Overview.md)

## Explanation Principles

"Current Corresponding Code Files" in contributor documentation is used to show the code scope each person has participated in, depends on, or needs to explain. Some files appear in multiple contributor documents because the project underwent architecture restructuring in the later stage, and business modules also have genuine collaborative relationships.

Handle during defense according to these rules:

- **Primary attribution**: Responsible for explaining the file's design, core logic, test points, and main risks.
- **Collaboration/dependency**: May explain why their business uses the file, without repeating the full implementation of the file.
- Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, Sun Jialu, and Sheng Yuhan business defenses use "primary attribution" as the standard. Wang Bangzhen's overlaps are more of a leader perspective on architecture coverage, not representing repeated claiming of every business file.

## Overlapping Files for Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, Sun Jialu, and Sheng Yuhan

| Overlapping File | Appears In | Defense Primary | Collaboration/Dependency Explanation |
| --- | --- | --- | --- |
| `backend/src/com/example/tarecruitment/common/storage/StoragePaths.java` | Zhou Bohan, Liu Tengyi | Zhou Bohan | Liu Tengyi's position module and config tests depend on runtime data directory. |
| `backend/src/com/example/tarecruitment/common/search/FuzzySearchUtil.java` | Liu Tengyi, Sun Jialu | Liu Tengyi | Sun Jialu's application/stats documentation involves filtering and search capability. |
| `backend/src/com/example/tarecruitment/admin/service/WorkloadStatsService.java` | Liu Tengyi, Sun Jialu | Liu Tengyi | Sun Jialu explains how application status, hiring, and withdrawal affect statistics calculation. |
| `backend/src/com/example/tarecruitment/admin/web/WorkloadStatsServlet.java` | Liu Tengyi, Sun Jialu | Liu Tengyi | Sun Jialu explains the business relationship between application flow and statistics interface. |

## Defense Focus for Each Contributor

| Contributor | Defense Focus | How to Handle Overlapping Files |
| --- | --- | --- |
| Ouyang Xiaojun | Authentication, Session, permissions, unified response, and logging utilities | Authentication and common return structure presented by Ouyang Xiaojun. |
| Zhou Bohan | TA profiles, file upload, CSV/data paths, DeepSeek recommendation search | `StoragePaths.java` presented by Zhou Bohan. AI recommendation search presented by Zhou Bohan. |
| Liu Tengyi | Position posting/query/validation, workload statistics, account profile sync | Workload statistics and search utility presented by Liu Tengyi. |
| Sun Jialu | Application flow, status transitions, notifications, admin invite code business | Workload statistics and common utility only explain business usage scenarios, not repeat implementation. |
| Sheng Yuhan | Frontend pages, interactions, styles, page JS API call methods | Sheng Yuhan has no direct code path overlap with Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, and Sun Jialu. |

## Special Note for Wang Bangzhen

Wang Bangzhen is the project leader and architecture organization role. Documentation may cover common frontend, common backend, scripts, documentation, and architecture migration files. Wang Bangzhen's defense focus is "whether the overall structure is unified, old interfaces are cleaned up, and frontend/backend routing is synchronized", not seizing primary attribution of business implementation from Ouyang Xiaojun, Zhou Bohan, Liu Tengyi, Sun Jialu, and Sheng Yuhan.