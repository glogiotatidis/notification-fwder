#!/bin/bash

# Main test orchestrator for Notification Forwarder testing framework
# This script runs all tests sequentially and reports results

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="$LOG_DIR/test_run_$TIMESTAMP.log"
RESULTS_FILE="$LOG_DIR/test_results_$TIMESTAMP.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Create logs directory
mkdir -p "$LOG_DIR"

echo "=================================="
echo "Notification Forwarder Test Suite"
echo "=================================="
echo "Timestamp: $(date)"
echo "Log file: $LOG_FILE"
echo ""

# Source helper functions
source "$SCRIPT_DIR/helpers/adb_helpers.sh"

# Initialize results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

declare -a TEST_RESULTS

# Function to run a single test
run_test() {
    local test_script=$1
    local test_name=$(basename "$test_script" .sh)
    
    echo -n "Running $test_name... "
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    local start_time=$(date +%s)
    
    if [ -x "$test_script" ]; then
        if "$test_script" >> "$LOG_FILE" 2>&1; then
            local end_time=$(date +%s)
            local duration=$((end_time - start_time))
            echo -e "${GREEN}PASSED${NC} (${duration}s)"
            PASSED_TESTS=$((PASSED_TESTS + 1))
            TEST_RESULTS+=("{\"name\":\"$test_name\",\"status\":\"passed\",\"duration\":$duration}")
        else
            local exit_code=$?
            local end_time=$(date +%s)
            local duration=$((end_time - start_time))
            echo -e "${RED}FAILED${NC} (exit code: $exit_code, ${duration}s)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
            TEST_RESULTS+=("{\"name\":\"$test_name\",\"status\":\"failed\",\"exit_code\":$exit_code,\"duration\":$duration}")
        fi
    else
        echo -e "${YELLOW}SKIPPED${NC} (not executable)"
        SKIPPED_TESTS=$((SKIPPED_TESTS + 1))
        TEST_RESULTS+=("{\"name\":\"$test_name\",\"status\":\"skipped\"}")
    fi
}

# Run setup
echo "Setting up test environment..."
if [ -x "$SCRIPT_DIR/setup.sh" ]; then
    "$SCRIPT_DIR/setup.sh" >> "$LOG_FILE" 2>&1 || {
        echo -e "${RED}Setup failed! Aborting tests.${NC}"
        exit 2
    }
fi
echo ""

# Check device connection
if ! check_device_connected; then
    echo -e "${RED}Error: No Android device connected${NC}"
    echo "Please connect a device and enable USB debugging"
    exit 3
fi

echo "Device connected: $(adb shell getprop ro.product.model)"
echo ""

# Run all tests in order
echo "Running tests..."
echo ""

for test_script in "$SCRIPT_DIR"/tests/*.sh; do
    if [ -f "$test_script" ]; then
        run_test "$test_script"
    fi
done

echo ""
echo "=================================="
echo "Test Results Summary"
echo "=================================="
echo -e "Total:   $TOTAL_TESTS"
echo -e "Passed:  ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:  ${RED}$FAILED_TESTS${NC}"
echo -e "Skipped: ${YELLOW}$SKIPPED_TESTS${NC}"
echo ""

# Generate JSON results
cat > "$RESULTS_FILE" <<EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "total": $TOTAL_TESTS,
  "passed": $PASSED_TESTS,
  "failed": $FAILED_TESTS,
  "skipped": $SKIPPED_TESTS,
  "tests": [
    $(IFS=,; echo "${TEST_RESULTS[*]}")
  ]
}
EOF

echo "Results saved to: $RESULTS_FILE"
echo "Detailed logs: $LOG_FILE"
echo ""

# Run teardown
if [ -x "$SCRIPT_DIR/teardown.sh" ]; then
    echo "Running teardown..."
    "$SCRIPT_DIR/teardown.sh" >> "$LOG_FILE" 2>&1
fi

# Exit with appropriate code
if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi

