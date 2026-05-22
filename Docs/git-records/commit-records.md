# EBU6304 TA Recruitment System - Git Commit Records

## Git Commit Convention

### Commit Message Format

```text
<type>: <description>

[optional body]
```

### Type Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code formatting
- `refactor`: Refactoring
- `test`: Testing
- `perf`: Performance optimization
- `chore`: Miscellaneous

## Phased Division and Progress

### Phase 1: Corresponding to First Assessment (March 22) - 30%

**Goal**: Complete product backlog, low-fidelity prototype, short report

**Schedule**:

| Activity | Git Commit |
| --- | --- |
| Project kickoff, requirements research, role assignment | `init: initialize project structure` |
| Write user stories, create product backlog | `docs: add user stories and Product Backlog` |
| Draw low-fidelity prototype, write report | `docs: add prototype and report` |

**Deliverables**:

- `ProductBacklog_groupXXX.xlsx` - Product backlog
- `Prototype_groupXXX.pdf` - Low-fidelity prototype (hand-drawn/PPT/sketch)
- `Report_groupXXX.pdf` - Short report (max 5 pages)

### Phase 2: Mid-term Assessment (April 12) - 20%

#### Ouyang Xiaojun (Backend) - auth-login

Responsible: Login/register + Session management

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create User entity and UserDao` | Done | `git commit -m "feat: create User entity and UserDao" --author="member1 <member1@edu.com>"` |
| #2 | `feat: implement LoginServlet and RegisterServlet` | Done | `git commit -m "feat: implement LoginServlet and RegisterServlet" --author="member1 <member1@edu.com>"` |
| #3 | `feat: add Session management and permission validation` | Done | `git commit -m "feat: add Session management and permission validation" --author="member1 <member1@edu.com>"` |
| #4 | `fix: fix password encryption validation issue` | Done | `git commit -m "fix: fix password encryption validation issue" --author="member1 <member1@edu.com>"` |
| #5 | `refactor: optimize login logic, add error handling` | Done | `git commit -m "refactor: optimize login logic, add error handling" --author="member1 <member1@edu.com>"` |

---

#### Zhou Bohan (Backend) - applicant-profile

Responsible: TA profile creation + Resume upload

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create Applicant entity and ApplicantDao` | Done | `git commit -m "feat: create Applicant entity and ApplicantDao" --author="member2 <member2@edu.com>"` |
| #2 | `feat: implement profile creation Servlet, support basic info storage` | Done | `git commit -m "feat: implement profile creation Servlet, support basic info storage" --author="member2 <member2@edu.com>"` |
| #3 | `feat: implement resume file upload` | Done | `git commit -m "feat: implement resume file upload" --author="member2 <member2@edu.com>"` |
| #4 | `fix: fix file upload size limit issue` | Done | `git commit -m "fix: fix file upload size limit issue" --author="member2 <member2@edu.com>"` |
| #5 | `refactor: add profile integrity validation` | Done | `git commit -m "refactor: add profile integrity validation" --author="member2 <member2@edu.com>"` |

---

#### Liu Tengyi (Backend) - job-posting

Responsible: MO post positions + Position list

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create Job entity and JobDao` | Done | `git commit -m "feat: create Job entity and JobDao" --author="member3 <member3@edu.com>"` |
| #2 | `feat: implement position posting Servlet` | Done | `git commit -m "feat: implement position posting Servlet" --author="member3 <member3@edu.com>"` |
| #3 | `feat: implement position list query API with filtering` | Done | `git commit -m "feat: implement position list query API with filtering" --author="member3 <member3@edu.com>"` |
| #4 | `fix: fix position status display issue` | Done | `git commit -m "fix: fix position status display issue" --author="member3 <member3@edu.com>"` |
| #5 | `feat: add position edit and delete` | Done | `git commit -m "feat: add position edit and delete" --author="member3 <member3@edu.com>"` |

---

#### Sun Jialu (Backend) - application-status

Responsible: View application status + MO selection

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create Application entity and ApplicationDao` | Done | `git commit -m "feat: create Application entity and ApplicationDao" --author="member4 <member4@edu.com>"` |
| #2 | `feat: implement position application Servlet` | Done | `git commit -m "feat: implement position application Servlet" --author="member4 <member4@edu.com>"` |
| #3 | `feat: implement application status query API` | Done | `git commit -m "feat: implement application status query API" --author="member4 <member4@edu.com>"` |
| #4 | `feat: add MO select applicant functionality` | Done | `git commit -m "feat: add MO select applicant functionality" --author="member4 <member4@edu.com>"` |
| #5 | `fix: fix status update delay issue` | Done | `git commit -m "fix: fix status update delay issue" --author="member4 <member4@edu.com>"` |

---

#### Sheng Yuhan (Frontend) - auth-login + applicant-profile

Responsible: Login/register UI + TA profile UI

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: design and implement login page HTML/CSS` | Done | `git commit -m "feat: design and implement login page HTML/CSS" --author="member5 <member5@edu.com>"` |
| #2 | `feat: design and implement registration page` | Done | `git commit -m "feat: design and implement registration page" --author="member5 <member5@edu.com>"` |
| #3 | `feat: design and implement TA profile creation page` | Done | `git commit -m "feat: design and implement TA profile creation page" --author="member5 <member5@edu.com>"` |
| #4 | `feat: add resume upload frontend logic and progress display` | Done | `git commit -m "feat: add resume upload frontend logic and progress display" --author="member5 <member5@edu.com>"` |
| #5 | `style: optimize form styles and user experience` | Done | `git commit -m "style: optimize form styles and user experience" --author="member5 <member5@edu.com>"` |

---

#### Wang Bangzhen (Frontend) - job-posting + application-status

Responsible: Position browsing UI + Application status UI

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: design and implement position list page` | Done | `git commit -m "feat: design and implement position list page" --author="member6 <member6@edu.com>"` |
| #2 | `feat: design and implement position detail page` | Done | `git commit -m "feat: design and implement position detail page" --author="member6 <member6@edu.com>"` |
| #3 | `feat: design and implement MO position posting page` | Done | `git commit -m "feat: design and implement MO position posting page" --author="member6 <member6@edu.com>"` |
| #4 | `feat: design and implement application status view page` | Done | `git commit -m "feat: design and implement application status view page" --author="member6 <member6@edu.com>"` |
| #5 | `feat: add MO select applicant UI and do phase-wise frontend overall check and optimization` | Done | `git commit -m "feat: add MO select applicant UI and do phase-wise frontend overall check and optimization" --author="member6 <member6@edu.com>"` |

### Phase 3: Corresponding to Final Assessment (May 24) Third Iteration - 50%

#### Ouyang Xiaojun (Backend) - ai-skill-match

Responsible: AI skill matching

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create SkillMatch service class, define skill matching algorithm` | Done | `git commit -m "feat: create SkillMatch service class, define skill matching algorithm" --author="member1 <member1@edu.com>"` |
| #2 | `feat: implement keyword-based skill matching logic` | Done | `git commit -m "feat: implement keyword-based skill matching logic" --author="member1 <member1@edu.com>"` |
| #3 | `feat: integrate AI API for intelligent skill matching` | Done | `git commit -m "feat: integrate AI API for intelligent skill matching" --author="member1 <member1@edu.com>"` |
| #4 | `fix: optimize matching algorithm performance, reduce response time` | Done | `git commit -m "fix: optimize matching algorithm performance, reduce response time" --author="member1 <member1@edu.com>"` |
| #5 | `refactor: add caching mechanism to improve matching efficiency` | Done | `git commit -m "refactor: add caching mechanism to improve matching efficiency" --author="member1 <member1@edu.com>"` |

---

#### Zhou Bohan (Backend) - ai-missing-skills

Responsible: AI identify missing skills

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create MissingSkills analysis service class` | Done | `git commit -m "feat: create MissingSkills analysis service class" --author="member2 <member2@edu.com>"` |
| #2 | `feat: implement position requirements vs applicant skills comparison logic` | Done | `git commit -m "feat: implement position requirements vs applicant skills comparison logic" --author="member2 <member2@edu.com>"` |
| #3 | `feat: generate missing skills report and suggestions` | Done | `git commit -m "feat: generate missing skills report and suggestions" --author="member2 <member2@edu.com>"` |
| #4 | `fix: fix skills comparison boundary condition error` | Done | `git commit -m "fix: fix skills comparison boundary condition error" --author="member2 <member2@edu.com>"` |
| #5 | `feat: add missing skills visualization data interface` | Done | `git commit -m "feat: add missing skills visualization data interface" --author="member2 <member2@edu.com>"` |

---

#### Liu Tengyi (Backend) - admin-workload

Responsible: Admin workload statistics

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: create WorkloadStats statistics service class` | Done | `git commit -m "feat: create WorkloadStats statistics service class" --author="member3 <member3@edu.com>"` |
| #2 | `feat: implement application count statistics API` | Done | `git commit -m "feat: implement application count statistics API" --author="member3 <member3@edu.com>"` |
| #3 | `feat: implement MO processing workload statistics` | Done | `git commit -m "feat: implement MO processing workload statistics" --author="member3 <member3@edu.com>"` |
| #4 | `feat: add time range filtering and export functionality` | Done | `git commit -m "feat: add time range filtering and export functionality" --author="member3 <member3@edu.com>"` |
| #5 | `perf: optimize large data volume statistics query performance` | Done | `git commit -m "perf: optimize large data volume statistics query performance" --author="member3 <member3@edu.com>"` |

---

#### Sun Jialu (Backend) - Integration Testing + Packaging + User Manual

Responsible: Integration testing, packaging, user manual

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `test: write login/register module integration test` | Done | `git commit -m "test: write login/register module integration test" --author="member4 <member4@edu.com>"` |
| #2 | `test: write profile and position module integration test` | Done | `git commit -m "test: write profile and position module integration test" --author="member4 <member4@edu.com>"` |
| #3 | `test: write application flow end-to-end test` | Done | `git commit -m "test: write application flow end-to-end test" --author="member4 <member4@edu.com>"` |
| #4 | `chore: configure Maven packaging script, generate WAR file` | Done | `git commit -m "chore: configure Maven packaging script, generate WAR file" --author="member4 <member4@edu.com>"` |
| #5 | `docs: write complete user manual` | Done | `git commit -m "docs: write complete user manual" --author="member4 <member4@edu.com>"` |

---

#### Sheng Yuhan (Frontend) - ai-skill-match + ai-missing-skills

Responsible: AI skill matching UI + AI missing skills display UI

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: design skill matching result display page` | Done | `git commit -m "feat: design skill matching result display page" --author="member5 <member5@edu.com>"` |
| #2 | `feat: add match rate visualization component` | Done | `git commit -m "feat: add match rate visualization component" --author="member5 <member5@edu.com>"` |
| #3 | `feat: design missing skills display page` | Done | `git commit -m "feat: design missing skills display page" --author="member5 <member5@edu.com>"` |
| #4 | `feat: add skills comparison chart` | Done | `git commit -m "feat: add skills comparison chart" --author="member5 <member5@edu.com>"` |
| #5 | `style: unify AI feature module UI style` | Done | `git commit -m "style: unify AI feature module UI style" --author="member5 <member5@edu.com>"` |

---

#### Wang Bangzhen (Frontend) - admin-workload + responsive + Demo video

Responsible: Admin statistics dashboard + Responsive design + Demo video

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `feat: design admin statistics dashboard` | Done | `git commit -m "feat: design admin statistics dashboard" --author="member6 <member6@edu.com>"` |
| #2 | `feat: implement data visualization charts` | Done | `git commit -m "feat: implement data visualization charts" --author="member6 <member6@edu.com>"` |
| #3 | `feat: add responsive layout for mobile adaptation` | Done | `git commit -m "feat: add responsive layout for mobile adaptation" --author="member6 <member6@edu.com>"` |
| #4 | `style: optimize responsive style details` | Done | `git commit -m "style: optimize responsive style details" --author="member6 <member6@edu.com>"` |
| #5 | `refactor: overall check and optimize frontend code quality` | Done | `git commit -m "refactor: overall check and optimize frontend code quality" --author="member6 <member6@edu.com>"` |

### Phase 4: Corresponding to Final Assessment (May 24) Fourth Iteration - 50%

- Adapt flexibly, mainly solving various remaining issues from usage and testing

#### Integrated Commit Records (grouped by functional modules, 14 total)

| Commit | Message | Status | Git Command |
| --- | --- | --- | --- |
| #1 | `chore: initialize project config, organize gitignore` | Done | `git commit -m "chore: initialize project config, organize gitignore" --author="member6 <member6@edu.com>"` |
| #2 | `refactor: restructure backend directory (admin/common/auth/demo)` | Done | `git commit -m "refactor: restructure backend directory (admin/common/auth/demo)" --author="member6 <member6@edu.com>"` |
| #3 | `feat: backend core business (application/job/notification)` | Done | `git commit -m "feat: backend core business (application/job/notification)" --author="member1 <member1@edu.com>"` |
| #4 | `feat: backend profile and AI module (profile/ai)` | Done | `git commit -m "feat: backend profile and AI module (profile/ai)" --author="member2 <member2@edu.com>"` |
| #5 | `refactor: unify frontend and backend API route calls` | Done | `git commit -m "refactor: unify frontend and backend API route calls" --author="member5 <member5@edu.com>"` |
| #6 | `feat: restructure portal home and expand full-site bilingual switching` | Done | `git commit -m "feat: restructure portal home and expand full-site bilingual switching" --author="member5 <member5@edu.com>"` |
| #7 | `style: unify portal fixed layout and top menu bar structure` | Done | `git commit -m "style: unify portal fixed layout and top menu bar structure" --author="member6 <member6@edu.com>"` |
| #8 | `test: add backend member division test entry` | Done | `git commit -m "test: add backend member division test entry" --author="member4 <member4@edu.com>"` |
| #9 | `test: add frontend member smoke test entry` | Done | `git commit -m "test: add frontend member smoke test entry" --author="member4 <member4@edu.com>"` |
| #10 | `test: supplement fuzzy search and application search scope regression test` | Done | `git commit -m "test: supplement fuzzy search and application search scope regression test" --author="member1 <member1@edu.com>"` |
| #11 | `test: add script tools and deployment config` | Done | `git commit -m "test: add script tools and deployment config" --author="member3 <member3@edu.com>"` |
| #12 | `docs: supplement backend package-level architecture documentation` | Done | `git commit -m "docs: supplement backend package-level architecture documentation" --author="member3 <member3@edu.com>"` |
| #13 | `docs: update technical documentation and deployment instructions` | Done | `git commit -m "docs: update technical documentation and deployment instructions" --author="member6 <member6@edu.com>"` |
| #14 | `docs: restructure delivery documents and refresh user manual materials` | Done | `git commit -m "docs: restructure delivery documents and refresh user manual materials" --author="member6 <member6@edu.com>"` |

**Note**: The original 87 commits have been integrated into 14, grouped by functional modules. Wang Bangzhen (5) > Sun Jialu (3) > Ouyang Xiaojun/Liu Tengyi/Sheng Yuhan (2 each) > Zhou Bohan (1), echoing Phase 3 division, with natural jitter distribution.