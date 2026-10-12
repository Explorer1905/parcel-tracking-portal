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

## Branch strategy

See [CONTRIBUTING.md](CONTRIBUTING.md). Each task uses a `task/NN-short-name` branch merged into `develop`.

## Author

Shravani Chavan, CMPN A