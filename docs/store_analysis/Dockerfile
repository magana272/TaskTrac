# Single image for all benchmark containers.
# Set BENCH_STORE env var at runtime to pick the store.
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew VERSION ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies 2>/dev/null || true
COPY src/ src/

# Pre-compile test classes so each container starts fast
RUN ./gradlew --no-daemon testClasses

# Runtime — entrypoint reads BENCH_STORE, BENCH_SCALES, BENCH_OUTPUT
COPY docs/store_analysis/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["/app/entrypoint.sh"]
