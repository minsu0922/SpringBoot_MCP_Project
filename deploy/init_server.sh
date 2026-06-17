#!/usr/bin/env bash
# ============================================================
# Spring Boot 서버 최초 초기 설정 스크립트 (템플릿)
#
# 사용법:
#   1. 아래 [설정 변수] 섹션을 프로젝트에 맞게 수정
#   2. 서버에서 실행: sudo bash init_server.sh
#
# 이 스크립트는 서버를 처음 세팅할 때 딱 한 번만 실행합니다.
# 이후 배포는 deploy-wsl.sh 를 사용합니다.
# ============================================================

set -euo pipefail

info()  { echo -e "\033[34m[INFO]\033[0m  $*"; }
ok()    { echo -e "\033[32m[OK]\033[0m    $*"; }

# ============================================================
# ★ 설정 변수 — 이 부분만 프로젝트에 맞게 수정하세요
# ============================================================
PROJECT_NAME="church"                          # 서비스명 (계정명·디렉토리명에 사용됩니다)
APP_DIR="/opt/church-website"                  # 앱 실행 디렉토리 (JAR 파일 위치)
UPLOAD_DIR="/var/church/uploads/ministry"      # 파일 업로드 저장 경로
LOG_DIR="/var/log/church"                      # 로그 디렉토리
ENV_DIR="/etc/church-website"                  # 환경변수 파일 디렉토리
SERVICE_FILE="church-website.service"          # systemd 서비스 파일명
# ============================================================

# ============================================================
# STEP 1. 전용 실행 계정 생성
# ============================================================
# -r : 시스템 계정으로 생성 (일반 로그인 불가)
# -s /sbin/nologin : SSH 직접 로그인 차단 (보안 강화)
info "STEP 1 — 전용 계정(${PROJECT_NAME}) 생성"
if id "${PROJECT_NAME}" &>/dev/null; then
    echo "  이미 존재함, 건너뜀"
else
    useradd -r -m -s /sbin/nologin "${PROJECT_NAME}"
    ok "계정 생성 완료"
fi

# ============================================================
# STEP 2. 필수 디렉토리 생성 및 권한 설정
# ============================================================
info "STEP 2 — 디렉토리 생성"

# 앱 실행 디렉토리 (JAR 파일이 위치할 경로)
mkdir -p "${APP_DIR}"
chown "${PROJECT_NAME}:${PROJECT_NAME}" "${APP_DIR}"
chmod 750 "${APP_DIR}"

# 파일 업로드 디렉토리
mkdir -p "${UPLOAD_DIR}"
chown -R "${PROJECT_NAME}:${PROJECT_NAME}" "/var/${PROJECT_NAME}"
chmod -R 755 "/var/${PROJECT_NAME}"

# 로그 디렉토리
mkdir -p "${LOG_DIR}"
chown "${PROJECT_NAME}:${PROJECT_NAME}" "${LOG_DIR}"
chmod 750 "${LOG_DIR}"

ok "디렉토리 생성 완료"

# ============================================================
# STEP 3. 환경변수 파일 초기화
# ============================================================
# 민감 정보(DB 비밀번호, API 키 등)를 소스코드와 분리해 관리합니다.
# chmod 600 으로 소유자만 읽을 수 있도록 보호합니다.
info "STEP 3 — 환경변수 파일 생성"
mkdir -p "${ENV_DIR}"

if [[ -f "${ENV_DIR}/env" ]]; then
    echo "  이미 존재함, 건너뜀 (기존 값 유지)"
else
    cat > "${ENV_DIR}/env" << 'EOF'
# 이 파일에 프로젝트에서 사용하는 환경변수를 입력하세요.
# 스크립트 실행 후 아래 명령으로 직접 편집합니다:
#   sudo nano /etc/[프로젝트명]/env
EOF
    chown "${PROJECT_NAME}:${PROJECT_NAME}" "${ENV_DIR}/env"
    chmod 600 "${ENV_DIR}/env"
    ok "환경변수 파일 생성: ${ENV_DIR}/env"
    echo ""
    echo "  !! 반드시 아래 파일을 열어 실제 값을 입력하세요 !!"
    echo "  sudo nano ${ENV_DIR}/env"
    echo ""
fi

# ============================================================
# STEP 4. systemd 서비스 등록
# ============================================================
# systemd 에 등록하면 서버 재부팅 시 자동으로 앱이 실행됩니다.
# 이 스크립트와 같은 폴더에 .service 파일이 있어야 합니다.
info "STEP 4 — systemd 서비스 등록"

# 이 스크립트 위치에서 서비스 파일을 찾는다
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/${SERVICE_FILE}" ]]; then
    cp "${SCRIPT_DIR}/${SERVICE_FILE}" /etc/systemd/system/
    systemctl daemon-reload
    systemctl enable "${PROJECT_NAME}"  # 서버 재부팅 시 자동 시작 등록
    ok "서비스 등록 완료 (아직 시작하지 않음)"
else
    echo "  ${SERVICE_FILE} 파일을 찾을 수 없습니다."
    echo "  수동으로 /etc/systemd/system/ 에 복사 후 아래 명령을 실행하세요:"
    echo "  sudo systemctl daemon-reload && sudo systemctl enable ${PROJECT_NAME}"
fi

# ============================================================
# STEP 5. sudo 권한 설정
# ============================================================
# 전용 계정이 서비스 시작·정지·로그 조회만 할 수 있도록 권한을 제한합니다.
# 전체 sudo 권한을 주지 않아 보안 사고 발생 시 피해 범위를 최소화합니다.
info "STEP 5 — sudo 권한 설정"
SUDOERS_FILE="/etc/sudoers.d/${PROJECT_NAME}"
if [[ ! -f "$SUDOERS_FILE" ]]; then
    cat > "$SUDOERS_FILE" << EOF
# ${PROJECT_NAME} 계정이 서비스 제어 명령만 실행할 수 있도록 허용
${PROJECT_NAME} ALL=(ALL) NOPASSWD: /bin/systemctl start ${PROJECT_NAME}
${PROJECT_NAME} ALL=(ALL) NOPASSWD: /bin/systemctl stop ${PROJECT_NAME}
${PROJECT_NAME} ALL=(ALL) NOPASSWD: /bin/systemctl restart ${PROJECT_NAME}
${PROJECT_NAME} ALL=(ALL) NOPASSWD: /bin/journalctl -u ${PROJECT_NAME} *
EOF
    chmod 440 "$SUDOERS_FILE"
    ok "sudo 권한 설정 완료"
fi

# ============================================================
# 완료
# ============================================================
echo ""
echo "=============================================="
ok "서버 초기 설정 완료"
echo "=============================================="
echo ""
echo "다음 단계:"
echo "  1. 환경변수 파일에 실제 값 입력"
echo "     sudo nano ${ENV_DIR}/env"
echo ""
echo "  2. JAR 파일을 서버로 전송한 뒤 서비스 시작"
echo "     sudo systemctl start ${PROJECT_NAME}"
echo ""
echo "  3. 서비스 상태 확인"
echo "     sudo systemctl status ${PROJECT_NAME}"
echo "     sudo journalctl -u ${PROJECT_NAME} -f"
