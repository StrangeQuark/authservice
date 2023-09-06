pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    try {
                        bat "start deploy.bat"

                        sleep 15

                        def response = httpRequest 'http://localhost:8080/api/v1/healt'

                        JENKINS_NODE_COOKIE=dontkill
                    } catch(Exception ex) {
                    }
                }
            }
        }
    }
}