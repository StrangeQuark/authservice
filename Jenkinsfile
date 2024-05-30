pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    try {
                        //Start the deployment
                        bat "docker-compose up --build -d"

                        //Wait 15 seconds for the application to start
                        sleep 15

                        //Make an httpRequest to the health check URL
                        def response = httpRequest 'http://localhost:8080/api/v1/health'
                    } catch(Exception ex) {
                        //Compose down the docker container
                        bat "docker-compose down"

                        //Fail the deploy stage
                        error("Health check failed")
                    }
                }
            }
        }
    }
}