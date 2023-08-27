pipeline {
    agent any

    stages {
        stage("build") {
            steps {
                mvn clean install
            }
        }

        stage("deploy") {
            steps {
                mvn deploy
            }
        }
    }
}