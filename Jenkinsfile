pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean verify'
            }
        }
        stage('Package') {
            steps {
                sh './mvnw -B clean package -Pproduction -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t ifs25009-pbo2026-springboot-cashflow .'
            }
        }
    }
}
