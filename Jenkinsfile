// pipeline {
//     agent {label 'Host PC'}
//
//     stages {
//         stage("deploy") {
//             steps {
//                 script {
//                     try {
//                         //Start the deployment
//                         bat "docker-compose up --build -d"
//
//                         //Wait 15 seconds for the application to start
//                         sleep 15
//
//                         //Make an httpRequest to the health check URL
//                         def response = httpRequest 'http://localhost:6001/health'
//                     } catch(Exception ex) {
//                         //Compose down the docker container
//                         bat "docker-compose down"
//
//                         //Fail the deploy stage
//                         error("Health check failed")
//                     }
//                 }
//             }
//         }
//     }
// }

pipeline {
    agent { label 'Host PC' }

    stages {
        stage("Deploy & Health Check") {
            steps {
                script {
                    try {
                        bat "docker-compose up --build -d"

                        def maxRetries = 4 * 10
                        def retryInterval = 15
                        def success = false

                        for (int i = 0; i < maxRetries; i++) {
                            try {
                                echo "Health check attempt ${i + 1}..."
                                def response = httpRequest(
                                    url: 'http://localhost:6001/auth/health',
                                    validResponseCodes: '200'
                                )
                                echo "App is healthy: ${response.status}"
                                success = true
                                break
                            } catch (err) {
                                echo "Health check failed, retrying in ${retryInterval} seconds..."
                                sleep(retryInterval)
                            }
                        }

                        if (!success) {
                            echo "Health check ultimately failed. Tearing down containers."
                            bat "docker-compose down"
                            error("Deployment failed: service not healthy.")
                        }

                    } catch (ex) {
                        echo "Unexpected failure: ${ex.getMessage()}"
                        bat "docker-compose down"
                        error("Deployment crashed.")
                    }
                }
            }
        }
    }
}
