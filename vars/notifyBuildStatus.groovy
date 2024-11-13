def notifyBuildStatus(String status) {
    def jobName = env.JOB_NAME
    def buildNumber = env.BUILD_NUMBER
    def subject = "${jobName} - Build ${buildNumber} - ${status}"
    
    def body = status == 'SUCCESS' 
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
