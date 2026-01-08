#!/bin/bash
set -e

# Blue-Green Deployment Script for Backend (8888 ↔ 8889 포트)

CONTAINER_NAME="wiki-beat-backend"
BLUE_PORT=8888
GREEN_PORT=8889
NGINX_CONF="/etc/nginx/conf.d/wikibeat.conf"
NETWORK="shared-infra"
IMAGE="ghcr.io/zzjiho/wiki-beat-back:latest"

echo "========================================="
echo "Backend Zero-Downtime Deployment Started"
echo "========================================="

# 0. 기존 -new 컨테이너 정리 (이전 배포 실패 시)
echo "Cleaning up any previous failed deployments..."
docker stop ${CONTAINER_NAME}-new 2>/dev/null || true
docker rm ${CONTAINER_NAME}-new 2>/dev/null || true

# 1. 현재 활성 포트 확인
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    CURRENT_PORT=$(docker port ${CONTAINER_NAME} | grep 8888 | cut -d':' -f2)
    echo "✓ Current active port: ${CURRENT_PORT}"

    if [ "$CURRENT_PORT" = "$BLUE_PORT" ]; then
        NEW_PORT=$GREEN_PORT
        OLD_PORT=$BLUE_PORT
    else
        NEW_PORT=$BLUE_PORT
        OLD_PORT=$GREEN_PORT
    fi
else
    # 첫 배포 시 BLUE부터 시작
    NEW_PORT=$BLUE_PORT
    OLD_PORT=$GREEN_PORT
    echo "✓ First deployment - starting with port ${NEW_PORT}"
fi

echo "→ Deploying to port: ${NEW_PORT}"
echo "→ Current port: ${OLD_PORT}"

# 2. 새 이미지 Pull
echo ""
echo "[1/6] Pulling latest image..."
docker pull ${IMAGE}

# 3. 새 포트로 컨테이너 시작
echo ""
echo "[2/6] Starting new container on port ${NEW_PORT}..."
docker run -d \
  --name ${CONTAINER_NAME}-new \
  --network ${NETWORK} \
  -p ${NEW_PORT}:8888 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e KAFKA_BOOTSTRAP_SERVERS=shared-kafka:29092 \
  -e REDIS_HOST=shared-redis \
  -e REDIS_PORT=6379 \
  -e REDIS_PASSWORD=${REDIS_PASSWORD} \
  --add-host=host.docker.internal:host-gateway \
  --restart unless-stopped \
  ${IMAGE}

# 4. Health Check
echo ""
echo "[3/6] Waiting for health check..."
for i in {1..30}; do
    if curl -sf http://localhost:${NEW_PORT}/api/health-check > /dev/null 2>&1; then
        echo "✓ Health check passed! (attempt $i/30)"
        break
    fi

    if [ $i -eq 30 ]; then
        echo "✗ Health check failed after 30 attempts"
        echo "Rolling back..."
        docker stop ${CONTAINER_NAME}-new
        docker rm ${CONTAINER_NAME}-new
        exit 1
    fi

    echo "  Waiting... (attempt $i/30)"
    sleep 5
done

# 5. Nginx Upstream 변경
echo ""
echo "[4/6] Updating Nginx upstream to port ${NEW_PORT}..."
sed "s/server localhost:${OLD_PORT}/server localhost:${NEW_PORT}/g" ${NGINX_CONF} > /tmp/wikibeat.conf.tmp
sudo cp /tmp/wikibeat.conf.tmp ${NGINX_CONF}
rm /tmp/wikibeat.conf.tmp

# 6. Nginx Reload
echo ""
echo "[5/6] Reloading Nginx (zero-downtime)..."
sudo nginx -t && sudo nginx -s reload

# 7. 기존 컨테이너 정리
echo ""
echo "[6/6] Cleaning up old container..."
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    docker stop ${CONTAINER_NAME}
    docker rm ${CONTAINER_NAME}
fi

# 8. 새 컨테이너 이름 변경
docker rename ${CONTAINER_NAME}-new ${CONTAINER_NAME}

# 9. 오래된 이미지 정리
docker image prune -f

echo ""
echo "========================================="
echo "✓ Deployment Completed Successfully!"
echo "  Active Port: ${NEW_PORT}"
echo "  Next deployment will use: ${OLD_PORT}"
echo "========================================="
