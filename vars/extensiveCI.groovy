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
            emailext (
                subject: "Pipeline Successful: ${currentBuild.fullDisplayName}",
                body: """
                    Pipeline completed successfully!
                    Build URL: ${env.BUILD_URL}
                    Coverage Report: ${env.BUILD_URL}Coverage_Report/
                """,
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        failure {
            echo "Pipeline failed! Check the logs for details."
            emailext (
                subject: "Pipeline Failed: ${currentBuild.fullDisplayName}",
                body: """
                    Pipeline failed!
                    Build URL: ${env.BUILD_URL}
                    Console Output: ${env.BUILD_URL}console
                """,
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        always {
            cleanup()
        }
    }
        
         // dont dist
}
}
