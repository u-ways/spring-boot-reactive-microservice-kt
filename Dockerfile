FROM gcr.io/distroless/java21:latest

ENV JDK_JAVA_OPTIONS="-Xmx3072m -XX:MaxMetaspaceSize=768m -XX:ReservedCodeCacheSize=384m"

ARG APP_NAME=modern-reactive-spring-backend-kotlin
ARG APP_VERSION=DEV-SNAPSHOT
ARG APP_JAR_LOCATION=./build/libs/${APP_NAME}-${APP_VERSION}.jar
ARG APP_PORT=8080

LABEL app="${APP_NAME}" \
      maintainer="U-ways (work@u-ways.info)" \
      description="A modern reactive Spring Boot Kotlin application based on the Katanox API." \
      url="https://github.com/u-ways/modern-reactive-spring-backend-kotlin"

COPY ${APP_JAR_LOCATION} /usr/app/api.jar

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/usr/app/api.jar"]
