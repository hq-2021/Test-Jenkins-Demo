pipeline {
    agent any

    tools {
        // 使用你 Jenkins 全局配置的 Maven 名称
        maven 'Maven 3.9.5'
    }

    environment {
        // ⚠️ 注意：如果是 HTTPS 拉取，Docker 镜像名不能包含大写字母
        IMAGE_NAME     = 'hq-springboot-demo'    // 你的镜像名称
        CONTAINER_NAME = 'my-demo-app'           // 你的容器名称
        // 宿主机端口:容器端口 (确保宿主机 8082 端口没被占用)
        PORT_MAPPING   = '8082:8082'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔍 Checking out code via HTTPS...'
                // 使用你截图里的凭据 ID 和 HTTPS 地址
                git branch: 'main',
                url: 'https://github.com/hq-2021/Test-Jenkins-Demo.git',
                credentialsId: 'Github-jenkins'

                script {
                    env.GIT_COMMIT_MSG = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    env.GIT_COMMIT_AUTHOR = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
                }
                echo "Commit: ${env.GIT_COMMIT_MSG} by ${env.GIT_COMMIT_AUTHOR}"
            }
        }

        stage('Maven Build') {
            steps {
                echo '🔨 Building application...'
                // 编译并打包
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                // 前提：你的项目根目录下必须有 Dockerfile
                sh """
                    docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${env.BUILD_NUMBER} .
                """
            }
        }

        stage('Docker Deploy') {
            steps {
                echo '🚀 Deploying to Docker...'
                sh """
                    # 1. 停止并删除旧容器
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    # 2. 启动新容器
                    docker run -d --name ${CONTAINER_NAME} -p ${PORT_MAPPING} ${IMAGE_NAME}:latest

                    # 3. 清理无用悬空镜像，防止撑爆磁盘
                    docker image prune -f
                """
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }
        success {
            echo '🎉 Pipeline completed successfully!'
        }
        failure {
            echo '💥 Pipeline failed!'
        }
    }
}