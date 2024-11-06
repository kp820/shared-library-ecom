def call() {
    try {
        sh """
            . ${env.VENV_NAME}/bin/activate
            pip install bandit safety detect-secrets
            mkdir -p security-reports
            bandit -r . -f json -o security-reports/bandit-results.json -ll || true
            safety check --output json > security-reports/safety-results.json || true
            detect-secrets scan . > security-reports/secrets-scan.json || true
        """
    } catch (Exception e) {
        echo "Security scan failed: ${e.getMessage()}"
        currentBuild.result = 'UNSTABLE'
    }
}