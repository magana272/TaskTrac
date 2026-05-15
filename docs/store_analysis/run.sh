#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

echo "=== Store Benchmark Pipeline ==="
echo "  JSON, DuckDB, Redis, MongoDB → N up to 100K"
echo "  Parquet                      → N up to 10K"
echo ""

docker compose build 2>&1 | tail -5
echo ""
echo "Starting 5 benchmark containers + Redis + MongoDB..."
docker compose up --abort-on-container-exit analysis 2>&1

echo ""
docker compose down -v

echo ""
ls -lh *.png *.csv system_specs.txt 2>/dev/null || echo "(no output files found)"
echo "Done."
