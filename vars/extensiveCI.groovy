// vars/pipelineJob.groovy
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
            VENV_NAME = 'venv'
            PYTHON_VERSION = 'python3'
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
            
            stage('Setup Python Environment') {
                steps {
                    pythonSetup()
                }
            }
            
            stage('Code Quality & Security') {
                parallel {
                    stage('Lint Check') {
                        steps {
                            lintCheck()
                        }
                    }
                    
                    stage('Security Scan') {
                        steps {
                            securityScan()
                        }
                        post {
                            always {
                                archiveArtifacts artifacts: 'security-reports/**/*', allowEmptyArchive: true
                            }
                        }
                    }
                }
            }
            
            stage('Run Tests') {
                steps {
                    runTests()
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'coverage/**/*', allowEmptyArchive: true
                        publishHTML([
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'coverage/htmlcov',
                            reportFiles: 'index.html',
                            reportName: 'Coverage Report',
                            reportTitles: 'Coverage Report'
                        ])
                    }
                }
            }

            stage('Docker Login') {
                steps {
                    dockerLogin()
                }
            }

            stage('Build Docker Image') {
                steps {
                    dockerBuild()
                }
            }
            
            // stage('Security Scan Docker Image') {
            //     steps {
            //         // dockerSecurityScan()
            //         echo 'scan'
            //     }
            //     post {
            //         always {
            //             archiveArtifacts artifacts: 'security-reports/trivy-results.txt', allowEmptyArchive: true
            //         }
            //     }
            // }
            
            stage('Docker Push') {
                steps {
                    dockerPush()
                }
            }
        }
        
        post {
            success {
                script {
                    notifyBuildStatus('SUCCESS')
                }
            }
            failure {
                script {
                    notifyBuildStatus('FAILURE')
                }
            }
            always {
                script {
                    cleanup()
                }
            }
        }
    }
}
