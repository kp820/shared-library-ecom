def call(String status) {
    def subject = status == 'SUCCESS' ? "Pipeline Successful: ${currentBuild.fullDisplayName}" : "Pipeline Failed: ${currentBuild.fullDisplayName}"
    def body = status == 'SUCCESS' ? 
        """
        Pipeline completed successfully!
        Build URL: ${env.BUILD_URL}
        Coverage Report: ${env.BUILD_URL}Coverage_Report/
        """ :
        """
        Pipeline failed!
        Build URL: ${env.BUILD_URL}
        Console Output: ${env.BUILD_URL}console
        """
    
    emailext (
        subject: subject,
        body: body,
        recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}