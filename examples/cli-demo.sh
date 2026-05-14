#!/bin/bash
set -e

mkdir -p .store

TT="java -jar trak-cli"

# 1. Create team
$TT user add manuel --first_name Manuel --last_name Magana --email manuel@company.com --password secret
$TT user add alice --first_name Alice --last_name Smith --email alice@company.com --password secret
$TT user add bob --first_name Bob --last_name Jones --email bob@company.com --password secret

# 2. Login
$TT login manuel --password secret

# 3. Create project (owner defaults to logged-in user)
OUTPUT=$($TT project add MobileApp --summary "Mobile app for iOS and Android")
echo "$OUTPUT"
P1=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)
echo "  → Project ID: $P1"

# 4. Add team members
$TT addmember MobileApp alice
$TT addmember MobileApp bob

# 5. Create tasks (--project requires numeric project ID)
OUTPUT=$($TT task add --title "Setup React Native" --project $P1 --assigned_to manuel --summary "Init repo, configure build" --deadline 2026-05-20 --estimate 4h)
echo "$OUTPUT"
T1=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)
echo "  → Task ID: $T1"

OUTPUT=$($TT task add --title "Design login screen" --project $P1 --assigned_to alice --summary "Figma mockups" --deadline 2026-05-22 --estimate 8h)
echo "$OUTPUT"
T2=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

OUTPUT=$($TT task add --title "Setup CI/CD" --project $P1 --assigned_to bob --summary "GitHub Actions" --deadline 2026-05-21 --estimate 3h)
echo "$OUTPUT"
T3=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

OUTPUT=$($TT task add --title "Implement auth API" --project $P1 --assigned_to manuel --summary "JWT endpoint" --deadline 2026-05-25 --estimate 6h)
echo "$OUTPUT"
T4=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

OUTPUT=$($TT task add --title "Write unit tests" --project $P1 --assigned_to alice --summary "Test auth and nav" --deadline 2026-05-26 --estimate 4h)
echo "$OUTPUT"
T5=$(echo "$OUTPUT" | grep -o '[0-9]\{10,\}' | head -1)

# 6. Verify project
$TT project get $P1

# 7. View workspace
$TT projects
$TT tasks

# 8. Start and stop a task
$TT start $T1
$TT cur
$TT end

# 9. Create sprint and add tasks
OUTPUT=$($TT sprint add Sprint1 --project MobileApp)
echo "$OUTPUT"
$TT sprint update Sprint1 --project MobileApp --start_date 2026-05-19 --end_date 2026-06-01
$TT sprint update Sprint1 --project MobileApp --add_task $T1
$TT sprint update Sprint1 --project MobileApp --add_task $T2
$TT sprint update Sprint1 --project MobileApp --add_task $T3

# 10. Complete a task
$TT complete $T1

# 11. Verify sprint — get sprint ID for detail command
S1=$($TT sprints | grep -o '[0-9]\{10,\}' | head -1)
$TT sprint get $S1
$TT sprints
$TT detail -s $S1

# 12. View tasks (now with sprint column and COMPLETE status)
$TT tasks
