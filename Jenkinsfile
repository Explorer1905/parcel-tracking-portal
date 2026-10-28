pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['production', 'staging'],
               description: 'Stack to deploy: production = port 8082, staging = port 8083')
        booleanParam(name: 'SEED_DEMO_DATA', defaultValue: true,
               description: 'Load the DEMO-xxxx parcels into the deployed database')
        string(name: 'REGISTRY_HOST', defaultValue: 'localhost:5000',
               description: 'Local Docker registry host and port')
        string(name: 'IMAGE_REPOSITORY', defaultValue: 'parcel-tracking-portal',
               description: 'Repository name in the Docker registry')
        string(name: 'DOCKER_EXE',
               defaultValue: 'C:\\Users\\Shravani\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe',
               description: 'Docker CLI executable on the Jenkins agent')
    }

    environment {
        DOCKER_EXE       = "${params.DOCKER_EXE}"
        REGISTRY_HOST    = "${params.REGISTRY_HOST}"
        IMAGE_REPOSITORY = "${params.IMAGE_REPOSITORY}"
        IMAGE_TAG        = "1.1.${env.BUILD_NUMBER}"
        IMAGE_REF        = "${params.REGISTRY_HOST}/${params.IMAGE_REPOSITORY}:1.1.${env.BUILD_NUMBER}"
        STACK_NAME       = "parcel-${params.DEPLOY_ENV}"
        PROD_APP_PORT    = "${params.DEPLOY_ENV == 'production' ? '8082' : '8083'}"
        PROD_DB_PORT     = "${params.DEPLOY_ENV == 'production' ? '5434' : '5435'}"
        SEED_DEMO        = "${params.SEED_DEMO_DATA}"
        TEST_PROJECT     = 'parcel-test'
        TEST_APP_PORT    = '8091'
        TEST_DB_PORT     = '5433'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Parameters') {
            steps {
                script {
                    if (!(params.DEPLOY_ENV in ['production', 'staging']) || params.SEED_DEMO_DATA == null) {
                        error "Parameters are out of date (DEPLOY_ENV='${params.DEPLOY_ENV}'). Normal on the first run after the Jenkinsfile changed: run Build with Parameters again."
                    }
                }
                bat '"%DOCKER_EXE%" compose version'
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

        stage('Build Docker Image') {
            steps {
                bat '''
                    "%DOCKER_EXE%" build -t "%IMAGE_REF%" . || exit /b 1
                    "%DOCKER_EXE%" tag "%IMAGE_REF%" "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest" || exit /b 1
                    "%DOCKER_EXE%" image inspect "%IMAGE_REF%" --format "image={{.Id}} size={{.Size}}"
                '''
            }
        }

        stage('Deploy Test Stack') {
            steps {
                bat '''
                    set "APP_IMAGE=%IMAGE_REF%"
                    set "APP_PORT=%TEST_APP_PORT%"
                    set "DB_PORT=%TEST_DB_PORT%"
                    set "DB_SEED_DEMO=false"
                    set "DB_CONTAINER=%TEST_PROJECT%-db"
                    set "APP_CONTAINER=%TEST_PROJECT%-app"
                    "%DOCKER_EXE%" compose -p %TEST_PROJECT% down -v --remove-orphans
                    "%DOCKER_EXE%" compose -p %TEST_PROJECT% up -d --no-build || exit /b 1
                    "%DOCKER_EXE%" compose -p %TEST_PROJECT% ps
                    curl.exe --fail --retry 40 --retry-delay 3 --retry-all-errors http://localhost:%TEST_APP_PORT%/parcel-tracking-portal/health
                '''
            }
        }

        stage('Selenium Tests') {
            steps {
                bat 'mvn -B test -Dselenium.baseUrl=http://localhost:%TEST_APP_PORT%/parcel-tracking-portal'
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
                    "%DOCKER_EXE%" push "%IMAGE_REF%" || exit /b 1
                    "%DOCKER_EXE%" push "%REGISTRY_HOST%/%IMAGE_REPOSITORY%:latest" || exit /b 1
                    curl.exe --fail http://%REGISTRY_HOST%/v2/%IMAGE_REPOSITORY%/tags/list
                '''
            }
        }

        stage('Deploy Stack') {
            steps {
                bat '''
                    "%DOCKER_EXE%" rm -f parcel-portal-cd >nul 2>&1
                    "%DOCKER_EXE%" pull "%IMAGE_REF%" || exit /b 1
                    set "APP_IMAGE=%IMAGE_REF%"
                    set "APP_PORT=%PROD_APP_PORT%"
                    set "DB_PORT=%PROD_DB_PORT%"
                    set "DB_SEED_DEMO=%SEED_DEMO%"
                    set "DB_CONTAINER=%STACK_NAME%-db"
                    set "APP_CONTAINER=%STACK_NAME%-app"
                    "%DOCKER_EXE%" compose -p %STACK_NAME% up -d --no-build || exit /b 1
                    "%DOCKER_EXE%" compose -p %STACK_NAME% ps
                    curl.exe --fail --retry 40 --retry-delay 3 --retry-all-errors http://localhost:%PROD_APP_PORT%/parcel-tracking-portal/health
                '''
            }
        }
    }

    post {
        always {
            bat '"%DOCKER_EXE%" compose -p %TEST_PROJECT% down -v --remove-orphans & exit /b 0'
            archiveArtifacts artifacts: 'target/selenium-failures/*.png', allowEmptyArchive: true
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
        }
        success {
            echo "Pipeline completed: test stack passed Selenium, image ${env.IMAGE_TAG} published, ${env.STACK_NAME} stack deployed."
        }
        failure {
            echo 'Pipeline failed. Nothing was published or deployed after the failing stage.'
        }
    }
}