#!/bin/bash
# Test script: loads data via CLI, then launches GUI
# Verifies: responsive cards, timer bar, button consistency
set -e

echo "=== Resetting store ==="
rm -rf .store .cache

echo "=== Starting embedded CLI to seed data ==="
TT="java -jar trak-cli"

# Create user and login
$TT user add dev1 --first_name Dev --last_name One --email dev1@test.com --password pass
$TT login dev1 --password pass

# Create a project
OUTPUT=$($TT project add FocusApp --summary "Solo dev focus app")
echo "$OUTPUT"
P1=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

# Create tasks with various estimates
T1=$($TT task add --title "Setup project structure" --project $P1 --assigned_to dev1 --summary "Init repo" --deadline 2026-05-20 --estimate 1h | grep -o '[0-9]\{10,\}' | head -1)
T2=$($TT task add --title "Build login screen" --project $P1 --assigned_to dev1 --summary "Auth UI" --deadline 2026-05-21 --estimate 2h | grep -o '[0-9]\{10,\}' | head -1)
T3=$($TT task add --title "Write API endpoints" --project $P1 --assigned_to dev1 --summary "REST routes" --deadline 2026-05-22 --estimate 4h | grep -o '[0-9]\{10,\}' | head -1)
T4=$($TT task add --title "Unit tests" --project $P1 --assigned_to dev1 --summary "Test coverage" --deadline 2026-05-23 --estimate 3h | grep -o '[0-9]\{10,\}' | head -1)
T5=$($TT task add --title "Deploy to staging" --project $P1 --assigned_to dev1 --summary "CI/CD setup" --deadline 2026-05-24 --estimate 30m | grep -o '[0-9]\{10,\}' | head -1)

# Create sprint and add tasks
$TT sprint add Sprint1 --project FocusApp
$TT sprint update Sprint1 --project FocusApp --start_date 2026-05-14 --end_date 2026-05-28
$TT sprint update Sprint1 --project FocusApp --add_task $T1
$TT sprint update Sprint1 --project FocusApp --add_task $T2
$TT sprint update Sprint1 --project FocusApp --add_task $T3

# Set one task to INPROGRESS (this should start the server-side timer)
$TT task update $T1 --status INPROGRESS

# Complete one task
$TT complete $T2

echo ""
echo "=== Data seeded ==="
echo "  Project: FocusApp (ID: $P1)"
echo "  Tasks: $T1 (INPROGRESS), $T2 (COMPLETE), $T3 (READY), $T4 (READY), $T5 (READY)"
echo "  Sprint: Sprint1 with tasks $T1, $T2, $T3"
echo ""
echo "=== Launching GUI ==="
echo "  Login as: dev1 / pass"
echo "  Look for:"
echo "    - Timer bar on task $T1 (INPROGRESS, 1h estimate)"
echo "    - All buttons same style (green primary)"
echo "    - Cards resize when you drag window edges"
echo "    - Sprint progress: 1/3 tasks complete (33%)"
echo ""

java -jar trak-gui --local
