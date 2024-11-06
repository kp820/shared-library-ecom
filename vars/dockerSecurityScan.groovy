def call() {
    try {
        sh """
            curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | TAG=latest sh -s -- -b ${WORKSPACE}/trivy
            mkdir -p security-reports
            ${WORKSPACE}/trivy/trivy image ${env.DOCKER_IMAGE}:${BUILD_NUMBER} \
                --no-progress --exit-code 0 \
                --severity HIGH,CRITICAL \
                --format template \
                --template '{{- range . }}{{- range .Vulnerabilities }}{{println .VulnerabilityID .Severity .PkgName .InstalledVersion .FixedVersion }}{{- end }}{{- end }}' \
                > security-reports/trivy-results.txt
        """
    } catch (Exception e) {
        echo "Trivy scan failed: ${e.getMessage()}"
        currentBuild.result = 'UNSTABLE'
    }
}
