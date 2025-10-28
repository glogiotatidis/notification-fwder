#!/bin/bash

# Notification generator - creates test notifications on Android device

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/adb_helpers.sh"

# Generate a simple notification using ADB
generate_notification() {
    local package_name=$1
    local title=$2
    local text=$3
    local id=$4

    # Using adb shell to post notifications
    # Note: This requires a helper app or using Android's test notification system
    adb shell "cmd notification post -t '$title' '$package_name' 'tag_$id' '$text'"
}

# Generate multiple test notifications
generate_test_notifications() {
    echo "Generating test notifications..."

    generate_notification "com.test.app1" "Test Title 1" "This is test notification 1" 1
    sleep 1

    generate_notification "com.test.app2" "Test Title 2" "This is test notification 2" 2
    sleep 1

    generate_notification "com.urgent.app" "URGENT" "This is an urgent notification" 3
    sleep 1

    generate_notification "com.test.app1" "Another Test" "Low priority notification" 4
    sleep 1

    generate_notification "com.example.messenger" "New Message" "You have a new message from John" 5

    echo "Generated 5 test notifications"
}

# Generate notification with specific priority
generate_priority_notification() {
    local priority=$1  # -2 to 2
    local package="com.test.priority"

    # Note: Priority needs to be set through a proper Android app
    # This is a simplified version
    generate_notification "$package" "Priority $priority" "Test notification with priority $priority" "p$priority"
}

# Clear all notifications
clear_all_notifications() {
    adb shell "cmd notification post --clear-all"
}

# Main function
main() {
    if [ $# -eq 0 ]; then
        generate_test_notifications
    else
        case $1 in
            clear)
                clear_all_notifications
                ;;
            priority)
                generate_priority_notification "${2:-0}"
                ;;
            custom)
                generate_notification "${2:-com.test.app}" "${3:-Test}" "${4:-Text}" "${5:-1}"
                ;;
            *)
                echo "Usage: $0 [clear|priority <-2..2>|custom <package> <title> <text> <id>]"
                exit 1
                ;;
        esac
    fi
}

# Run if executed directly
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi

