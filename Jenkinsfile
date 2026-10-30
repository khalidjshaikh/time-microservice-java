pipeline {
    agent any

    environment {
        PATH = "/opt/homebrew/bin:/opt/homebrew/opt/openjdk/bin:/opt/homebrew/opt/ruby/bin:/Users/kshaikh/.nvm/versions/node/v26.11.1/bin:/Users/kshaikh/.pyenv/shims:/usr/bin:/bin:/usr/sbin:/sbin"
        IMAGE = "time-microservice-java"
        TAG   = "${env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh './scripts/test.sh'
            }
        }

        stage('Build Binary') {
            steps {
                sh './scripts/build.sh'
            }
        }

        stage('Build Image') {
            steps {
                sh './scripts/package.sh'
            }
        }

        stage('Push Image') {
            when {
                expression { return env.REGISTRY?.trim() }
            }
            steps {
                sh '''
                    docker tag "$IMAGE:latest" "$REGISTRY/$IMAGE:$TAG"
                    docker push "$REGISTRY/$IMAGE:$TAG"
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    export NAMESPACE="${NAMESPACE:-default}"
                    export IMAGE="${REGISTRY:+$REGISTRY/}$IMAGE"
                    export TAG="$TAG"
                    ./scripts/deploy.sh
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'bin/*,coverage.out', allowEmptyArchive: true
        }
    }
}
