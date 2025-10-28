#!/bin/bash

# Script to generate a release keystore for Android app signing

set -e

echo "🔑 Android Release Keystore Generator"
echo "======================================"
echo ""

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "❌ Error: keytool not found. Please install JDK."
    exit 1
fi

# Get keystore information
read -p "Enter keystore filename (default: release.keystore): " KEYSTORE_FILE
KEYSTORE_FILE=${KEYSTORE_FILE:-release.keystore}

read -p "Enter key alias (default: release): " KEY_ALIAS
KEY_ALIAS=${KEY_ALIAS:-release}

read -sp "Enter keystore password: " KEYSTORE_PASSWORD
echo ""
read -sp "Confirm keystore password: " KEYSTORE_PASSWORD_CONFIRM
echo ""

if [ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_CONFIRM" ]; then
    echo "❌ Error: Passwords don't match"
    exit 1
fi

read -sp "Enter key password: " KEY_PASSWORD
echo ""
read -sp "Confirm key password: " KEY_PASSWORD_CONFIRM
echo ""

if [ "$KEY_PASSWORD" != "$KEY_PASSWORD_CONFIRM" ]; then
    echo "❌ Error: Passwords don't match"
    exit 1
fi

echo ""
echo "Generating keystore..."

# Generate keystore
keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD"

echo ""
echo "✅ Keystore generated successfully: $KEYSTORE_FILE"
echo ""
echo "📝 For GitHub Actions, encode your keystore to base64:"
echo "   base64 -i $KEYSTORE_FILE | tr -d '\\n'"
echo ""
echo "⚠️  Keep your keystore and passwords safe!"
echo "⚠️  Never commit the keystore to version control!"

