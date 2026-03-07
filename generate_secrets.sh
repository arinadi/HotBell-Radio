#!/bin/bash

# Configuration
KEYSTORE_FILE="app/release.jks"
ALIAS="hotbell"
PASSWORD="hotbell_password" # Change this or use it as a default for now

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "Keystore $KEYSTORE_FILE already exists. Skipping generation."
else
    echo "Generating new keystore..."
    keytool -genkey -v -keystore "$KEYSTORE_FILE" -alias "$ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
      -storepass "$PASSWORD" -keypass "$PASSWORD" \
      -dname "CN=HotBell, OU=Dev, O=HotBell, L=Internet, S=Internet, C=HB"
fi

# Generate Base64
if [ -f "$KEYSTORE_FILE" ]; then
    BASE64_KEYSTORE=$(base64 -w 0 "$KEYSTORE_FILE")
    echo "------------------------------------------------------------"
    echo "GITHUB SECRETS CONFIGURATION"
    echo "------------------------------------------------------------"
    echo "SIGNING_KEY_STORE_BASE64: (copied to clipboard or shown below)"
    echo "$BASE64_KEYSTORE"
    echo ""
    echo "SIGNING_STORE_PASSWORD: $PASSWORD"
    echo "SIGNING_KEY_ALIAS: $ALIAS"
    echo "SIGNING_KEY_PASSWORD: $PASSWORD"
    echo "------------------------------------------------------------"
    echo "Please add these to your GitHub Repository Secrets."
    
    # Save to a temporary file for the user to copy easily
    echo "$BASE64_KEYSTORE" > /tmp/hotbell_keystore_base64.txt
    echo "Base64 also saved to /tmp/hotbell_keystore_base64.txt"
fi
