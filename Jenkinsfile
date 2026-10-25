pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['local-tomcat', 'staging-tomcat'],
            description: 'Deployment environment for the WAR artifact'
        )
        string(
            name: 'TOMCAT_HOME',
            defaultValue: 'C:\\Users\\Shravani\\Downloads\\apache-tomcat-10.1.60-windows-x86\\apache-tomcat-10.1.60',
            description: 'Tomcat installation directory on the Jenkins agent'
        )
        string(
            name: 'REGISTRY_HOST',
            defaultValue: 'localhost:5000',
            description: 'Local Docker registry host and port'
        )
        string(
            name: 'IMAGE_REPOSITORY',
            defaultValue: 'parcel-tracking-portal',
            description: 'Repository name in the Docker registry'
        )
        string(
            name: 'CONTAINER_NAME',
            defaultValue: 'parcel-portal-cd',
            description: 'Container name for the fresh deployment'
        )
        string(
            name: 'CONTAINER_PORT',
            defaultValue: '8082',
            description: 'Host port mapped to Tomcat port 8080'
        )
        string(
            name: 'DOCKER_EXE',
            defaultValue: 'C:\\Users\\Shravani\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe',
            description: 'Docker CLI executable on the Jenkins agent'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean compile'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                bat '''
                    if not exist "%TOMCAT_HOME%\\webapps" (
                        echo Tomcat webapps directory was not found: %TOMCAT_HOME%\\webapps
                        exit /b 1
                    )
                    copy /Y "target\\parcel-tracking-portal.war" "%TOMCAT_HOME%\\webapps\\parcel-tracking-portal.war"
                '''
                sleep time: 20, unit: 'SECONDS'
                bat 'curl.exe --fail --retry 30 --retry-delay 2 --retry-all-errors http://localhost:8081/parcel-tracking-portal/health'
                echo "Deployed parcel-tracking-portal.war to ${params.DEPLOY_ENV} at ${params.TOMCAT_HOME}"
            }
        }

        stage('Selenium Tests') {
            steps {
                bat 'mvn -B test -Dselenium.baseUrl=http://localhost:8081/parcel-tracking-portal'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.REGISTRY_HOST = params.REGISTRY_HOST?.trim()
                    env.IMAGE_REPOSITORY = params.IMAGE_REPOSITORY?.trim()
                    env.CONTAINER_NAME = params.CONTAINER_NAME?.trim()
                    env.CONTAINER_PORT = params.CONTAINER_PORT?.trim()
                    env.DOCKER_EXE = params.DOCKER_EXE?.trim()
                    env.IMAGE_TAG = "1.1.${env.BUILD_NUMBER}"
                    if (!env.REGISTRY_HOST || !env.IMAGE_REPOSITORY || !env.CONTAINER_NAME ||
                        !env.CONTAINER_PORT || !env.DOCKER_EXE) {
                        error 'REGISTRY_HOST, IMAGE_REPOSITORY, CONTAINER_NAME, CONTAINER_PORT and DOCKER_EXE are required.'
                    }
                }
                bat '''
                    "%DOCKER_EXE%" build -t "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%" .
                    "%DOCKER_EXE%" tag "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%" "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest"
                    "%DOCKER_EXE%" image inspect "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                '''
            }
        }

        stage('Publish to Local Registry') {
            steps {
                bat '''
                    "%DOCKER_EXE%" inspect parcel-registry >nul 2>&1
                    if errorlevel 1 (
                        "%DOCKER_EXE%" run -d --restart=always --name parcel-registry -p 5000:5000 registry:2
                    ) else (
                        "%DOCKER_EXE%" start parcel-registry >nul 2>&1
                    )
                    "%DOCKER_EXE%" push "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    "%DOCKER_EXE%" push "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest"
                    curl.exe --fail http://%REGISTRY_HOST%/v2/%IMAGE_REPOSITORY%/tags/list
                '''
            }
        }

        stage('Deploy Fresh Container') {
            steps {
                bat '''
                    "%DOCKER_EXE%" rm -f "%CONTAINER_NAME%" >nul 2>&1
                    "%DOCKER_EXE%" pull "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    "%DOCKER_EXE%" run -d --name "%CONTAINER_NAME%" -p "%CONTAINER_PORT%:8080" "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    "%DOCKER_EXE%" ps --filter "name=%CONTAINER_NAME%"
                    "%DOCKER_EXE%" logs "%CONTAINER_NAME%"
                    curl.exe --fail --retry 30 --retry-delay 2 --retry-all-errors http://localhost:%CONTAINER_PORT%/parcel-tracking-portal/health
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/selenium-failures/*.png', allowEmptyArchive: true
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
        }
        success {
            echo 'Pipeline completed: tests, Docker build, registry publish and fresh container deployment succeeded.'
        }
        failure {
            echo 'Pipeline failed. Review the stage log before redeploying.'
        }
    }
}