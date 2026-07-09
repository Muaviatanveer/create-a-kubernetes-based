```groovy
import jenkins.model.*
import hudson.model.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import java.util.logging.Logger

// Define the logger for this script
private static final Logger LOGGER = Logger.getLogger("JenkinsMonitoring")

/**
 * This class is responsible for monitoring Jenkins jobs and logging their status.
 */
class JenkinsMonitor {
    private Jenkins jenkinsInstance

    /**
     * Constructor to initialize the Jenkins instance.
     */
    JenkinsMonitor() {
        this.jenkinsInstance = Jenkins.getInstance()
    }

    /**
     * Method to monitor all Jenkins jobs and log their statuses.
     */
    void monitorJobs() {
        try {
            // Get all jobs in Jenkins
            def jobs = jenkinsInstance.getAllItems(Job.class)
            
            for (Job job : jobs) {
                if (job instanceof WorkflowJob) {
                    WorkflowJob workflowJob = (WorkflowJob) job
                    LOGGER.info("Monitoring job: ${workflowJob.getFullName()}")
                    
                    // Get the last build of the job
                    def lastBuild = workflowJob.getLastBuild()
                    if (lastBuild != null) {
                        LOGGER.info("Last build number: ${lastBuild.getNumber()}")
                        
                        // Log the build status
                        switch (lastBuild.getResult()) {
                            case Result.SUCCESS:
                                LOGGER.info("Build successful")
                                break
                            case Result.FAILURE:
                                LOGGER.warning("Build failed")
                                break
                            case Result.ABORTED:
                                LOGGER.warning("Build aborted")
                                break
                            default:
                                LOGGER.info("Build status: ${lastBuild.getResult()}")
                        }
                    } else {
                        LOGGER.warning("No builds found for job: ${workflowJob.getFullName()}")
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error monitoring Jenkins jobs: ${e.message}")
            e.printStackTrace()
        }
    }
}

// Main execution block
if (__FILE__.equals("$JENKINS_HOME/ci/jenkins/monitoring.groovy")) {
    try {
        // Create an instance of the monitor and start monitoring
        def monitor = new JenkinsMonitor()
        monitor.monitorJobs()
    } catch (Exception e) {
        LOGGER.severe("Error in main execution block: ${e.message}")
        e.printStackTrace()
    }
}
```

This Groovy script is designed to monitor all Jenkins jobs, specifically focusing on `WorkflowJob` types. It logs the status of each job, including whether it succeeded, failed, or was aborted. The script uses Java's built-in logging framework to log messages at different levels (info, warning, severe). Error handling is included to catch and log any exceptions that occur during the monitoring process.