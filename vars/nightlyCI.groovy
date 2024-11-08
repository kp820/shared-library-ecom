def call(Map config = [:]) {
  def defaultConfig = [
    pythonVersion: 'python3',
    dockerImage: 'divyavundavalli/ecom_projectexci',
    gitUrl: 'https://github.com/DivyaJyothiVundavalli/Ecom_Project_space.git',
    gitBranch: 'main',
    emailRecipients: 'divyajyothivundavalli@gmail.com',
    triggers: [:],
    buildType: 'nightly'
  ]

  config = defaultConfig + config

  pipeline {
    agent any

    environment {
      PYTHON_VERSION = "${config.pythonVersion}"
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
