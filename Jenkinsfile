pipeline {
    agent { label 'Host PC' }

    environment {
        VAULT_URL = credentials('VAULT_URL') // Integration line: Vault
        CICD_TOKEN = credentials('CICD_TOKEN') // Integration line: Vault
    }

    stages {
        // Integration function start: Vault
        stage("Retrieve Env Vars") {
            steps {
                script {
                    def response = httpRequest(
                        url: VAULT_URL + '/api/vault/cicd/authservice/e3',
                        httpMode: 'GET',
                        customHeaders: [
                            [name: 'X-CICD-TOKEN', value: CICD_TOKEN, maskValue: true]
                        ],
                        acceptType: 'APPLICATION_JSON'
                    )

                    def json = readJSON text: response.content
                    def envFileContent = ''

                    json.each { entry ->
                        envFileContent += "${entry.key}=${entry.value}\n"
                    }

                    writeFile file: '.env', text: envFileContent
                    echo "Environment variables written to .env"
                }
            }
        }
        // Integration function end: Vault
        stage("Deploy & Health Check") {
            steps {
                script {
                    try {
                        sh "docker compose --env-file .env up --build -d"

                        def maxRetries = 4 * 10
                        def retryInterval = 15
                        def success = false

                        for (int i = 0; i < maxRetries; i++) {
                            try {
                                echo "Health check attempt ${i + 1}..."
                                def healthResponse = httpRequest(
                                    url: 'http://localhost:6001/api/auth/health',
                                    validResponseCodes: '200'
                                )
                                echo "App is healthy: ${healthResponse.status}"
                                success = true
                                break
                            } catch (err) {
                                echo "Health check failed, retrying in ${retryInterval} seconds..."
                                sleep(retryInterval)
                            }
                        }

                        if (!success) {
                            echo "Health check ultimately failed. Tearing down containers."
                            sh "docker compose down"
                            error("Deployment failed: service not healthy.")
                        }

                    } catch (ex) {
                        echo "Unexpected failure: ${ex.getMessage()}"
                        sh "docker compose down"
                        error("Deployment crashed.")
                    }
                }
            }
        }
    }
}
