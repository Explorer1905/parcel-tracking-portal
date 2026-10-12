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

        stage('Selenium Tests') {
            steps {
                bat 'mvn -B test -Dselenium.baseUrl=http://localhost:8081/parcel-tracking-portal'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
                }
                bat '''
                    docker build -t "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%" .
                    docker tag "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%" "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest"
                    docker image inspect "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                '''
            }
        }

        stage('Publish to Local Registry') {
            steps {
                bat '''
                    docker inspect parcel-registry >nul 2>&1
                    if errorlevel 1 (
                        docker run -d --restart=always --name parcel-registry -p 5000:5000 registry:2
                    ) else (
                        docker start parcel-registry >nul 2>&1
                    )
                    docker push "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    docker push "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest"
                    curl.exe --fail http://%REGISTRY_HOST%/v2/%IMAGE_REPOSITORY%/tags/list
                '''
            }
        }

        stage('Deploy Fresh Container') {
            steps {
                bat '''
                    docker rm -f "%CONTAINER_NAME%" >nul 2>&1
                    docker pull "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    docker run -d --name "%CONTAINER_NAME%" -p "%CONTAINER_PORT%:8080" "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:%IMAGE_TAG%"
                    timeout /t 10 /nobreak >nul
                    docker ps --filter "name=%CONTAINER_NAME%"
                    docker logs "%CONTAINER_NAME%"
                    curl.exe --fail http://localhost:%CONTAINER_PORT%/parcel-tracking-portal/health
                '''
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
                echo "Deployed parcel-tracking-portal.war to ${params.DEPLOY_ENV} at ${params.TOMCAT_HOME}"
            }
        }
    }

    post {
        always {
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
