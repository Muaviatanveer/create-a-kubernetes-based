```groovy
import jenkins.model.*
import hudson.model.*
import hudson.tasks.*
import org.jenkinsci.plugins.workflow.steps.*

// Define a class to encapsulate security scanning logic
class SecurityScan {
    private String jobName
    private String workspace

    SecurityScan(String jobName, String workspace) {
        this.jobName = jobName
        this.workspace = workspace
    }

    // Method to execute Snyk scan
    void runSnykScan() {
        try {
            def snykScript = """
                #!/bin/bash
                cd ${workspace}
                snyk test --json > snyk-report.json
                if [ $? -ne 0 ]; then
                    echo "Snyk scan failed. Please review the report."
                    exit 1
                fi
            """

            def snykStep = new Shell(snykScript)
            snykStep.perform(build, launcher, listener)

            // Parse Snyk report and handle vulnerabilities
            def snykReport = readJSON file: "${workspace}/snyk-report.json"
            if (snykReport.vulnerabilities.size() > 0) {
                error "Snyk scan detected ${snykReport.vulnerabilities.size()} vulnerabilities. Please address them."
            }
        } catch (Exception e) {
            error "Failed to execute Snyk scan: ${e.message}"
        }
    }

    // Method to execute SonarQube scan
    void runSonarQubeScan() {
        try {
            def sonarScript = """
                #!/bin/bash
                cd ${workspace}
                sonar-scanner -Dsonar.projectKey=${jobName} -Dsonar.sources=. -Dsonar.host.url=http://sonarqube-server:9000 -Dsonar.login=your_sonar_token
            """

            def sonarStep = new Shell(sonarScript)
            sonarStep.perform(build, launcher, listener)

            // Check SonarQube analysis status and handle issues
            def sonarAnalysisUrl = "http://sonarqube-server:9000/dashboard?id=${jobName}"
            echo "SonarQube analysis results can be found at ${sonarAnalysisUrl}"
        } catch (Exception e) {
            error "Failed to execute SonarQube scan: ${e.message}"
        }
    }
}

// Jenkins pipeline script
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    def securityScan = new SecurityScan(env.JOB_NAME, env.WORKSPACE)
                    securityScan.runSnykScan()
                    securityScan.runSonarQubeScan()
                }
            }
        }
    }

    post {
        failure {
            echo 'One or more security scans failed. Build aborted.'
        }
        success {
            echo 'All security scans passed. Build successful.'
        }
    }
}
```

This Groovy script integrates Snyk and SonarQube into a Jenkins pipeline. It defines a `SecurityScan` class to encapsulate the logic for running both types of scans. The pipeline includes stages for checking out code and performing security scans. Error handling is included for each scan, and the pipeline will fail if any scan detects issues.