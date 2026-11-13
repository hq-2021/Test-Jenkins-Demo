# 使用轻量级的 JDK 21 基础镜像（匹配你的 Jenkins 环境）
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 将 Maven 打包好的 jar 复制进去
COPY target/*.jar app.jar

# 声明服务端口（如果你的 SpringBoot 不是 8080，请修改这里）
EXPOSE 8082

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]