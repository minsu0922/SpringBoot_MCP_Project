#!/usr/bin/env bash
# ============================================================
# 샘물교회 웹사이트 — 서버 최초 1회 초기 설정 스크립트
#
# 사용법 (서버에서 root 또는 sudo 권한으로 실행):
#   sudo bash init_server.sh
#
# 이 스크립트는 서버를 처음 세팅할 때 딱 한 번만 실행한다.
# 이후 배포는 deploy.sh 를 사용한다.
# ============================================================

set -euo pipefail

info()  { echo -e "\033[34m[INFO]\033[0m  $*"; }
ok()    { echo -e "\033[32m[OK]\033[0m    $*"; }

# ============================================================
# STEP 1. 전용 실행 계정 생성
# ============================================================
info "STEP 1 — 전용 계정(church) 생성"
if id "church" &>/dev/null; then
    echo "  이미 존재함, 건너뜀"
else
    useradd -r -m -s /sbin/nologin church
    ok "계정 생성 완료"
fi

# ============================================================
# STEP 2. 필수 디렉토리 생성 및 권한 설정
# ============================================================
info "STEP 2 — 디렉토리 생성"

# 앱 실행 디렉토리
mkdir -p /opt/church-website
chown church:church /opt/church-website
chmod 750 /opt/church-website

# 사역사진 업로드 디렉토리
mkdir -p /var/church/uploads/ministry
chown -R church:church /var/church
chmod -R 755 /var/church

# 로그 디렉토리
mkdir -p /var/log/church
chown church:church /var/log/church
chmod 750 /var/log/church

ok "디렉토리 생성 완료"

# ============================================================
# STEP 3. 환경변수 파일 초기화
# ============================================================
info "STEP 3 — 환경변수 파일 생성"
mkdir -p /etc/church-website

if [[ -f /etc/church-website/env ]]; then
    echo "  이미 존재함, 건너뜀 (기존 값 유지)"
else
    # deploy/church-website.env 를 복사해서 실제 값을 채워넣어야 함
    cat > /etc/church-website/env << 'EOF'
DB_USERNAME=church_user
DB_PASSWORD=여기에_실제_비밀번호_입력
KAKAO_MAP_API_KEY=여기에_실제_API키_입력
UPLOAD_DIR=/var/church/uploads/ministry
EOF
    # 민감 정보 파일 — 소유자만 읽기 가능
    chown church:church /etc/church-website/env
    chmod 600 /etc/church-website/env
    ok "환경변수 파일 생성: /etc/church-website/env"
    echo ""
    echo "  !! 반드시 아래 파일을 열어 실제 값을 입력하세요 !!"
    echo "  sudo nano /etc/church-website/env"
    echo ""
fi

# ============================================================
# STEP 4. systemd 서비스 등록
# ============================================================
info "STEP 4 — systemd 서비스 등록"

# 이 스크립트 위치에서 서비스 파일을 찾는다
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${SCRIPT_DIR}/church-website.service" ]]; then
    cp "${SCRIPT_DIR}/church-website.service" /etc/systemd/system/
    systemctl daemon-reload
    systemctl enable church-website  # 서버 재부팅 시 자동 시작 등록
    ok "서비스 등록 완료 (아직 시작하지 않음)"
else
    echo "  church-website.service 파일을 찾을 수 없습니다."
    echo "  수동으로 /etc/systemd/system/ 에 복사 후 아래 명령을 실행하세요:"
    echo "  sudo systemctl daemon-reload && sudo systemctl enable church-website"
fi

# ============================================================
# STEP 5. sudo 권한 설정 (church 계정이 서비스 제어 가능하도록)
# ============================================================
info "STEP 5 — sudo 권한 설정"
SUDOERS_FILE="/etc/sudoers.d/church-website"
if [[ ! -f "$SUDOERS_FILE" ]]; then
    cat > "$SUDOERS_FILE" << 'EOF'
# church 계정이 church-website 서비스만 제어할 수 있도록 허용
church ALL=(ALL) NOPASSWD: /bin/systemctl start church-website
church ALL=(ALL) NOPASSWD: /bin/systemctl stop church-website
church ALL=(ALL) NOPASSWD: /bin/systemctl restart church-website
church ALL=(ALL) NOPASSWD: /bin/journalctl -u church-website *
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
echo "     sudo nano /etc/church-website/env"
echo ""
echo "  2. JAR 파일을 서버로 전송한 뒤 서비스 시작"
echo "     (로컬에서) ./deploy/deploy.sh <서버IP>"
echo ""
echo "  3. 서비스 상태 확인"
echo "     sudo systemctl status church-website"
echo "     sudo journalctl -u church-website -f"
