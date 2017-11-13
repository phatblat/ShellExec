/*
 * Jenkinsfile
 * SimpleExec
 * 
 * Declarative pipeline script - https://jenkins.io/doc/book/pipeline/
 */

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew build'
            }
        }
    }
}
