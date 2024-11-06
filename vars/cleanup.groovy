def call() {
    sh '''
        docker logout || true
        docker system prune -f || true
        rm -rf ~/.docker/config.json || true
    '''
    
    sh """
        docker rmi ${env.DOCKER_IMAGE}:${BUILD_NUMBER} || true
        docker rmi ${env.DOCKER_IMAGE}:latest || true
        docker image prune -f
    """
    
    cleanWs(
        cleanWhenNotBuilt: false,
        deleteDirs: true,
        disableDeferredWipeout: true,
        cleanWhenSuccess: true,
        cleanWhenUnstable: true
    )
}