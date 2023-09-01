pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                timeout(time: 15, unit: 'SECONDS') {
                    bat 'docker-compose up'
                }
                script {
                    env.curlOutput = bat 'curl -s -o /dev/null -w "%{http_code}" localhost:8080'
                }
                echo "CURL OUTPUT: ${env.curlOutput}"
//                 if(env.curlOutput == '403') {
//                     error("403")
//                 }
            }
        }
    }
}