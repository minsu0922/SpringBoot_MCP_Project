#!/usr/bin/env bash
# ============================================================
# 샘물교회 웹사이트 배포 스크립트 (WSL 전용)
#
# 전제:
#   - IntelliJ에서 먼저 ./gradlew clean bootJar 로 빌드 완료
#   - 빌드 결과물: C:/deploy/app.jar (build.gradle bootJar 블록에서 경로 고정)
#
# 사용법:
#   bash deploy-wsl.sh
# ============================================================

set -euo pipefail



# ── 설정 ──────────────────────────────────────────────────
SERVER="54.180.20.104"
SSH_USER="ubuntu"
PEM="$HOME/.ssh/church-website.pem"
APP_DIR="/opt/church-website"
SERVICE_NAME="church-website"
JAR_PATH="/mnt/c/deploy/app.jar"

# ── 색상 출력 ──────────────────────────────────────────────
info()  { echo -e "\033[34m[INFO]\033[0m  $*"; }
ok()    { echo -e "\033[32m[OK]\033[0m    $*"; }
error() { echo -e "\033[31m[ERROR]\033[0m $*"; exit 1; }

# ── STEP 1. 빌드 결과물 확인 ───────────────────────────────
info "STEP 1 — 빌드 파일 확인"
if [[ ! -f "$JAR_PATH" ]]; then
    error "JAR 파일이 없습니다. IntelliJ에서 './gradlew clean bootJar' 먼저 실행하세요."
fi
ok "JAR 확인 완료"

# ── STEP 2. 서버로 전송 (홈 디렉토리 경유) ─────────────────
info "STEP 2 — 서버로 JAR 전송"
scp -i "$PEM" "$JAR_PATH" "${SSH_USER}@${SERVER}:~/app.jar"
ok "전송 완료"

# ── STEP 3. 교체 및 재시작 ─────────────────────────────────
info "STEP 3 — 서비스 교체 및 재시작"
ssh -i "$PEM" "${SSH_USER}@${SERVER}" bash << EOF
    set -e
    # 이전 버전 백업 (롤백용)
    if [[ -f "${APP_DIR}/app.jar" ]]; then
        sudo mv "${APP_DIR}/app.jar" "${APP_DIR}/app-prev.jar"
    fi
    sudo mv ~/app.jar "${APP_DIR}/app.jar"
    sudo chown church:church "${APP_DIR}/app.jar"
    sudo systemctl restart ${SERVICE_NAME}
EOF
ok "재시작 요청 완료"

# ── STEP 4. 헬스 체크 ──────────────────────────────────────
info "STEP 4 — 기동 대기 및 헬스 체크 (최대 60초)"
for i in {1..12}; do
    sleep 5
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://${SERVER}/" || echo "000")
    if [[ "$HTTP_STATUS" == "200" ]]; then
        ok "배포 성공! (HTTP ${HTTP_STATUS})"
        echo ""
        echo "  사이트: http://${SERVER}"
        echo "  롤백:   ssh -i \"$PEM\" ${SSH_USER}@${SERVER} 'sudo mv ${APP_DIR}/app-prev.jar ${APP_DIR}/app.jar && sudo systemctl restart ${SERVICE_NAME}'"
        exit 0
    fi
    echo "  기동 대기 중... (${i}/12, 현재 HTTP ${HTTP_STATUS})"
done

error "60초 내 기동 실패 — 서버 로그 확인: ssh -i \"$PEM\" ${SSH_USER}@${SERVER} 'sudo journalctl -u ${SERVICE_NAME} -n 50'"
