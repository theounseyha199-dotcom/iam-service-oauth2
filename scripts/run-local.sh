#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."
if [[ ! -f .env.local ]]; then
  echo 'Create .env.local from .env.example and set your local secrets first.' >&2
  exit 1
fi
set -a
source .env.local
set +a
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
if [[ ",${SPRING_PROFILES_ACTIVE}," == *,local,* ]]; then
  docker compose --env-file .env.local --profile local up -d --wait mailpit
fi
exec ./gradlew :iam-main:bootRun
