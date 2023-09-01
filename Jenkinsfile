pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
                        start deploy.bat
                    }
                }
                sleep 10
                script {
                    env.curlOutput = bat "curl -s -o /dev/null -w '%{http_code}' localhost:8080"
                }
                echo "CURL OUTPUT: ${env.curlOutput}"
//                 if(env.curlOutput == '403') {
//                     error("403")
//                 }
            }
        }
    }
}