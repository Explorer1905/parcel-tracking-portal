# Task 15 – Final End-to-End Run + Viva Preparation
## Evidence Document

**Branch:** `docs/phase-d`  
**Date:** 29 October 2026

---

## Final Test Results (All Phases Complete)

```
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
├── ApiHelperTest              (16 tests – JSON/CSV escaping, jsonField extraction)
├── ParcelServiceValidationTest (17 tests – weight, priority, date, agent, ID validation)
└── ParcelPortalSeleniumTest    ( 6 tests – full browser automation against running stack)

Maven command: mvn test "-Dselenium.baseUrl=http://localhost:8090/parcel-tracking-portal"
BUILD SUCCESS. Total time: ~30s
```

---

## Final Endpoint Smoke Test

```bash
# Health
curl http://localhost:8090/parcel-tracking-portal/health
# → OK

# API Summary
curl http://localhost:8090/parcel-tracking-portal/api/summary
# → {"total":18,"inTransit":6,"delivered":2,"delayed":10}

# API Parcels (paginated)
curl "http://localhost:8090/parcel-tracking-portal/api/parcels?size=2&page=1"
# → {"total":18,"page":1,"size":2,"parcels":[...]}

# Single parcel with events
curl http://localhost:8090/parcel-tracking-portal/api/parcels/DEMO-1001
# → {...,"events":[...]}

# CSV export
curl -I http://localhost:8090/parcel-tracking-portal/export/parcels.csv
# → Content-Disposition: attachment; filename="parcels.csv"

# Flyway migration history
docker exec parcel-db psql -U parcel -d parcels \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
# → V1 create schema: t, demo data: t, V2 add parcel details: t, demo data: t
```

---

## All 15 Tasks – Final Status

| # | Task | Status | Branch | Key Evidence |
|---|------|--------|--------|-------------|
| 1 | Problem definition | ✅ | task/01-problem-definition | `docs/task-01-problem-definition.docx` |
| 2 | Agile plan + DevOps lifecycle | ✅ | task/02-agile-planning | `docs/task-02-agile-planning.docx` |
| 3 | Architecture + SRS + API | ✅ | task/03-architecture-setup | `docs/API.md`, diagrams in screenshots |
| 4 | GitHub repo setup | ✅ | task/04-repo-init | Protected main, branch strategy |
| 5 | Feature branch + PR + review | ✅ | feature/parcel-entry | GitHub PR with comments |
| 6 | MVP + merge conflict + tag v1.0 | ✅ | task/06-mvp-completion | Conflict resolution commit, v1.0 tag |
| 7 | Jenkins CI job | ✅ | task/07-jenkins-ci | Jenkins `parcel-continuous-testing` job |
| 8 | Jenkinsfile pipeline-as-code | ✅ | task/08-pipeline-as-code | `Jenkinsfile`, 5 stages |
| 9 | Selenium tests (≥5) | ✅ | task/09-selenium-tests | 6 Selenium tests, failure screenshots |
| 10 | Selenium quality gate + defect | ✅ | task/10-continuous-testing | Deliberate bug → red build → fix → green |
| 11 | Docker containerisation | ✅ | task/11-docker | `Dockerfile`, compose lifecycle commands |
| 12 | Jenkins-Docker CD versioned | ✅ | task/12-jenkins-docker-cd | Push to localhost:5000, versioned tags |
| 13 | Ansible config management | ✅ | ops/ansible-postgres | 7 roles, 5 playbooks, YAML validated |
| 14 | Provisioning + idempotency + rollback | ✅ | ops/ansible-postgres | changed=0 on 2nd run, rollback demo |
| 15 | Final end-to-end + docs | ✅ | docs/phase-d | This file + README.md + API.md + VIVA_QA.md |

---

## What Makes This "Not Basic" (Professor's Concern Addressed)

| Concern | Before | After |
|---------|--------|-------|
| "No database" | H2 in-memory (wiped on restart) | PostgreSQL 16 with named Docker volume, Flyway migrations |
| "Too basic" | 5-field parcel form | weight, priority, expected delivery, assigned agent + CHECK constraints |
| Limited API | HTML forms only | REST API (5 endpoints) + CSV export + public /track page |
| Few tests | ~3 Selenium tests | 39 tests (17 unit + 16 unit + 6 Selenium) |
| No validation | Server-side only | Full validation before DAO call, 17 unit tests, DB constraints |
| Basic CI | Simple freestyle job | Full pipeline: build → unit test → Selenium → push image → deploy → health |
| Old Ansible | WAR copy to /tmp | 7 roles: provisioning, secrets management, idempotency, rollback, backup/restore |
| No docs | Minimal README | README.md, API.md, VIVA_QA.md (40 Q&As), task-13/14 evidence markdown |

---

## Viva Demo Order (recommended)

1. Open `http://localhost:8090/parcel-tracking-portal/` in browser
2. Navigate to `/track` — search for `DEMO-1001` (shows Express priority, 2.5 kg, agent)
3. Navigate to `/dashboard` — show summary cards
4. Navigate to `/alerts` — show delayed/failed parcels
5. Navigate to `/parcels` — register a new parcel with weight and priority
6. Show `curl /api/summary` and `curl /api/parcels?size=3` in terminal
7. Run `mvn test` in another terminal → show 39 tests pass
8. Show Jenkins console: `http://localhost:8080`
9. Show `docker exec parcel-db psql ... SELECT version, description ...` (Flyway history)
10. In WSL: `ansible-playbook --syntax-check` (clean) and explain rollback

**Time estimate:** 10–12 minutes
