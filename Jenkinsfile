pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                script {
                    withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
                        bat "start deploy.bat"
                    }
                }
                sleep 15
                script {
                    env.curlOutput = bat "curl -i localhost:8080"
                    echo env.curlOutput
                    if(env.curlOutput == null) {
                        echo "Test"
                    }
                    echo "CURL OUTPUT: ${env.curlOutput}"
                }
//                 echo "CURL OUTPUT: ${env.curlOutput}"
//                 if(env.curlOutput == '403') {
//                     error("403")
//                 }
            }
        }
    }
}