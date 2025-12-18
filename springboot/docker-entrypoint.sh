#!/usr/bin/env bash
set -e

# Wait for a TCP service
wait_for(){
  host=$1
  port=$2
  echo "Waiting for ${host}:${port} ..."
  until nc -z ${host} ${port}; do
    sleep 1
  done
}

# Wait for services
wait_for ${WAIT_MYSQL_HOST:-mysql} ${WAIT_MYSQL_PORT:-3306}
wait_for ${WAIT_NEO4J_HOST:-neo4j} ${WAIT_NEO4J_PORT:-7687}

# Start the app
echo "Starting Spring Boot app..."
java -jar /app/app.jar &
APP_PID=$!

# Wait for the app to open the port
echo "Waiting for application to open port ${APP_PORT:-9090}..."
until nc -z localhost ${APP_PORT:-9090}; do
  sleep 1
done

# Trigger graph rebuild (retry a few times)
RETRIES=10
for i in $(seq 1 $RETRIES); do
  echo "Attempting to trigger /article/rebuild (try $i/$RETRIES)"
  if curl -s -X POST http://localhost:${APP_PORT:-9090}/article/rebuild; then
    echo "Rebuild triggered"
    break
  fi
  sleep 3
done

wait $APP_PID
