# Test Keys Directory

This directory should contain test RSA keys for JWT signing during testing.

## Generating Test Keys

You can generate test keys using the script in the project root:

```bash
# Make the script executable
chmod +x scripts/generate-keys.sh

# Generate test keys (this will create keys in src/main/resources/keys/)
./scripts/generate-keys.sh

# Copy keys to test directory
cp src/main/resources/keys/private.pem src/test/resources/keys/test-private.pem
cp src/main/resources/keys/public.pem src/test/resources/keys/test-public.pem
```

## Alternative: Generate Simple Test Keys

For testing purposes, you can also generate simple keys using OpenSSL:

```bash
# Create test keys directory
mkdir -p src/test/resources/keys

# Generate private key
openssl genpkey -algorithm RSA -out src/test/resources/keys/test-private.pem -pkeyopt rsa_keygen_bits:2048

# Generate public key
openssl rsa -pubout -in src/test/resources/keys/test-private.pem -out src/test/resources/keys/test-public.pem
```

## Note

These are test keys and should never be used in production. The actual production keys should be generated separately and kept secure.
