FROM ghcr.io/graalvm/native-image-community:21-ol9 AS graalvm
WORKDIR /home/app

COPY build.gradle gradle.properties settings.gradle gradlew /home/app
COPY src /home/app

RUN ./gradlew nativeImage

FROM gcr.io/distroless/base:latest
EXPOSE 8080
COPY --link --from=graalvm /home/app/application /app/application
ENTRYPOINT ["/app/application"]


