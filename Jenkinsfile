pipeline {
    agent any

    tools {
        maven 'Maven 3.3.9'
        jdk 'jdk17'
    }

    stages {
        stage("build") {
            steps {
                sh 'mvn clean install'
            }
        }

        stage("deploy") {
            steps {
                sh 'mvn deploy'
            }
        }
    }
}