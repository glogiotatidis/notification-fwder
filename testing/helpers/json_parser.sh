#!/bin/bash

# JSON parsing helpers (without external dependencies if possible)

# Extract value from JSON using grep/sed (simple cases)
json_get_value() {
    local json=$1
    local key=$2

    echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2- | tr -d '"' | tr -d ' '
}

# Check if jq is available
has_jq() {
    command -v jq &> /dev/null
}

# Parse JSON with jq if available, fallback to grep/sed
parse_json() {
    local json=$1
    local query=$2

    if has_jq; then
        echo "$json" | jq -r "$query"
    else
        # Fallback to simple parsing
        json_get_value "$json" "$query"
    fi
}

# Create JSON object
create_json() {
    local -n pairs=$1
    local json="{"
    local first=true

    for key in "${!pairs[@]}"; do
        if [ "$first" = true ]; then
            first=false
        else
            json+=","
        fi
        json+="\"$key\":\"${pairs[$key]}\""
    done

    json+="}"
    echo "$json"
}

# Pretty print JSON
pretty_json() {
    local json=$1

    if has_jq; then
        echo "$json" | jq '.'
    else
        echo "$json"
    fi
}

# Validate JSON
is_valid_json() {
    local json=$1

    if has_jq; then
        echo "$json" | jq '.' > /dev/null 2>&1
        return $?
    else
        # Basic validation
        [[ "$json" =~ ^\{.*\}$ ]] || [[ "$json" =~ ^\[.*\]$ ]]
    fi
}

