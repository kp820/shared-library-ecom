def call() {
    sh '''
        # Remove existing Docker credentials
        rm -rf ~/.docker/config.json || true
        security delete-generic-password -s "Docker Credentials" || true
        
        # Verify Docker is running
        docker info
        
        # Login to Docker Hub
        echo $DOCKER_REGISTRY_CREDENTIALS_PSW | docker login -u $DOCKER_REGISTRY_CREDENTIALS_USR --password-stdin || {
            echo "Docker login failed, retrying after cleanup..."
            docker logout
            rm -rf ~/.docker/config.json || true
            echo $DOCKER_REGISTRY_CREDENTIALS_PSW | docker login -u $DOCKER_REGISTRY_CREDENTIALS_USR --password-stdin
        }
    '''
}

