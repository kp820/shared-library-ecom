def call() {
    try {
        sh """
            . ${env.VENV_NAME}/bin/activate
            mkdir -p coverage
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