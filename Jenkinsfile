pipeline {
    agent any

    environment {
        POSTGRES_DB = 'ecampus'
        POSTGRES_USER = 'ecampus'
        POSTGRES_PASSWORD = 'ecampus_test_password'
        JWT_SECRET = 'ci-only-jwt-secret-for-github-actions-testing'
        SPRING_PROFILES_ACTIVE = 'postgres'
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/ecampus'
        SPRING_DATASOURCE_USERNAME = 'ecampus'
        SPRING_DATASOURCE_PASSWORD = 'ecampus_test_password'
    }

    options {
        timestamps()
    }

    stages {
        stage('Build and Test') {
            steps {
                sh 'docker compose -p "ecampus-ci-${BUILD_NUMBER}" up -d --wait db && chmod +x mvnw && ./mvnw -B clean verify'
            }
        }

        stage('Build Container') {
            steps {
                sh 'docker build --tag "ecampus:${BUILD_NUMBER}" .'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true, fingerprint: true
            sh 'docker compose -p "ecampus-ci-${BUILD_NUMBER}" down -v --remove-orphans || true'
        }
    }
}
