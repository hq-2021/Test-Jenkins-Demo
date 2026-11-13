# 使用轻量级的 JDK 21 基础镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# ⚠️ 重点：直接指定确切的 jar 包名字，不要用 *.jar
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口（如果你的 application.yml 里是 8082，这里就写 8082）
EXPOSE 8082

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]