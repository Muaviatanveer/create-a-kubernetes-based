```groovy
// Define the Jenkins pipeline script in Groovy

pipeline {
    agent any // This can be customized to specify a specific agent or node

    environment {
        // Environment variables that will be available throughout the pipeline
        APP_NAME = 'my-application'
        DOCKER_IMAGE = "${APP_NAME}:latest"
        TEST_REPORT_PATH = 'target/surefire-reports'
        DEPLOYMENT_ENV = 'production'
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from version control system (e.g., Git)
                git branch: 'main', url: 'https://github.com/myuser/myrepo.git'
            }
        }

        stage('Build') {
            steps {
                // Build the application
                sh './mvnw clean package -DskipTests=true' // Example for Maven project

                // Build Docker image
                script {
                    docker.build(DOCKER_IMAGE)
                }
            }
        }

        stage('Test') {
            steps {
                // Run tests
                sh './mvnw test'

                // Archive test reports
                junit allowEmptyResults: true, testResults: "${TEST_REPORT_PATH}/*.xml"
            }
        }

        stage('Code Quality Check') {
            steps {
                // Perform code quality checks (e.g., SonarQube analysis)
                sh './mvnw sonar:sonar'
            }
        }

        stage('Deploy') {
            when {
                expression { env.DEPLOYMENT_ENV == 'production' } // Conditional deployment
            }
            steps {
                script {
                    withDockerRegistry([credentialsId: 'docker-registry', url: '']) {
                        docker.image(DOCKER_IMAGE).push()
                    }

                    // Deploy to production environment (e.g., Kubernetes)
                    sh 'kubectl apply -f kubernetes/production/deployment.yaml'
                }
            }
        }
    }

    post {
        always {
            // Always run these steps regardless of the pipeline outcome
            cleanWs() // Clean up workspace

            // Archive artifacts for later use
            archiveArtifacts artifacts: '**/*', fingerprint: true
        }

        success {
            // Notify on successful build
            echo 'Build succeeded!'
        }

        failure {
            // Notify on failed build
            echo 'Build failed!'
        }
    }
}
```

This Jenkins pipeline script is designed to handle a complete automated build, test, and deployment workflow. It includes stages for checking out code, building the application, running tests, performing code quality checks, and deploying to a production environment. The script also handles error handling and cleaning up the workspace after execution.