#!/bin/bash

# Generate RSA key pair for JWT signing
# This script generates a 2048-bit RSA key pair

echo "Generating RSA key pair for JWT signing..."

# Create keys directory if it doesn't exist
mkdir -p src/main/resources/keys

# Generate private key
openssl genpkey -algorithm RSA -out src/main/resources/keys/private.pem -pkeyopt rsa_keygen_bits:2048

# Generate public key from private key
openssl rsa -pubout -in src/main/resources/keys/private.pem -out src/main/resources/keys/public.pem

# Set proper permissions
chmod 600 src/main/resources/keys/private.pem
chmod 644 src/main/resources/keys/public.pem

echo "RSA key pair generated successfully!"
echo "Private key: src/main/resources/keys/private.pem"
echo "Public key: src/main/resources/keys/public.pem"
echo ""
echo "IMPORTANT: Keep the private key secure and never commit it to version control!"
