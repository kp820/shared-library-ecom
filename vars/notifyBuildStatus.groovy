def call(Map config) {
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def pipelineStatus = currentBuild.result ?: 'SUCCESS' // Default to 'SUCCESS' if no result is set
    def subject = "${jobName} - Build ${buildNumber} - ${pipelineStatus}"

    def body = pipelineStatus == 'SUCCESS' 
        ? "Build Successful: ${jobName} - Build ${buildNumber}\n\nBuild completed successfully."
        : "Build Failed: ${jobName} - Build ${buildNumber}\n\nPlease check the logs for details."

    emailext(
        subject: subject,
        body: body,
        to: config.emailRecipients,
        from: 'divyavundavalli777@gmail.com',
        replyTo: 'divyavundavalli777@gmail.com',
        mimeType: 'text/plain'
    )
}
