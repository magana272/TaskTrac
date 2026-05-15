#!/usr/bin/env bash
set -euo pipefail

STORE="${BENCH_STORE:?BENCH_STORE must be set (JSON, PARQUET, DUCKDB, REDIS)}"
OUTPUT="${BENCH_OUTPUT:-/output}"
SCALES="${BENCH_SCALES:-10,100,1000,2000,4000,10000,100000}"

export BENCH_STORE="$STORE"
export BENCH_SCALES="$SCALES"
export BENCH_OUTPUT="$OUTPUT"

echo "=== Store Benchmark: $STORE ==="
echo "    Scales: $SCALES"
echo "    Output: $OUTPUT"

mkdir -p "$OUTPUT"

./gradlew --no-daemon test \
    --tests "task.trak.benchmark.StoreBenchmark" \
    -Dorg.gradle.jvmargs="-Xmx512m" \
    2>&1 | tail -40

echo "=== $STORE benchmark complete ==="
