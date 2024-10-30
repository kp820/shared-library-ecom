def call() {
    script {
        try {
            echo "Pushing Docker image to registry..."
            sh """
                docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                docker push ${DOCKER_IMAGE}:latest
            """
        } catch (Exception e) {
            echo "Error during Docker push: ${e.getMessage()}"
            throw e
        }
    }
}
