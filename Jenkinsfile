/*
 * Jenkinsfile
 * SimpleExec
 *
 * Declarative pipeline script - https://jenkins.io/doc/book/pipeline/
 */

pipeline {
    agent any

    options {
        // https://jenkins.io/doc/book/pipeline/syntax/#options
        buildDiscarder(logRotator(numToKeepStr: '100'))
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    triggers {
        // cron('H */4 * * 1-5')
        githubPush()
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew build --stacktrace'
            }
        }
        stage('Test') {
            steps {
                sh './gradlew build --stacktrace'
            }
        }
    }
}
