# Parcel Tracking Portal

A web application for recording, searching and monitoring parcel status, built to demonstrate a complete CI/CD pipeline.

## Tech stack

Java 17, Servlets/JSP (Jakarta), Maven, Apache Tomcat 10.1, Git/GitHub, Jenkins, Selenium, Docker and Ansible.

## Build

```text
mvn clean package
```

The generated WAR file is `target/parcel-tracking-portal.war`.

## Run

Copy `target/parcel-tracking-portal.war` into Tomcat's `webapps` folder, start Tomcat and open:

`http://localhost:8081/parcel-tracking-portal/`

The application skeleton also provides a health check at `/health`.

## Docker

Build the WAR and Docker image from the repository root:

```text
mvn -B clean package -DskipTests
docker build -t parcel-tracking-portal:1.0.0 .
docker run -d --name parcel-portal -p 8082:8080 parcel-tracking-portal:1.0.0
```

The container listens on port 8080 internally and is exposed on port 8082 on
the host. Verify `http://localhost:8082/parcel-tracking-portal/health`, then
inspect and manage its lifecycle:

```text
docker image inspect parcel-tracking-portal:1.0.0
docker logs parcel-portal
docker stop parcel-portal
docker start parcel-portal
docker rm -f parcel-portal
```

## Jenkins pipeline deployment

The root [`Jenkinsfile`](Jenkinsfile) defines Checkout, Build, Selenium Tests,
Package, Docker build, registry publish, fresh container deployment and Deploy
to Tomcat stages. The pipeline accepts `DEPLOY_ENV`, `TOMCAT_HOME`,
`REGISTRY_HOST`, `IMAGE_REPOSITORY`, `CONTAINER_NAME` and `CONTAINER_PORT`
parameters. Each successful build creates a versioned image tag
`1.0.<Jenkins build number>`, pushes it to the local registry, pulls that exact
tag and starts a fresh container. It also publishes Maven Surefire XML results
and archives `target/parcel-tracking-portal.war`.

Start a local registry once before running the Jenkins job:

```text
docker run -d --restart=always --name parcel-registry -p 5000:5000 registry:2
```

Use `localhost:5000` for `REGISTRY_HOST`, `parcel-tracking-portal` for
`IMAGE_REPOSITORY`, `parcel-portal-cd` for `CONTAINER_NAME` and `8082` for
`CONTAINER_PORT`. The pipeline creates the registry if it is not already
present. After a successful run, verify:

```text
docker image ls localhost:5000/parcel-tracking-portal
docker ps --filter name=parcel-portal-cd
curl.exe http://localhost:8082/parcel-tracking-portal/health
```

For the local Windows setup, use `local-tomcat` and the Tomcat directory
configured on the Jenkins agent, then verify:

`http://localhost:8081/parcel-tracking-portal/`

## Selenium tests

The local Selenium suite covers parcel registration, dashboard search, event
entry and the alerts page. Start Tomcat, ensure Chrome is installed, then run:

```text
mvn test -Dselenium.baseUrl=http://localhost:8081/parcel-tracking-portal
```

Chrome runs headlessly by default. Set `-Dselenium.headless=false` to watch the
browser. Failed tests save PNG screenshots in `target/selenium-failures`, and
the Maven Surefire report is written under `target/surefire-reports`.

## Ansible configuration management

Task 13 defines the Ubuntu/Debian target prerequisites in
[`ansible/configuration-spec.md`](ansible/configuration-spec.md). The
playbook installs Java, Maven, Docker and utilities, creates the least-
privileged `parcel` service account, creates application/configuration/data
directories, writes the managed configuration file, starts Docker at boot and
opens the application, Tomcat and registry ports.

Run it from Ubuntu/WSL:

```text
cd ansible
ansible-playbook -i inventory.ini site.yml --check --diff
ansible-playbook -i inventory.ini site.yml
ansible-playbook -i inventory.ini site.yml
```

The final run should be idempotent and report no changes. Save the first
execution output as `docs/reports/task-13-ansible-first-execution.log`.

On WSL with Docker Desktop integration, Ubuntu may not provide a native
`docker.service`; the playbook detects this and leaves the already-running
Docker Desktop engine in control. On a normal Ubuntu server, it enables and
starts the native Docker service.

## Automated provisioning and rollback

Task 14 extends the prerequisite playbook with
[`ansible/deploy.yml`](ansible/deploy.yml) and
[`ansible/rollback.yml`](ansible/rollback.yml). The deployment playbook pulls
an exact image tag, replaces the container, waits for the health endpoint and
records the active release. The rollback playbook restores a known stable tag
and performs the same health check. See
[`ansible/reliability-runbook.md`](ansible/reliability-runbook.md) for the
first execution, idempotence and recovery commands.

## Branch strategy

See [CONTRIBUTING.md](CONTRIBUTING.md). Each task uses a `task/NN-short-name` branch merged into `develop`.

## Author

Shravani Chavan, CMPN A