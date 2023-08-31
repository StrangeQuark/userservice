pipeline {
    agent {label 'Host PC'}

    tools {
        maven 'Maven 3.9.4'
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
                sh 'docker-compose up'
            }
        }
    }
}