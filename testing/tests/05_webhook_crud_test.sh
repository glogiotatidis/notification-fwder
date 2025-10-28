#!/bin/bash

# Test 05: Webhook CRUD Test
# Tests database CRUD operations for webhooks

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../helpers/adb_helpers.sh"

TEST_NAME="webhook_crud_test"

echo "========================================="
echo "Test 05: Webhook CRUD Test"
echo "========================================="

# Ensure app is running
launch_app
sleep 2

# Test 1: Check initial webhook count
echo -n "Checking initial webhook count... "
INITIAL_COUNT=$(count_table_rows "webhook_configs" || echo "0")
echo "OK (count: $INITIAL_COUNT)"

# Test 2: Insert webhook via database
echo -n "Inserting test webhook... "
execute_sql "INSERT INTO webhook_configs (url, headers, enabled, createdAt, updatedAt) VALUES ('https://test.example.com/webhook', '{}', 1, $(date +%s)000, $(date +%s)000);" || true
sleep 1
NEW_COUNT=$(count_table_rows "webhook_configs" || echo "0")
if [ "$NEW_COUNT" -gt "$INITIAL_COUNT" ]; then
    echo "OK (new count: $NEW_COUNT)"
else
    echo "FAILED - Webhook not inserted"
    exit 1
fi

# Test 3: Read webhook
echo -n "Reading webhook... "
WEBHOOK_URL=$(execute_sql "SELECT url FROM webhook_configs LIMIT 1;" | tr -d '\r')
if [ -n "$WEBHOOK_URL" ]; then
    echo "OK (URL: $WEBHOOK_URL)"
else
    echo "FAILED"
    exit 2
fi

# Test 4: Update webhook
echo -n "Updating webhook... "
execute_sql "UPDATE webhook_configs SET enabled = 0 WHERE url = '$WEBHOOK_URL';" || true
sleep 1
ENABLED=$(execute_sql "SELECT enabled FROM webhook_configs WHERE url = '$WEBHOOK_URL';" | tr -d '\r')
if [ "$ENABLED" = "0" ]; then
    echo "OK"
else
    echo "FAILED - Update did not persist"
    exit 3
fi

# Test 5: Check trigger rules relationship
echo -n "Checking trigger rules relationship... "
WEBHOOK_ID=$(execute_sql "SELECT id FROM webhook_configs WHERE url = '$WEBHOOK_URL';" | tr -d '\r')
execute_sql "INSERT INTO trigger_rules (webhookId, enabled) VALUES ($WEBHOOK_ID, 1);" || true
RULE_COUNT=$(execute_sql "SELECT COUNT(*) FROM trigger_rules WHERE webhookId = $WEBHOOK_ID;" | tr -d '\r')
if [ "$RULE_COUNT" -gt 0 ]; then
    echo "OK (rules: $RULE_COUNT)"
else
    echo "WARNING - No trigger rules found"
fi

# Test 6: Delete webhook (cascade delete trigger rules)
echo -n "Deleting webhook... "
execute_sql "DELETE FROM webhook_configs WHERE url = '$WEBHOOK_URL';"
sleep 1
FINAL_COUNT=$(count_table_rows "webhook_configs" || echo "0")
if [ "$FINAL_COUNT" -lt "$NEW_COUNT" ]; then
    echo "OK (final count: $FINAL_COUNT)"
else
    echo "FAILED - Delete did not work"
    exit 4
fi

# Output JSON result
cat <<EOF
{
  "test": "$TEST_NAME",
  "status": "passed",
  "operations": ["create", "read", "update", "delete"],
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
EOF

echo "========================================="
echo "Webhook CRUD test completed successfully"
echo "========================================="

exit 0

