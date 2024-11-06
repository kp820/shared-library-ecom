def call(Map config = [:]) {
    def defaultConfig = [
        pythonVersion: 'python3',
        dockerImage: 'divyavundavalli/ecom_projectexci',
        gitUrl: 'https://github.com/DivyaJyothiVundavalli/Ecom_Project_space.git',
        gitBranch: 'main',
        venvName: 'venv'
    ]
    
    config = defaultConfig + config
    
    pipeline {
        agent any
        
        environment {
            DOCKER_IMAGE = config.dockerImage
            DOCKER_REGISTRY_CREDENTIALS = credentials('docker')
            VENV_NAME = config.venvName
            PYTHON_VERSION = config.pythonVersion
        }
        
        stages {
            stage('Cleanup Workspace') {
                steps {
                    cleanWs()
                }
            }
            
            stage('Checkout') {
                steps {
                    git branch: config.gitBranch,
                        url: config.gitUrl
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
            
            stage('Security Scan Docker Image') {
                steps {
                    dockerSecurityScan()
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'security-reports/trivy-results.txt', allowEmptyArchive: true
                    }
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