def call(Map config = [:]) {
    def dockerImage = config.dockerImage ?: ''
    def dockerCredentialsId = config.dockerCredentialsId ?: ''
    def gitUrl = config.gitUrl ?: ''
    def gitBranch = config.gitBranch ?: 'main'

    pipeline {
        agent any

        environment {
            DOCKER_IMAGE = "${dockerImage}"
            DOCKER_REGISTRY_CREDENTIALS = credentials("${dockerCredentialsId}")
        }

        stages {
            stage('Checkout') {
                steps {
                    git branch: "${gitBranch}",
                        url: "${gitUrl}"
                }
            }

            stage('Docker Login updated in local') {
                steps {
                    dockerLogin()
                }
            }

            stage('Docker Build') {
                steps {
                    dockerBuild()
                }
            }

            stage('Docker Push') {
                steps {
                    dockerPush()
                }
            }
        }

        post {
        always {
            notifyBuildStatus()
            cleanup()
        }
    }
    }
}
