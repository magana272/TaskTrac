#!/bin/bash
set -e

echo "=== Resetting store ==="
rm -rf .store .cache
make build 2>/dev/null

TT="java -jar trak-cli"

$TT user add dev1 --first_name Dev --last_name One --email dev1@test.com --password pass
$TT login dev1 --password pass

OUTPUT=$($TT project add FocusApp --summary "Solo dev focus app")
P1=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

# 20 tasks — mix of short (1-3 min) and longer estimates
T1=$($TT task add --title "Fix typo in README" --project $P1 --assigned_to dev1 --summary "Quick fix" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T2=$($TT task add --title "Update .gitignore" --project $P1 --assigned_to dev1 --summary "Add build artifacts" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T3=$($TT task add --title "Rename variable" --project $P1 --assigned_to dev1 --summary "Refactor naming" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T4=$($TT task add --title "Add license file" --project $P1 --assigned_to dev1 --summary "MIT license" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T5=$($TT task add --title "Write login endpoint" --project $P1 --assigned_to dev1 --summary "POST /auth/login" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)
T6=$($TT task add --title "Add input validation" --project $P1 --assigned_to dev1 --summary "Validate email format" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T7=$($TT task add --title "Create user model" --project $P1 --assigned_to dev1 --summary "User schema" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)
T8=$($TT task add --title "Setup database" --project $P1 --assigned_to dev1 --summary "Init DuckDB tables" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T9=$($TT task add --title "Write unit test" --project $P1 --assigned_to dev1 --summary "Test auth flow" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)
T10=$($TT task add --title "Fix null pointer" --project $P1 --assigned_to dev1 --summary "Handle null session" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T11=$($TT task add --title "Add error dialog" --project $P1 --assigned_to dev1 --summary "Show user-facing errors" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T12=$($TT task add --title "Style buttons" --project $P1 --assigned_to dev1 --summary "Uniform button sizes" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T13=$($TT task add --title "Add delete confirm" --project $P1 --assigned_to dev1 --summary "Confirm before delete" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T14=$($TT task add --title "Refactor controller" --project $P1 --assigned_to dev1 --summary "Extract methods" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)
T15=$($TT task add --title "Add loading spinner" --project $P1 --assigned_to dev1 --summary "Show during fetch" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T16=$($TT task add --title "Fix date formatting" --project $P1 --assigned_to dev1 --summary "Use ISO-8601" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T17=$($TT task add --title "Write API docs" --project $P1 --assigned_to dev1 --summary "Document endpoints" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)
T18=$($TT task add --title "Add sort dropdown" --project $P1 --assigned_to dev1 --summary "Sort by date/estimate" --estimate 2m | grep -o '[0-9]\{10,\}' | head -1)
T19=$($TT task add --title "Fix card hover" --project $P1 --assigned_to dev1 --summary "Gold glow on hover" --estimate 1m | grep -o '[0-9]\{10,\}' | head -1)
T20=$($TT task add --title "Deploy to prod" --project $P1 --assigned_to dev1 --summary "Final deploy" --estimate 3m | grep -o '[0-9]\{10,\}' | head -1)

# Sprint with 10 tasks
$TT sprint add Sprint1 --project FocusApp
$TT sprint update Sprint1 --project FocusApp --start_date 2026-05-14 --end_date 2026-05-21
for T in $T1 $T2 $T3 $T4 $T5 $T6 $T7 $T8 $T9 $T10; do
    $TT sprint update Sprint1 --project FocusApp --add_task $T
done

# Set 3 tasks in progress (timers will run)
$TT task update $T1 --status INPROGRESS
$TT task update $T5 --status INPROGRESS
$TT task update $T9 --status INPROGRESS

# Complete 2 tasks
$TT complete $T2
$TT complete $T4

echo ""
echo "=== 20 tasks seeded ==="
echo "  3 INPROGRESS: $T1 (1m), $T5 (3m), $T9 (3m) — timer bars active"
echo "  2 COMPLETE: $T2, $T4"
echo "  15 READY"
echo "  Sprint1: 10 tasks (2 complete, 3 in progress, 5 ready)"
echo ""
echo "  Login: dev1 / pass"
echo ""

java -jar trak-gui --local
