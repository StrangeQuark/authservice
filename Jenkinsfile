pipeline {
    agent {label 'Host PC'}

//     tools {
//         maven 'Maven 3.9.4'
//         jdk 'jdk17'
//     }

    stages {
        stage("build") {
            steps {
                bat 'type nul > test.txt'
            }
        }

        stage("deploy") {
            steps {
                bat 'docker-compose up'
            }
        }
    }
}