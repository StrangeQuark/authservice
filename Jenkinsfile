pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    try {
                        //Run the start script
                        bat "start deploy.bat"

                        //Sleep for 15 seconds to allow the server to start
                        sleep 15

                        //Make a health check request
                        def response = httpRequest 'http://localhost:8080/api/v1/health'

                        //Keep the process alive if the healthcheck is a success
                        env.JENKINS_NODE_COOKIE="dontKill"

                        //Run an extra command to allow JENKINS_NODE_COOKIE time to change before exiting
                        echo "Deploy stage complete"
                    } catch(Exception ex) {
                        error("The health check did not pass")
                    }
                }
            }
        }
    }
}