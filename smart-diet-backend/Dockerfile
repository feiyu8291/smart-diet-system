# ==========================================
# 1. 编译打包阶段
# ==========================================
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
# 预先下载依赖以提升后续构建速度
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 2. 生产运行阶段
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/smart-diet-backend-1.0.0.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
