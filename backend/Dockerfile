# Multi-stage build: build with full JDK, run on slim JRE
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :backend:installDist --no-daemon

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /app/backend/build/install/backend/ ./
EXPOSE 8080
ENV APP_ENVIRONMENT=production
ENTRYPOINT ["./bin/backend"]
