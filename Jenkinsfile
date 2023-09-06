pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
                        try {
                            bat "start deploy.bat"

                            sleep 15

                            def response = httpRequest 'http://localhost:8080/api/v1/healt'
                        } catch(Exception ex) {
                            JENKINS_NODE_COOKIE=kill
                        }
                    }
                }
            }
        }
    }
}