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
            
            stage('Cleanup Workspace') {
              steps {
                cleanWs()
              }
          }
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

        // post {
        //     success {
        //         echo "Pipeline executed successfully!"
        //     }
        //     failure {
        //         echo "Pipeline failed! Check the logs for details."
        //         echo "rough content - stash ex"
        //         echo "done"
        //     }
        //     always {
        //         cleanup()
        //     }
        // }
        post {
        always {
            script {
                def jobName = env.JOB_NAME
                def buildNumber = env.BUILD_NUMBER
                def pipelineStatus = currentBuild.result ?: 'UNKNOWN'
                def bannerColor = pipelineStatus.toUpperCase() == 'SUCCESS' ? 'green' : 'red'
                
                def body = """
                <html>
                    <body>
                        <div style="border: 4px solid ${bannerColor}; padding: 10px;">
                            <h2>${jobName} - Build ${buildNumber}</h2>
                            <div style="background-color: ${bannerColor}; padding: 10px;">
                                <h3 style="color: white;">Pipeline Status: ${pipelineStatus}</h3>
                            </div>
                            <p>Check the <a href="${env.BUILD_URL}">Build Logs</a> for more details.</p>
                        </div>
                    </body>
                </html>
                """
                
                emailext(
                    subject: "${jobName} - Build ${buildNumber} - ${pipelineStatus}",
                    body: body,
                    to: 'divyavundavalli777@gmail.com',
                    from: 'divyavundavalli777@gmail.com',
                    replyTo: 'divyavundavalli777@gmail.com',
                    mimeType: 'text/html'
                )
            }
        }
    }
}
    }
}
