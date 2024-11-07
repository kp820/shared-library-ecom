def call(Map config) {
    emailext (
        subject: "${BUILD_TYPE} Build Successful: ${currentBuild.fullDisplayName}",
        body: """
            ${BUILD_TYPE} Build completed successfully!
            
            Build Details:
            - Build Number: ${BUILD_NUMBER}
            - Docker Image Tags: 
              * ${config.dockerImage}:${BUILD_NUMBER}
              * ${config.dockerImage}:latest
              ${BUILD_TYPE == 'nightly' ? '* ' + config.dockerImage + ':' + NIGHTLY_TAG : ''}
            - Build URL: ${env.BUILD_URL}
            
            Changes since last build:
            ${currentBuild.changeSets.size() > 0 ? currentBuild.changeSets : 'No changes'}
        """,
        to: config.emailRecipients,
        recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}
