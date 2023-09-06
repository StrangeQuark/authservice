pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
                        bat "start deploy.bat"

                        sleep 30

                        def response = httpRequest 'http://localhost:8080/health'

                        echo "Status: ${response.status}"
                    }
                }
            }
        }
    }
}