def call() {
    sh '''
        # Cleanup
        docker logout || true
        rm -rf ~/.docker/config.json || true
        security delete-generic-password -s "Docker Credentials" || true
    '''
    
    sh """
        docker rmi ${DOCKER_IMAGE}:${BUILD_NUMBER} || true
        docker rmi ${DOCKER_IMAGE}:latest || true
    """
}
