#!/usr/bin/env bash
# ============================================================
# 샘물교회 웹사이트 배포 스크립트
#
# 사용법:
#   ./deploy/deploy.sh <서버IP또는도메인>
#
# 예시:
#   ./deploy/deploy.sh 192.168.0.100
#   ./deploy/deploy.sh church.example.com
#
# 사전 조건:
#   - 로컬에 Java 21, Gradle 설치
#   - 서버에 SSH 키 인증 설정 완료
#   - 서버에서 1회성 초기 설정(init_server.sh)이 먼저 실행되어 있어야 함
# ============================================================

set -euo pipefail  # 오류 즉시 중단 / 미선언 변수 오류 / 파이프 오류 감지

# ── 설정 ──────────────────────────────────────────────────
SERVER="${1:?사용법: ./deploy.sh <서버IP>}"
SSH_USER="church"
APP_DIR="/opt/church-website"
JAR_NAME="church-website-0.0.1-SNAPSHOT.jar"
SERVICE_NAME="church-website"

# ── 색상 출력 헬퍼 ─────────────────────────────────────────
info()  { echo -e "\033[34m[INFO]\033[0m  $*"; }
ok()    { echo -e "\033[32m[OK]\033[0m    $*"; }
warn()  { echo -e "\033[33m[WARN]\033[0m  $*"; }
error() { echo -e "\033[31m[ERROR]\033[0m $*"; exit 1; }

# ============================================================
# STEP 1. 로컬 빌드
# ============================================================
info "STEP 1 — JAR 빌드 시작"
./gradlew bootJar -q
JAR_PATH="build/libs/${JAR_NAME}"

if [[ ! -f "$JAR_PATH" ]]; then
    error "빌드 실패: ${JAR_PATH} 파일이 없습니다."
fi
ok "빌드 완료: ${JAR_PATH}"

# ============================================================
# STEP 2. 서버에 JAR 전송
# ============================================================
info "STEP 2 — 서버(${SERVER})에 JAR 전송"
scp "$JAR_PATH" "${SSH_USER}@${SERVER}:${APP_DIR}/app-new.jar"
ok "전송 완료"

# ============================================================
# STEP 3. 서버에서 교체 및 재시작
# ============================================================
info "STEP 3 — 서비스 재시작"
ssh "${SSH_USER}@${SERVER}" bash << EOF
    set -euo pipefail

    # 이전 JAR를 롤백용으로 보관 (app.jar → app-prev.jar)
    if [[ -f "${APP_DIR}/app.jar" ]]; then
        mv "${APP_DIR}/app.jar" "${APP_DIR}/app-prev.jar"
        echo "  이전 버전 백업: app-prev.jar"
    fi

    # 새 JAR 활성화
    mv "${APP_DIR}/app-new.jar" "${APP_DIR}/app.jar"
    echo "  새 버전 배치 완료"

    # 서비스 재시작
    sudo systemctl restart ${SERVICE_NAME}
    echo "  서비스 재시작 요청 완료"

    # 정상 기동 대기 (최대 30초)
    echo "  기동 대기 중..."
    for i in {1..6}; do
        sleep 5
        if systemctl is-active --quiet ${SERVICE_NAME}; then
            echo "  서비스 정상 기동 확인 (${i}번째 시도)"
            exit 0
        fi
        echo "  아직 기동 중... (${i}/6)"
    done

    echo "[ERROR] 30초 내 기동 실패 — 로그를 확인하세요:"
    sudo journalctl -u ${SERVICE_NAME} -n 30 --no-pager
    exit 1
EOF

ok "배포 완료"

# ============================================================
# STEP 4. 헬스 체크
# ============================================================
info "STEP 4 — 헬스 체크"
sleep 3
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://${SERVER}:8080/" || echo "000")

if [[ "$HTTP_STATUS" == "200" ]]; then
    ok "헬스 체크 통과 (HTTP ${HTTP_STATUS})"
else
    warn "헬스 체크 응답: HTTP ${HTTP_STATUS}"
    warn "서버에서 로그를 확인하세요: sudo journalctl -u ${SERVICE_NAME} -f"
fi

echo ""
echo "=============================="
ok "배포 완료"
echo "=============================="
echo "  서버:      ${SERVER}"
echo "  로그 확인: ssh ${SSH_USER}@${SERVER} 'sudo journalctl -u ${SERVICE_NAME} -f'"
echo "  롤백:      ssh ${SSH_USER}@${SERVER} 'mv ${APP_DIR}/app-prev.jar ${APP_DIR}/app.jar && sudo systemctl restart ${SERVICE_NAME}'"
