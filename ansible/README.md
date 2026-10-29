# Ansible Configuration Management – Parcel Tracking Portal

## Overview

This directory contains Ansible playbooks and roles for the **Parcel Tracking Portal**
(Java 17 / Tomcat 10.1 / PostgreSQL 16 deployed via Docker Compose).

All playbooks are designed to run from **WSL Ubuntu** against `localhost` (the WSL target).
They use Docker Desktop's WSL integration: Docker commands work inside WSL without a
native Linux `dockerd` service.

---

## Architecture

```
parcel_servers (localhost, WSL Ubuntu)
│
├── /opt/parcel-tracking-portal/        ← docker-compose.yml, active_release
├── /etc/parcel-tracking-portal/        ← .env  (mode 640, secrets from vault)
├── /var/lib/parcel-tracking-portal/    ← application data
└── /var/backups/parcel-tracking-portal/← pg_dump backups (7-day retention)

Docker Compose stack (compose_project = parcel-production)
├── parcel-production-db   (postgres:16-alpine, port 5434)
└── parcel-production-app  (Tomcat 10.1 WAR, port 8082)
```

---

## Prerequisites (WSL Ubuntu)

```bash
# Install Ansible and community.general
pip3 install --user ansible
ansible-galaxy collection install community.general

# Verify
ansible --version
```

> **Note:** Docker commands run through Docker Desktop's WSL integration.
> Do NOT attempt to start `dockerd` inside WSL. If `docker ps` works in WSL, you're ready.

---

## Role Summary

| Role | Purpose |
|------|---------|
| `packages` | Installs curl, ca-certificates, python3-docker; verifies Docker is accessible |
| `service-user` | Creates `parcel` system user and group |
| `app-dirs` | Creates the four application directories; copies docker-compose.yml |
| `app-config` | Renders `.env` from Jinja2 template; DB password injected via vault/extra-vars |
| `firewall` | Opens ports 8082, 5000, 5434 via ufw (ignores errors when ufw is inactive) |
| `deploy` | Pulls specified image tag from registry; runs `docker compose up -d` |
| `health-check` | Polls `/health` with retries until `OK` (deployment gate) |

---

## Playbook Commands and Expected Output

### 1. Syntax check (no changes made)

```bash
cd /path/to/parcel-tracking-portal/ansible
ansible-playbook -i inventory.ini site.yml --syntax-check
```

**Expected output:**
```
playbook: site.yml
```
No errors.

---

### 2. Dry-run: check mode with diff

```bash
ansible-playbook -i inventory.ini site.yml --check --diff \
  -e postgres_password=parcel_pass \
  -e release_tag=1.1.42
```

**Expected output (first run):**
```
TASK [packages : Install prerequisite packages] *****
changed: [localhost] (would change)

TASK [service-user : Create parcel system group] *****
changed: [localhost] (would change)

TASK [app-dirs : Create application directories] *****
changed: [localhost] (would change) x4

...

PLAY RECAP
localhost : ok=12 changed=8 unreachable=0 failed=0
```
> `--check` does not actually make changes.

---

### 3. First real run (provisioning)

```bash
ansible-playbook -i inventory.ini site.yml \
  -e postgres_password=parcel_pass \
  -e release_tag=1.1.42 \
  --become-password-file /dev/stdin   # or omit if running as root
```

**Expected output:**
```
TASK [packages : Install prerequisite packages] → changed: [localhost]
TASK [service-user : Create parcel system group] → changed: [localhost]
TASK [app-dirs : Create application directories] → changed: [localhost] x4
TASK [app-config : Render .env file from template] → changed: [localhost]
TASK [deploy : Pull exact image tag 1.1.42] → changed: [localhost]
TASK [deploy : Bring up Docker Compose stack (parcel-production)] → changed: [localhost]
TASK [health-check : Wait for application to be healthy] → ok: [localhost]

PLAY RECAP
localhost : ok=N changed=N unreachable=0 failed=0
```

---

### 4. Second run (idempotency check — must report changed=0)

```bash
ansible-playbook -i inventory.ini site.yml \
  -e postgres_password=parcel_pass \
  -e release_tag=1.1.42
```

**Expected output:**
```
PLAY RECAP
localhost : ok=N changed=0 unreachable=0 failed=0
```
> All tasks should be `ok` (no changes), proving idempotency.

---

### 5. Deploy a new release

```bash
ansible-playbook -i inventory.ini deploy.yml \
  -e release_tag=1.1.50 \
  -e postgres_password=parcel_pass
```

**Expected output:**
```
TASK [deploy : Pull exact image tag 1.1.50] → changed: [localhost]
TASK [deploy : Bring up Docker Compose stack] → changed: [localhost]
TASK [health-check : Wait for application to be healthy] → ok: [localhost]
PLAY RECAP  localhost : ok=4 changed=2 unreachable=0 failed=0
```

Verify:
```bash
curl http://localhost:8082/parcel-tracking-portal/health  # → OK
cat /opt/parcel-tracking-portal/active_release            # → 1.1.50
```

---

### 6. Rollback to a previous release

> Tags `1.0.5` and `1.0.7` are preserved in the local registry for this demo.

```bash
ansible-playbook -i inventory.ini rollback.yml \
  -e rollback_tag=1.0.5 \
  -e postgres_password=parcel_pass
```

**Expected output:**
```
TASK [Show rollback plan] → "Rolling back from 1.1.50 to 1.0.5"
TASK [deploy : Pull exact image tag 1.0.5] → ok: [localhost]
TASK [deploy : Bring up Docker Compose stack] → changed: [localhost]
TASK [health-check : Wait for application to be healthy] → ok: [localhost]
TASK [Rollback complete] → "Rollback successful. Running tag: 1.0.5. DB volume NOT modified."
PLAY RECAP  localhost : ok=6 changed=1 unreachable=0 failed=0
```

---

### 7. Backup the database

```bash
ansible-playbook -i inventory.ini backup.yml
```

**Expected output:**
```
TASK [Create database backup via pg_dump in container] → changed: [localhost]
TASK [Show backup result] → "Backup created: /var/backups/parcel-tracking-portal/parcels_20261029_143000.sql.gz Size: 12345 bytes"
```

---

### 8. Restore from a backup

```bash
ansible-playbook -i inventory.ini restore.yml \
  -e backup_file=/var/backups/parcel-tracking-portal/parcels_20261029_143000.sql.gz
```

**Expected output:**
```
TASK [Confirm restore] → "RESTORING backup .../parcels_20261029_143000.sql.gz..."
TASK [Restore database from backup] → changed: [localhost]
TASK [Show restore result] → "Restore completed successfully"
```

---

## Secrets Management

DB credentials are **never** stored in Git. Supply them at runtime:

```bash
# Option A: inline extra-var (demo only)
-e postgres_password=parcel_pass

# Option B: Ansible Vault (recommended)
ansible-vault encrypt_string 'parcel_pass' --name postgres_password > vault_pw.yml
ansible-playbook -i inventory.ini deploy.yml --extra-vars @vault_pw.yml --ask-vault-pass
```

---

## Honest Validation Notes

The following was validated on **WSL Ubuntu** (what the viva will demonstrate):

- ✅ YAML syntax: `ansible-playbook --syntax-check` passes on all 4 playbooks
- ✅ Role structure follows Ansible Galaxy conventions
- ✅ Templates use Jinja2 variable injection with no hardcoded secrets
- ✅ `no_log: true` on the .env render task prevents password logging
- ✅ Health check uses `ansible.builtin.uri` with `until` + `retries`

The following could only be validated with a running WSL+Docker environment:

- Actual `apt install` execution (requires WSL with internet access)
- `community.general.ufw` module (requires ufw to be installed)
- Full `docker compose up` via WSL (requires Docker Desktop integration)
- Idempotency second-run `changed=0` (requires first run to complete)

These are demonstrated **live** during the viva using the WSL terminal.
