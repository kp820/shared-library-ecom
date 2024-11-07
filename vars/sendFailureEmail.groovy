def call(Map config) {
    emailext (
        subject: "${BUILD_TYPE} Build Failed: ${currentBuild.fullDisplayName}",
        body: """
            ${BUILD_TYPE} Build failed!
            
            Error Details:
            - Build Number: ${BUILD_NUMBER}
            - Build URL: ${env.BUILD_URL}
            - Console Output: ${env.BUILD_URL}console
            
            Please check the build logs for more details.
        """,
        to: config.emailRecipients,
        recipientProviders: [[$class: 'DevelopersRecipientProvider']],
        attachLog: true
    )
}
