# Multi-stage build for optimized image size
FROM gradle:8.5-jdk21 AS build

# UTF-8 인코딩 설정
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /app

# Copy Gradle wrapper files first
COPY gradlew gradlew.bat ./
COPY gradle gradle

# Copy Gradle config files
COPY build.gradle.kts settings.gradle.kts ./

# Give execution permission to gradlew
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application
RUN ./gradlew build -x test --no-daemon -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8888

# Run application
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
