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

## Jenkins pipeline deployment

The root [`Jenkinsfile`](Jenkinsfile) defines Checkout, Build, Package and Deploy
stages. The pipeline accepts `DEPLOY_ENV` and `TOMCAT_HOME` parameters, archives
`target/parcel-tracking-portal.war`, and copies it to the selected Tomcat
installation. For the local Windows setup, use `local-tomcat` and the Tomcat
directory configured on the Jenkins agent, then verify:

`http://localhost:8081/parcel-tracking-portal/`

## Branch strategy

See [CONTRIBUTING.md](CONTRIBUTING.md). Each task uses a `task/NN-short-name` branch merged into `develop`.

## Author

Shravani Chavan, CMPN A