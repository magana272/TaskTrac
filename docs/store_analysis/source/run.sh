#!/usr/bin/env bash
# Store Benchmark — parallel Docker pipeline
# Each store runs in its own container simultaneously.
set -euo pipefail
cd "$(dirname "$0")"

echo "=== Store Benchmark Pipeline (parallel) ==="
echo "  JSON, DuckDB, Redis  → N up to 100K"
echo "  Parquet              → N up to 10K (file-rewrite cap)"
echo ""

docker compose build 2>&1 | tail -5
echo ""
echo "Starting 4 benchmark containers in parallel + Redis..."
docker compose up --abort-on-container-exit analysis 2>&1

echo ""
echo "=== Cleaning up ==="
docker compose down -v

echo ""
echo "=== Output ==="
ls -lh *.png *.csv system_specs.txt 2>/dev/null || echo "(no output files found)"
echo ""
echo "Done. Figures and data in docs/store_analysis/"
