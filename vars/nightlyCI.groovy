def call(Map config = [:]) {
    def defaultConfig = [
        dockerImage: 'default-image',
        dockerRegistry: 'docker.io',
        emailRecipients: 'divyajyothivundavalli@gmail.com',
        gitUrl: '',
        gitBranch: 'main',
        triggers: [:],
        buildType: 'regular' // Can be 'regular' or 'nightly'
    ]
    
    config = defaultConfig + config
    
    pipeline {
        agent any
        
        environment {
            DOCKER_IMAGE = "${config.dockerImage}"
            DOCKER_REGISTRY_CREDENTIALS = credentials('docker')
            BUILD_TYPE = "${config.buildType}"
            NIGHTLY_TAG = "nightly-${BUILD_TIMESTAMP}"
        }
        
        stages {
            stage('Checkout') {
                steps {
                    git branch: config.gitBranch,
                        url: config.gitUrl
                }
            }
            
            stage('Docker Login') {
                steps {
                    dockerLogin()
                }
            }
            
            stage('Docker Build') {
                steps {
                    dockerBuild(config)
                }
            }
            
            stage('Docker Push') {
                steps {
                    dockerPush(config)
                }
            }
        }
        
        post {
            success {
                sendSuccessEmail(config)
            }
            failure {
                sendFailureEmail(config)
            }
            always {
                cleanup(config)
            }
        }
    }
}
