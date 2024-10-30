def call() {
    script {
        try {
            echo "Building Docker image..."
            sh """
                docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
                docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
            """
        } catch (Exception e) {
            echo "Error during Docker build: ${e.getMessage()}"
            throw e
        }
    }
}
