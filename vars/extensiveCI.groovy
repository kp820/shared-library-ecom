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
            VENV_NAME = "venv"
            PYTHON_VERSION = "python3"
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
            success {
                echo "Pipeline executed successfully!"
            }
            failure {
                echo "Pipeline failed! Check the logs for details."
                echo "rough content - stash ex"
                echo "done"
            }
            always {
                cleanup()
            }
        }
    }
}
