pipeline {
    agent {label 'Host PC'}

    stages {
        stage("deploy") {
            steps {
                bat 'docker-compose up'
            }
        }
    }
}