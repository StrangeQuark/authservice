pipeline {
    agent { label 'linux-agent' }

    environment {
        VAULT_URL = credentials('VAULT_URL')
        CICD_TOKEN = credentials('AUTH_CICD_TOKEN')
        VAULTSERVICE_ENABLED = credentials('VAULTSERVICE_ENABLED')
        KUBERNETES_CICD_TOKEN = credentials('KUBERNETES_CICD_TOKEN')
    }

    stages {

        stage("Retrieve Env Vars") {
            steps {
                script {
                    if(VAULTSERVICE_ENABLED == "true") {
                        def response = httpRequest(
                            url: VAULT_URL + '/api/vault/cicd',
                            httpMode: 'POST',
                            contentType: 'APPLICATION_JSON',
                            requestBody: '{"serviceName":"authservice","environmentName":"e3"}',
                            customHeaders: [
                                [name: 'X-CICD-TOKEN', value: CICD_TOKEN, maskValue: true]
                            ],
                            validResponseCodes: '200'
                        )

                        writeFile file: 'authservice.env', text: response.content
                        echo "Environment variables written to authservice.env"
                    } else {
                        withCredentials([file(credentialsId: 'AUTH_SERVICE_ENV', variable: 'AUTH_SERVICE_ENV')]) {
                            sh 'cp "$AUTH_SERVICE_ENV" authservice.env'
                        }
                    }
                }
            }
        }

        stage("Deploy & Health Check") {
            steps {
                script {
                    def kubernetesEnabled = sh(
                        script: "grep -qx 'KUBERNETES_ENABLED=true' authservice.env",
                        returnStatus: true
                    ) == 0

                    if(kubernetesEnabled) {
                        def environmentVariables = readProperties file: 'authservice.env'
                        def authServiceImageRepository = environmentVariables.get('AUTH_SERVICE_IMAGE_REPOSITORY', '')
                        def kubernetesServiceUrl = environmentVariables.get('KUBERNETESERVICE_URL', '')

                        if(authServiceImageRepository.isEmpty() || kubernetesServiceUrl.isEmpty())
                            error("AuthService Kubernetes deployment configuration is incomplete")

                        def authServiceImage = authServiceImageRepository + ":" + env.BUILD_NUMBER

                        withEnv([
                            "AUTH_SERVICE_IMAGE=" + authServiceImage,
                            "KUBERNETESERVICE_URL=" + kubernetesServiceUrl
                        ]) {
                            sh "docker build -t " + authServiceImage + " ."
                            sh "docker push " + authServiceImage
                            sh '''
                                curl --fail-with-body \
                                    -X POST \
                                    -H "X-CICD-TOKEN: $KUBERNETES_CICD_TOKEN" \
                                    -F "serviceName=authservice" \
                                    -F "image=$AUTH_SERVICE_IMAGE" \
                                    -F "environmentFile=@authservice.env" \
                                    "$KUBERNETESERVICE_URL/api/kubernetes/deploy"
                            '''
                        }
                    } else {
                    try {
                        sh "docker compose --env-file authservice.env up --build -d"

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

    post {
        always {
            sh "rm -f authservice.env"
            echo "Cleaned up authservice.env"
        }
    }

}
