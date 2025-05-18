#!/bin/bash
set -e

export VAULT_ADDR=http://vault:8200
export VAULT_TOKEN=root

until curl -s "$VAULT_ADDR/v1/sys/health"; do
  echo "Waiting for Vault..."
  sleep 2
done

JWT_SECRET=$(openssl rand -base64 64)

vault login $VAULT_TOKEN >/dev/null

vault kv put secret/acme-platform jwt.secret="$JWT_SECRET"

echo "Vault initialized with JWT secret."
