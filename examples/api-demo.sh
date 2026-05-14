#!/usr/bin/env bash
# Trak REST API demo — exercises all endpoints
# Usage: ./examples/api-demo.sh [base_url]
#   Defaults to http://localhost:8080

set -euo pipefail

BASE="${1:-http://localhost:8080}"
TOKEN=""

# ── Helpers ──────────────────────────────────────────────
post()   { curl -s -X POST   -H "Content-Type: application/json" ${TOKEN:+-H "Authorization: Bearer $TOKEN"} -d "$2" "$BASE$1"; echo; }
get()    { curl -s -X GET    ${TOKEN:+-H "Authorization: Bearer $TOKEN"} "$BASE$1"; echo; }
put()    { curl -s -X PUT    -H "Content-Type: application/json" ${TOKEN:+-H "Authorization: Bearer $TOKEN"} -d "$2" "$BASE$1"; echo; }
delete() { curl -s -X DELETE ${TOKEN:+-H "Authorization: Bearer $TOKEN"} "$BASE$1"; echo; }

section() { echo; echo "══════════════════════════════════════"; echo "  $1"; echo "══════════════════════════════════════"; }

# ── Auth ─────────────────────────────────────────────────
section "1. SIGNUP"
RESP=$(post "/api/auth/signup" '{"username":"manuel","password":"pass123","firstName":"Manuel","lastName":"Magana","email":"manuel@example.com"}')
echo "$RESP"
TOKEN=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])" 2>/dev/null || true)
echo "  → token: ${TOKEN:0:12}..."

section "2. LOGOUT"
post "/api/auth/logout" '{}'

section "3. LOGIN"
RESP=$(post "/api/auth/login" '{"username":"manuel","password":"pass123"}')
echo "$RESP"
TOKEN=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])" 2>/dev/null || true)
echo "  → token: ${TOKEN:0:12}..."

# ── Users ────────────────────────────────────────────────
section "4. GET USER"
get "/api/users/manuel"

section "5. UPDATE USER"
put "/api/users/manuel" '{"firstName":"Manuel","lastName":"Magana-Updated","email":"m@trak.dev"}'

section "6. CREATE SECOND USER (for project member)"
post "/api/users" '{"username":"alice","firstName":"Alice","lastName":"Dev","email":"alice@example.com","password":"alice123"}'

# ── Projects ─────────────────────────────────────────────
section "7. CREATE PROJECT"
post "/api/projects" '{"name":"WebApp","summary":"Main web application","ownerUsername":"manuel","memberUsernames":["alice"]}'

section "8. LIST PROJECTS (by user)"
get "/api/projects?user=manuel"

section "9. GET PROJECT BY NAME"
get "/api/projects/name/WebApp"

section "10. UPDATE PROJECT"
put "/api/projects/WebApp" '{"newName":"WebApp","newSummary":"Updated description","newMemberUsernames":["alice"]}'

section "11. ADD MEMBER"
post "/api/projects/WebApp/members" '{"username":"alice"}'

# ── Tasks ────────────────────────────────────────────────
section "12. CREATE TASKS"
T1=$(post "/api/tasks" '{"title":"Setup CI/CD","projectName":"WebApp","assignedTo":"manuel","summary":"Configure pipelines","estimate":"2d 4h 0m"}')
echo "$T1"
TASK_ID=$(echo "$T1" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "1")
echo "  → task id: $TASK_ID"

post "/api/tasks" '{"title":"Write tests","projectName":"WebApp","assignedTo":"alice","summary":"Unit + integration","estimate":"1d 0h 0m"}'

section "13. LIST TASKS (by assignee)"
get "/api/tasks?assignee=manuel"

section "14. GET TASK BY ID"
get "/api/tasks/$TASK_ID"

section "15. UPDATE TASK"
put "/api/tasks/$TASK_ID" '{"status":"INPROGRESS"}'

# ── Sprints ──────────────────────────────────────────────
section "16. CREATE SPRINT"
post "/api/sprints" '{"name":"Sprint-1","projectName":"WebApp"}'

section "17. LIST SPRINTS"
get "/api/sprints"

section "18. GET SPRINT BY NAME"
get "/api/sprints/name/Sprint-1?project=WebApp"

section "19. UPDATE SPRINT (add task + dates)"
# Get sprint ID first
SPRINT_ID=$(get "/api/sprints/name/Sprint-1?project=WebApp" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "1")
put "/api/sprints/$SPRINT_ID" '{"startDate":"2026-06-01","endDate":"2026-06-14","addTask":'$TASK_ID'}'

# ── Backlogs ─────────────────────────────────────────────
section "20. CREATE BACKLOG"
post "/api/backlogs" '{"name":"MainBacklog","projectName":"WebApp"}'

section "21. ADD TASK TO BACKLOG"
put "/api/backlogs/MainBacklog" '{"addTask":'$TASK_ID'}'

section "22. GET BACKLOG"
get "/api/backlogs/MainBacklog"

section "23. REMOVE TASK FROM BACKLOG"
put "/api/backlogs/MainBacklog" '{"removeTask":'$TASK_ID'}'

# ── Cleanup ──────────────────────────────────────────────
section "24. DELETE BACKLOG"
delete "/api/backlogs/MainBacklog"

section "25. DELETE TASK"
delete "/api/tasks/$TASK_ID"

section "26. DELETE SPRINT"
delete "/api/sprints/Sprint-1"

section "27. DELETE PROJECT"
delete "/api/projects/WebApp"

section "28. DELETE USER"
delete "/api/users/alice"

echo
echo "✓ Done — all endpoints exercised."
