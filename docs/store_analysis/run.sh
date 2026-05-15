#!/usr/bin/env bash
# Store Benchmark — Docker pipeline
# Runs Java benchmark (with Redis) then R analysis, outputs to this directory.
set -euo pipefail
cd "$(dirname "$0")"

echo "=== Store Benchmark Pipeline ==="
echo "Starting Redis + benchmark containers..."

docker compose up --build --abort-on-container-exit benchmark 2>&1

echo ""
echo "=== Cleaning up containers ==="
docker compose down -v

echo ""
echo "=== Output ==="
ls -lh *.png *.csv system_specs.txt 2>/dev/null || echo "(no output files found)"
echo ""
echo "Done. Figures and data are in docs/store_analysis/"
