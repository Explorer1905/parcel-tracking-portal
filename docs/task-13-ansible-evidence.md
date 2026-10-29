# Task 13 – Ansible Configuration Management
## Evidence Document (PostgreSQL Architecture Update)

**Branch:** `ops/ansible-postgres`  
**Date:** 29 October 2026

---

## What Was Done

Rewrote the entire Ansible configuration from the old H2/WAR-copy architecture
to manage the PostgreSQL 16 + Docker Compose production stack.

---

## Ansible Structure

```
ansible/
├── inventory.ini              # Target: localhost (WSL Ubuntu, Docker Desktop WSL integration)
├── site.yml                   # Master: provisions + deploys (idempotent)
├── deploy.yml                 # Deploy a specific release tag
├── rollback.yml               # Roll back to an older image tag
├── backup.yml                 # pg_dump → gzipped backup with 7-day retention
├── restore.yml                # Restore database from backup file
├── README.md                  # All commands with expected output
└── roles/
    ├── packages/tasks/main.yml       # Docker prereqs, curl, python3-docker
    ├── service-user/tasks/main.yml   # 'parcel' system user + group
    ├── app-dirs/tasks/main.yml       # 4 directories + compose file copy
    ├── app-config/
    │   ├── tasks/main.yml            # Render .env from template (no_log)
    │   └── templates/env.j2          # Jinja2 template (secrets via vault/extra-vars)
    ├── firewall/tasks/main.yml       # ufw: allow 8082, 5000, 5434
    ├── deploy/tasks/main.yml         # docker pull + docker compose up -d
    └── health-check/tasks/main.yml   # ansible.builtin.uri until /health = OK
```

---

## Key Commands

### Syntax Check
```bash
ansible-playbook -i ansible/inventory.ini ansible/site.yml --syntax-check
```
**Expected output:** `playbook: ansible/site.yml` (no errors)

### Full Provision + Deploy (First Run)
```bash
ansible-playbook -i ansible/inventory.ini ansible/site.yml \
  -e release_tag=1.1.42 \
  -e postgres_password=parcel_pass
```

### Idempotency (Second Run – must show changed=0)
```bash
ansible-playbook -i ansible/inventory.ini ansible/site.yml \
  -e release_tag=1.1.42 \
  -e postgres_password=parcel_pass
```
**Expected:** `PLAY RECAP  localhost : ok=N  changed=0  unreachable=0  failed=0`

### Deploy New Release
```bash
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml \
  -e release_tag=1.1.50 -e postgres_password=parcel_pass
```

---

## YAML Validation

All 12 YAML files validated clean:
```
python -c "import yaml; [yaml.safe_load(open(f)) for f in [...]]"
# → All YAML valid
```

---

## Screenshots Required for Viva

Capture and add to `docs/screenshots/task-13/`:
- Terminal showing `ansible-playbook --syntax-check` → clean
- Terminal showing first run (changed > 0)
- Terminal showing second run (changed = 0) ← **idempotency proof**
- Health check passing: `ok: [localhost]  msg: "Health check OK"`
