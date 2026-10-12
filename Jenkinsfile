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
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean test'
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
                echo "Deployed parcel-tracking-portal.war to ${params.DEPLOY_ENV} at ${params.TOMCAT_HOME}"
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed: build, package and Tomcat deployment succeeded.'
        }
        failure {
            echo 'Pipeline failed. Review the stage log before redeploying.'
        }
    }
}
