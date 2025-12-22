#!/usr/bin/env bash
set -euo pipefail

REQ_FILE="springboot/requirements.txt"
TARGETS=("docker/wheels" "springboot/wheels")

for dir in "${TARGETS[@]}"; do
  mkdir -p "$dir"
done

# Retry up to 3 times to download wheels (handles transient network issues)
for i in 1 2 3; do
  echo "Downloading wheels (attempt $i)..."
  if pip3 download -r "$REQ_FILE" -d "${TARGETS[0]}"; then
    # Mirror to springboot/wheels for Dockerfile COPY wheels /app/wheels
    rsync -a --delete "${TARGETS[0]}/" "${TARGETS[1]}/"
    echo "Downloaded wheels to ${TARGETS[*]}"
    exit 0
  else
    echo "Attempt $i failed, retrying in 5s..."
    sleep 5
  fi
done

echo "Failed to download wheels after 3 attempts" >&2
exit 1
