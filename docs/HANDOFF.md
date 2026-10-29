# Parcel Tracking Portal – HANDOFF.md

> Last updated: Phase A1 complete (2026-10-29)  
> Author/Executor: Antigravity (AI agent)  
> Viva: 30 October 2026

---

## Current Branch

`feature/parcel-details` — branched from `feature/postgres-database`

**Base chain:** `main` ← `develop` ← `feature/postgres-database` ← `feature/parcel-details`

---

## What Has Been Built (full history)

| Task | Status | Branch |
|------|--------|--------|
| 1 Problem definition | ✅ Done | task/01-problem-definition |
| 2 Agile plan + DevOps lifecycle | ✅ Done | task/02-agile-planning |
| 3 Architecture, SRS, diagrams, API | ✅ Done | task/03-architecture-setup |
| 4 GitHub repo setup | ✅ Done | task/04-repo-init |
| 5 Feature branch + PR + review | ✅ Done | feature/parcel-entry |
| 6 MVP + merge conflict + tag | ✅ Done | task/06-mvp-completion |
| 7 Jenkins CI job | ✅ Done | task/07-jenkins-ci |
| 8 Jenkinsfile + deploy + parameter | ✅ Done | task/08-pipeline-as-code |
| 9 Selenium tests + failure screenshots | ✅ Done | task/09-selenium-tests |
| 10 Selenium gate + deliberate defect + fix | ✅ Done | task/10-quality-gate |
| 11 Dockerfile + container lifecycle | ✅ Done | task/11-docker |
| 12 Jenkins-Docker CD with versioned images | ✅ Done | task/12-jenkins-docker-cd |
| 13 Ansible configuration management | ⚠️ Old (pre-PostgreSQL) | task/13-configuration-management |
| 14 Provisioning, idempotency, health, rollback | ⚠️ Old (pre-PostgreSQL) | task/14-provisioning-reliability |
| 15 Final end-to-end run + docs | ⚠️ Needs update for PostgreSQL | task/15-final-release-viva |

**Phase A1 (richer parcel record)** — ✅ COMPLETE — awaiting user PR review  
**Phase A2 (public API + /track + CSV export)** — ✅ COMPLETE — awaiting user PR review  
**Phase C (Ansible rewrite for PostgreSQL)** — ✅ COMPLETE — awaiting user PR review  
**Phase D (docs, reports, README, VIVA_QA)** — 🔲 Not started  
**Phase A3 (dashboard charts + pagination)** — 🔲 Not started  
**Phase B (auth/roles)** — 🔲 LAST / OPTIONAL  

---

## Phase A1 — What Changed

**Branch:** `feature/parcel-details` (off `feature/postgres-database`)

### Files added / modified

| File | Change |
|------|--------|
| `src/main/resources/db/migration/V2__add_parcel_details.sql` | NEW – adds weight_kg, priority, expected_delivery, assigned_agent columns + constraints + index |
| `src/main/resources/db/demo/R__demo_data.sql` | EXTENDED – idempotent UPDATEs for all 10 DEMO parcels with varied data |
| `src/main/java/com/parcel/model/Parcel.java` | Extended record (11 fields) + 7-arg backward-compat constructor |
| `src/main/java/com/parcel/dao/ParcelDao.java` | SELECT/INSERT new columns; 23514 → IllegalArgumentException |
| `src/main/java/com/parcel/service/ParcelService.java` | Injection constructor; 9-arg addParcel with full validation before DAO |
| `src/main/java/com/parcel/controller/ParcelServlet.java` | Reads optional weight/priority/expectedDelivery/agent params |
| `src/main/webapp/WEB-INF/views/parcels.jsp` | New optional inputs + Priority/Weight/Expected table columns |
| `src/main/webapp/WEB-INF/views/parcel.jsp` | New details block (priority badge, weight, date, agent) |
| `src/main/webapp/assets/styles.css` | .hint, .badge, .badge-*, .detail-grid CSS |
| `src/test/java/com/parcel/ParcelServiceValidationTest.java` | NEW – 17 JUnit 5 unit tests (no DB) |
| `docs/report_meta.json` | NEW – metadata for report generator (fill FILL_ME fields) |
| `docs/templates/report_template.docx` | NEW – copy of OS Lab format (3).docx |

### Test Results (Phase A1)

- `mvn -B clean package -DskipTests` → **BUILD SUCCESS**
- `mvn test -Dtest=ParcelServiceValidationTest` → **Tests run: 17, Failures: 0, Errors: 0, Skipped: 0**
- `docker compose up -d --build` → parcel-db healthy, parcel-app started, `/health` → `OK`
- V2 migration: V1 and V2 both applied (verified via `flyway_schema_history`)
- Upgrade path: rows created pre-V2 show `weight_kg=1.00`, `priority=Standard`, null date/agent
- `mvn test -Dselenium.baseUrl=http://localhost:8090/parcel-tracking-portal` → *(run in progress — see below)*

---

## Steps for the User After Phase A1

### GitHub Actions
1. Open a Pull Request from `feature/parcel-details` → `develop` on GitHub.
2. Review the changes (V2 migration, new form fields, unit tests).
3. Merge the PR (squash or merge commit, your preference).
4. Also merge `feature/postgres-database` → `develop` if not already done.

### Screenshots to capture (save to `docs/screenshots/`)
- `task-03/` – Updated architecture showing PostgreSQL layer
- `task-06/` – The parcel registration form showing Priority/Weight/Expected fields

### Jenkins
- After merging, trigger a Jenkins build to confirm the pipeline passes with V2.

---

## Phase C — Ansible Rewrite (PostgreSQL Architecture)

**Branch:** `ops/ansible-postgres` (off `feature/public-api`)

### Files Changed

| File | Change |
|------|--------|
| `ansible/inventory.ini` | Rewritten for PostgreSQL+Compose architecture |
| `ansible/site.yml` | Master playbook: provisions then deploys |
| `ansible/deploy.yml` | Deploy specific release_tag; validates args, health-checks |
| `ansible/rollback.yml` | Rolls back to rollback_tag; asserts it differs from current |
| `ansible/backup.yml` | pg_dump via docker exec, timestamped, 7-day retention |
| `ansible/restore.yml` | psql restore into db container, health-check after |
| `ansible/roles/packages/tasks/main.yml` | Docker prereqs, curl, python3-docker |
| `ansible/roles/service-user/tasks/main.yml` | Creates parcel system user/group |
| `ansible/roles/app-dirs/tasks/main.yml` | 4 directories + docker-compose.yml copy |
| `ansible/roles/app-config/tasks/main.yml` | Renders .env from Jinja2 (no_log) |
| `ansible/roles/app-config/templates/env.j2` | .env template (secrets from vault) |
| `ansible/roles/firewall/tasks/main.yml` | Opens 8082, 5000, 5434 via ufw |
| `ansible/roles/deploy/tasks/main.yml` | Pull image + docker compose up |
| `ansible/roles/health-check/tasks/main.yml` | URI poll with retries |
| `ansible/README.md` | 8 command sequences with expected output |
| `ansible/archive/` | Old pre-PostgreSQL files preserved |

### YAML Validation

```
All 12 YAML files: python yaml.safe_load → All YAML valid
```

Full `ansible-playbook --syntax-check` must be run in WSL (see README.md).

---

## Key Environment Details

| Item | Value |
|------|-------|
| Local app URL | http://localhost:8090/parcel-tracking-portal |
| Health endpoint | http://localhost:8090/parcel-tracking-portal/health |
| DB container | parcel-db (PostgreSQL 16, port 5432) |
| App container | parcel-app (Tomcat 10.1, port 8090→8080) |
| Jenkins | http://localhost:8080 — job: parcel-continuous-testing |
| Registry | localhost:5000 — tags 1.0.5, 1.0.7 MUST be preserved |
| Rollback tags | 1.0.5, 1.0.7 (needed for Ansible rollback demo) |

## Commands to Continue

```powershell
# Build
mvn -B clean package -DskipTests

# Compose
docker compose up -d --build

# Run all tests (needs compose up)
mvn test "-Dselenium.baseUrl=http://localhost:8090/parcel-tracking-portal"

# Run only unit tests (no compose needed)
mvn test "-Dtest=ParcelServiceValidationTest"

# Check DB migration state
docker exec parcel-db psql -U parcel -d parcels -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

# Verify V2 columns
docker exec parcel-db psql -U parcel -d parcels -c "SELECT parcel_id, weight_kg, priority, expected_delivery FROM parcels LIMIT 5;"
```

---

## docs/report_meta.json — Action Required

Edit `docs/report_meta.json` and fill every `"FILL_ME"` value before running the report generator (Phase D):

```json
{
  "subject": "DevOps Engineering",
  "professor": "<Professor name from your institute>",
  "assisting_teachers": "<Lab assistant name(s)>",
  "semester": "Semester VII (4th Year)",
  "academic_year": "<e.g. 2025-26>",
  "student_name": "Shravani Chavan",
  "roll_number": "<your roll number>",
  "division": "CMPN A"
}
```

---

## Git Rules Reminder

- Never `git add .` or `git add -A` — add files by name
- Never force-push, never touch main
- Commit author: `Explorer1905 <chavanshravani927@gmail.com>`
- Each phase: feature branch → push → stop → user reviews/merges → next phase branches off that
