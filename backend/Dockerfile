# ============================================================
# 星光国际影城 CMS 后端镜像（多阶段构建）
# 构建：docker build -t cms-server:latest ./backend
# 运行：docker run -p 8080:8080 --env-file ../.env cms-server:latest
# ============================================================

# ---------- 阶段 1：Maven 编译打包 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷 pom，利用 Docker 层缓存跳过重复下载依赖
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 再拷源码编译（产物名由 pom 的 finalName=cms-server 固定）
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- 阶段 2：精简 JRE 运行 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区 + JVM 参数（G1GC / 容器内存感知 / OOM 自动 dump）
ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

# 健康检查依赖 curl；顺手创建非 root 运行用户与数据目录
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r cms && useradd -r -g cms cms \
    && mkdir -p /data/uploads /app/logs \
    && chown -R cms:cms /data /app

COPY --from=builder /build/target/cms-server.jar app.jar

USER cms
VOLUME ["/data/uploads", "/app/logs"]
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:8080/api/actuator/health | grep -q '"status":"UP"' || exit 1

# 上传目录固定到挂载卷；其余连接参数全部走环境变量（见 .env.example）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --cms.upload.path=/data/uploads/"]
