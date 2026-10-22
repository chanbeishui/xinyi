# 仅打包已验证jar。实际发布必须将JRE_IMAGE锁到批准digest。
ARG JRE_IMAGE=eclipse-temurin:21-jre
FROM ${JRE_IMAGE}
WORKDIR /app
RUN groupadd --system vi && useradd --system --gid vi --home /app vi
COPY --chown=vi:vi app.jar /app/app.jar
USER vi
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","/app/app.jar"]
