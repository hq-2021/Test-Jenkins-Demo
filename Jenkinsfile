pipeline {
    agent any

    tools {
        // 使用你 Jenkins 全局配置的 Maven
        maven 'Maven 3.9.5'
    }

    environment {
        GIT_CRED_ID    = 'github-ssh-key'        // 你的 Git 凭据 ID
        IMAGE_NAME     = 'hq-springboot-demo'    // 镜像名称
        CONTAINER_NAME = 'my-demo-app'           // 容器名称
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
                echo '🔍 Checking out code via SSH...'
                sshagent(credentials: [env.GIT_CRED_ID]) {
                    checkout([$class: 'GitSCM',
                            branches: [[name: '*/main']],
                            userRemoteConfigs: [[url: 'git@github.com:hq-2021/Test-Jenkins-Demo.git']]])
                }
            }
        }

        stage('Maven Build') {
            steps {
                echo '🔨 Building application...'
                // 如果是标准 Maven 项目，直接打包；有测试代码可去掉 -DskipTests
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
                // 这里的 docker 命令会直接发送给宿主机的 Docker 引擎执行
                sh """
                    # 构建镜像，同时打上最新版和构建号标签
                    docker build -t ${IMAGE_NAME}:latest -t ${IMAGE_NAME}:${env.BUILD_NUMBER} .
                """
            }
        }

        stage('Docker Deploy') {
            steps {
                echo '🚀 Deploying to Docker...'
                sh """
                    # 1. 停止并删除旧容器（|| true 保证如果本来没运行也不会报错失败）
                    docker stop ${CONTAINER_NAME} || true
                    docker rm ${CONTAINER_NAME} || true

                    # 2. 启动新容器
                    docker run -d --name ${CONTAINER_NAME} -p ${PORT_MAPPING} ${IMAGE_NAME}:latest

                    # 3. 清理悬空镜像（可选，防止磁盘撑爆）
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