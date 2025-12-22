#!/usr/bin/env bash
set -euo pipefail

REQ_FILE="springboot/requirements.txt"
WHEEL_DIR="docker/wheels"

mkdir -p "$WHEEL_DIR"

# Retry up to 3 times to download wheels (handles transient network issues)
for i in 1 2 3; do
  echo "Downloading wheels (attempt $i)..."
  if pip3 download -r "$REQ_FILE" -d "$WHEEL_DIR"; then
    echo "Downloaded wheels to $WHEEL_DIR"
    exit 0
  else
    echo "Attempt $i failed, retrying in 5s..."
    sleep 5
  fi
done

echo "Failed to download wheels after 3 attempts" >&2
exit 1
