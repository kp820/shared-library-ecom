def notifyBuildStatus() {
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
