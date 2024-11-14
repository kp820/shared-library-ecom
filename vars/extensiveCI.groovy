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
            
        stage('Setup Python Environment') {
            steps {
                script {
                    sh """
                        ${PYTHON_VERSION} -m venv ${VENV_NAME}
                        . ${VENV_NAME}/bin/activate
                        pip install --upgrade pip
                        # Install with specific versions to avoid conflicts
                        pip install Flask==2.0.1 Werkzeug==2.0.1
                        pip install pytest==7.4.0 pytest-cov==4.1.0
                        pip install -r requirements.txt
                    """
                }
            }
        }
            
        stage('Code Quality & Security') {
            parallel {
                stage('Lint Check') {
                    steps {
                        script {
                            sh """
                                . ${VENV_NAME}/bin/activate
                                pip install flake8 pylint
                                # Run flake8
                                flake8 . --max-line-length=120 --exclude=${VENV_NAME} || true
                                # Run pylint
                                pylint --exit-zero *.py || true
                            """
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        script {
                            try {
                                sh """
                                    . ${VENV_NAME}/bin/activate
                                    # Install security scanning tools
                                    pip install bandit safety detect-secrets
                                    
                                    # Create reports directory
                                    mkdir -p security-reports
                                    
                                    # Run Bandit security scan
                                    bandit -r . -f json -o security-reports/bandit-results.json -ll || true
                                    
                                    # Check dependencies for known security vulnerabilities
                                    safety check --output json > security-reports/safety-results.json || true
                                    
                                    # Run detect-secrets for credential scanning
                                    detect-secrets scan . > security-reports/secrets-scan.json || true
                                """
                            } catch (Exception e) {
                                echo "Security scan failed: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
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
                script {
                    try {
                        sh """
                            . ${VENV_NAME}/bin/activate
                            # Create coverage directory
                            mkdir -p coverage
                            
                            # Run tests with coverage
                            python -m pytest -v \
                                --cov=. \
                                --cov-report=xml:coverage/coverage.xml \
                                --cov-report=html:coverage/htmlcov \
                                test.py
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        echo "Tests failed: ${e.getMessage()}"
                    }
                }
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
        post {
        always {
            notifyBuildStatus()
            // script {
            //     def jobName = env.JOB_NAME
            //     def buildNumber = env.BUILD_NUMBER
            //     def pipelineStatus = currentBuild.result ?: 'UNKNOWN'
            //     def bannerColor = pipelineStatus.toUpperCase() == 'SUCCESS' ? 'green' : 'red'
                
            //     def body = """
            //     <html>
            //         <body>
            //             <div style="border: 4px solid ${bannerColor}; padding: 10px;">
            //                 <h2>${jobName} - Build ${buildNumber}</h2>
            //                 <div style="background-color: ${bannerColor}; padding: 10px;">
            //                     <h3 style="color: white;">Pipeline Status: ${pipelineStatus}</h3>
            //                 </div>
            //                 <p>Check the <a href="${env.BUILD_URL}">Build Logs</a> for more details.</p>
            //             </div>
            //         </body>
            //     </html>
            //     """
                
            //     emailext(
            //         subject: "${jobName} - Build ${buildNumber} - ${pipelineStatus}",
            //         body: body,
            //         to: 'divyavundavalli777@gmail.com',
            //         from: 'divyavundavalli777@gmail.com',
            //         replyTo: 'divyavundavalli777@gmail.com',
            //         mimeType: 'text/html'
            //     )
            // }
            cleanup()
        }
    }
    }
}
