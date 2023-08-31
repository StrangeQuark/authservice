pipeline {
    agent {label 'Host PC'}

//     tools {
//         maven 'Maven 3.9.4'
//         jdk 'jdk17'
//     }

    stages {
        stage("build") {
            steps {
                sh 'java --version'
            }
        }

        stage("deploy") {
            steps {
                sh 'docker-compose up'
            }
        }
    }
}