#!/bin/bash
set -e

# Blue-Green Deployment Script for Frontend
# 무중단 배포: 3000 ↔ 3001 포트를 번갈아 사용

CONTAINER_NAME="wiki-beat-frontend"
BLUE_PORT=3000
GREEN_PORT=3001
NGINX_CONF="/etc/nginx/conf.d/wikibeat.conf"
NETWORK="shared-infra"
IMAGE="ghcr.io/zzjiho/wiki-beat-frontend:latest"

echo "=========================================="
echo "Frontend Zero-Downtime Deployment Started"
echo "=========================================="

# 0. 기존 -new 컨테이너 정리 (이전 배포 실패 시)
echo "Cleaning up any previous failed deployments..."
docker stop ${CONTAINER_NAME}-new 2>/dev/null || true
docker rm ${CONTAINER_NAME}-new 2>/dev/null || true

# 1. 현재 활성 포트 확인
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    CURRENT_PORT=$(docker port ${CONTAINER_NAME} | grep 80 | cut -d':' -f2 | head -1)
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
  -p ${NEW_PORT}:80 \
  --restart unless-stopped \
  ${IMAGE}

# 4. Health Check
echo ""
echo "[3/6] Waiting for health check..."
for i in {1..15}; do
    if curl -sf http://localhost:${NEW_PORT}/health > /dev/null 2>&1; then
        echo "✓ Health check passed! (attempt $i/15)"
        break
    fi

    if [ $i -eq 15 ]; then
        echo "✗ Health check failed after 15 attempts"
        echo "Rolling back..."
        docker stop ${CONTAINER_NAME}-new
        docker rm ${CONTAINER_NAME}-new
        exit 1
    fi

    echo "  Waiting... (attempt $i/15)"
    sleep 2
done

# 5. Nginx Upstream 변경
echo ""
echo "[4/6] Updating Nginx upstream to port ${NEW_PORT}..."
sed "s/server localhost:${OLD_PORT}/server localhost:${NEW_PORT}/g" ${NGINX_CONF} > /tmp/wikibeat.conf.tmp
sudo cp /tmp/wikibeat.conf.tmp ${NGINX_CONF}
rm /tmp/wikibeat.conf.tmp

# 6. Nginx Reload (무중단!)
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
echo "=========================================="
echo "✓ Deployment Completed Successfully!"
echo "  Active Port: ${NEW_PORT}"
echo "  Next deployment will use: ${OLD_PORT}"
echo "=========================================="
