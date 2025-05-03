FROM ghcr.io/graalvm/graalvm-ce:21 AS graalvm
WORKDIR /home/app

COPY build.gradle gradle.properties settings.gradle gradle gradlew /home/app/
COPY src /home/app/src

RUN chmod +x gradlew
RUN ./gradlew nativeImage

FROM cgr.dev/chainguard/wolfi-base:latest
EXPOSE 8080
COPY --link --from=graalvm /home/app/application /app/application
ENTRYPOINT ["/app/application"]
