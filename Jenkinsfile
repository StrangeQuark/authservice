pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                bat 'docker-compose up'
                sleep 15
                env.curlOutput = (bat 'curl -s -o /dev/null -w "%{http_code}" localhost:8080')
                echo "CURL OUTPUT: ${env.curlOutput}"
                if(env.curlOutput == '403') {
                    error("403")
                }
            }
        }
    }
}