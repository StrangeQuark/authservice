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
                sleep 10
                script {
                    curlOutput = bat "curl -i localhost:8080"
                    echo curlOutput
                    if(curlOutput == null) {
                        echo "Test"
                    }
                    echo "CURL OUTPUT: ${curlOutput}"
                }
//                 echo "CURL OUTPUT: ${env.curlOutput}"
//                 if(env.curlOutput == '403') {
//                     error("403")
//                 }
            }
        }
    }
}