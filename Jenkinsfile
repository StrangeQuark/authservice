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
                    if(curlOutput == "HTTP/1.1 403
                                                        X-Content-Type-Options: nosniff
                                                        X-XSS-Protection: 0
                                                        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
                                                        Pragma: no-cache
                                                        Expires: 0
                                                        X-Frame-Options: DENY
                                                        Content-Length: 0
                                                        Date: Fri, 01 Sep 2023 23:56:56 GMT") {
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