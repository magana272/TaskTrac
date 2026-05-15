# Stage 1: Build and run Java benchmark
FROM eclipse-temurin:17-jdk AS benchmark

WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew VERSION ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies 2>/dev/null || true

COPY src/ src/
COPY docs/store_analysis/ docs/store_analysis/

# Run only the StoreBenchmark test
ENV REDIS_URL=redis://redis:6379
RUN ./gradlew --no-daemon cleanTest test \
    --tests "task.trak.benchmark.StoreBenchmark" \
    -x compileTestJava 2>/dev/null; \
    ./gradlew --no-daemon cleanTest test \
    --tests "task.trak.benchmark.StoreBenchmark" || true

# Stage 2: R analysis
FROM r-base:4.4.1 AS analysis

RUN apt-get update && apt-get install -y --no-install-recommends \
    libcairo2-dev libxt-dev pkg-config && \
    rm -rf /var/lib/apt/lists/*
RUN Rscript -e 'install.packages(c("ggplot2", "dplyr", "scales"), repos="https://cloud.r-project.org")'

WORKDIR /analysis
COPY --from=benchmark /app/docs/store_analysis/results.csv .
COPY --from=benchmark /app/docs/store_analysis/trial_data.csv .
COPY --from=benchmark /app/docs/store_analysis/system_specs.txt .
COPY docs/store_analysis/analyze.R .

RUN Rscript analyze.R

# Stage 3: Collect output
FROM alpine:3.20

WORKDIR /output
COPY --from=analysis /analysis/*.png .
COPY --from=analysis /analysis/*.csv .
COPY --from=analysis /analysis/system_specs.txt .

CMD ["sh", "-c", "ls -la /output && echo 'Copy output with: docker cp <container>:/output/ docs/store_analysis/'"]
