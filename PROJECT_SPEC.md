# 샘물교회 웹사이트 프로젝트 명세서

> 최종 작성일: 2026-04-28

---

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트명 | 샘물교회 공식 웹사이트 |
| 목적 | 교회 공개 정보 제공 + 관리자 콘텐츠 관리 시스템(CMS) |
| 서버 포트 | 8080 (기본값) |
| 빌드 도구 | Gradle 9.3 |
| 그룹 ID | `com.church` |
| 아티팩트 ID | `church-website` |
| 버전 | `0.0.1-SNAPSHOT` |

---

## 2. 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 (LTS) | 런타임 언어 |
| Spring Boot | 4.0.2 | 애플리케이션 프레임워크 |
| Spring MVC | (Boot 내장) | HTTP 요청 처리 |
| Spring Security | (Boot 내장) | 인증·인가 |
| Spring Data JPA | (Boot 내장) | ORM / 데이터 접근 |
| Hibernate | (JPA 구현체) | SQL 생성 / 영속성 |
| QueryDSL | 5.1.0 (Jakarta) | 동적 쿼리 빌더 |
| Bean Validation | (Boot 내장) | 입력값 검증 |
| Lombok | (latest) | 보일러플레이트 제거 |

### Frontend
| 기술 | 용도 |
|------|------|
| Thymeleaf | 서버사이드 템플릿 엔진 |
| thymeleaf-extras-springsecurity6 | Thymeleaf Spring Security 통합 |
| Vanilla JS / CSS | 클라이언트 인터랙션 |
| Kakao Maps API | 오시는 길 지도 표시 |
| Google Fonts (Noto Sans KR) | 한글 폰트 |

### Database
| 기술 | 용도 |
|------|------|
| MySQL | 운영 DB (`church_db`) |

### 인프라 / 운영
| 기술 | 용도 |
|------|------|
| Nginx (권장) | 리버스 프록시 (X-Forwarded-For 처리) |
| 환경변수 | DB 자격증명, API 키, 업로드 경로 주입 |

---

## 3. 프로젝트 구조

```
SpringBoot_MCP_Project/
├── build.gradle
├── PROJECT_SPEC.md
├── CLAUDE.md
├── src/
│   └── main/
│       ├── java/com/church/website/
│       │   ├── ChurchWebsiteApplication.java       # 진입점
│       │   ├── config/
│       │   │   ├── DataInitializer.java             # 앱 시작 시 기본 데이터 삽입
│       │   │   ├── GlobalExceptionHandler.java      # 전역 예외 처리 (404/500)
│       │   │   ├── QueryDslConfig.java              # JPAQueryFactory 빈 등록
│       │   │   ├── RequestLoggingInterceptor.java   # 요청/응답 로그 인터셉터
│       │   │   ├── SecurityConfig.java              # Spring Security 설정
│       │   │   └── WebMvcConfig.java                # 리소스 핸들러, 인터셉터 등록
│       │   ├── controller/
│       │   │   ├── BaseController.java              # run() / runWithModel() 헬퍼
│       │   │   ├── AdminController.java             # /admin/** 관리자 CRUD
│       │   │   ├── MainController.java              # 공개 페이지 라우팅
│       │   │   ├── NewFamilyController.java         # /new-family 방문자 등록 폼
│       │   │   ├── LoginController.java             # /login 페이지
│       │   │   └── FaviconController.java           # /favicon.ico 처리
│       │   ├── entity/
│       │   │   ├── Notice.java
│       │   │   ├── NewFamily.java
│       │   │   ├── MinistryPhoto.java
│       │   │   ├── MainImage.java
│       │   │   ├── Location.java
│       │   │   ├── Sermon.java
│       │   │   └── User.java
│       │   ├── repository/
│       │   │   ├── NoticeRepository.java / NoticeRepositoryCustom / NoticeRepositoryImpl
│       │   │   ├── NewFamilyRepository.java / Custom / Impl
│       │   │   ├── SermonRepository.java / Custom / Impl
│       │   │   ├── MinistryPhotoRepository.java
│       │   │   ├── MainImageRepository.java
│       │   │   ├── LocationRepository.java
│       │   │   └── UserRepository.java
│       │   ├── service/
│       │   │   ├── NoticeService.java
│       │   │   ├── NewFamilyService.java
│       │   │   ├── MinistryPhotoService.java
│       │   │   ├── MainImageService.java
│       │   │   ├── LocationService.java
│       │   │   ├── SermonService.java
│       │   │   ├── UserService.java
│       │   │   └── CustomUserDetailsService.java    # Spring Security UserDetailsService
│       │   └── exception/
│       │       └── EntityNotFoundException.java
│       └── resources/
│           ├── application.properties               # 공통 설정
│           ├── application-prod.properties          # 운영 환경 오버라이드
│           ├── application-local.properties.example # 로컬 환경 예시 (gitignore)
│           ├── static/
│           │   ├── css/, js/, images/
│           │   └── uploads/                         # 개발 환경 파일 업로드 (운영은 외부 경로)
│           └── templates/
│               ├── index.html                       # 홈
│               ├── login.html
│               ├── location.html
│               ├── about/        church, pastor, staff
│               ├── worship/      schedule, department
│               ├── notice/       list, detail
│               ├── ministry/     index
│               ├── sermon/       list, detail
│               ├── new-family/   form, complete
│               ├── error/        404, 500
│               └── admin/
│                   ├── fragments/layout.html        # 관리자 공통 레이아웃
│                   ├── index.html                   # 대시보드
│                   ├── notice/       list, form
│                   ├── ministry/     list, form
│                   ├── new-family/   list, detail
│                   ├── sermon/       list, form
│                   ├── account/      list
│                   └── settings.html
```

---

## 4. 아키텍처 레이어

```
HTTP Request
    │
    ▼
[Spring Security Filter Chain]
    │  ├─ 인증 (CustomUserDetailsService)
    │  └─ 인가 (/admin/** → ROLE_ADMIN)
    ▼
[Controller Layer]
    │  BaseController.run() / runWithModel() 로 예외 처리 통일
    │  ├─ AdminController   (/admin/**)
    │  ├─ MainController    (공개 페이지)
    │  └─ NewFamilyController (/new-family)
    ▼
[Service Layer]
    │  비즈니스 로직 + @Transactional 처리
    │  파일 업로드/삭제 (DB 성공 후 파일 조작)
    ▼
[Repository Layer]
    │  Spring Data JPA (메서드명 쿼리)
    │  QueryDSL (동적 검색 — Custom + Impl 패턴)
    ▼
[MySQL (church_db)]
```

### 예외 처리 흐름
```
GET 핸들러 예외  → GlobalExceptionHandler → error/404 or error/500 뷰
POST 핸들러 예외 → BaseController.run()  → Flash 메시지 + redirect
  ├─ IllegalArgumentException → 사용자 메시지 노출 (비즈니스 규칙 위반)
  └─ 기타 Exception           → "처리 중 오류가 발생했습니다." (내부 메시지 은닉)
```

---

## 5. 엔티티 설계

### Notice (공지사항)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| title | VARCHAR(200) | NOT NULL | |
| content | TEXT | NOT NULL | |
| author | VARCHAR | NOT NULL | 작성자 (로그인 username) |
| createdAt | DATETIME | NOT NULL | @PrePersist 자동 설정 |
| updatedAt | DATETIME | NULL | @PreUpdate 자동 갱신 |
| viewCount | INT | NOT NULL, DEFAULT 0 | |
| popup | BOOLEAN | NOT NULL, DEFAULT false | 메인 팝업 여부 |
| popupStartDate | DATETIME | NULL | 팝업 노출 시작 |
| popupEndDate | DATETIME | NULL | 팝업 노출 종료 |

### NewFamily (새가족 등록)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| name | VARCHAR(50) | NOT NULL | @NotBlank 검증 |
| phone | VARCHAR(20) | NOT NULL | @Pattern `^\d{2,3}-\d{3,4}-\d{4}$` |
| visitRoute | VARCHAR(100) | NULL | |
| memo | TEXT | NULL | 방문자 메모 |
| adminMemo | TEXT | NULL | 관리자 내부 메모 |
| checked | BOOLEAN | NOT NULL, DEFAULT false | 확인 처리 여부 |
| createdAt | DATETIME | NOT NULL | |

### MinistryPhoto (사역 사진)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| category | VARCHAR(50) | NOT NULL | WORSHIP / CHILDREN / YOUTH / MISSION / COMMUNITY / ETC |
| title | VARCHAR(100) | NOT NULL | |
| description | VARCHAR(500) | NULL | |
| photoUrl | VARCHAR(500) | NOT NULL | `/uploads/ministry/{uuid}.ext` |
| displayOrder | INT | NOT NULL, DEFAULT 0 | 낮을수록 앞에 표시 |
| isActive | BOOLEAN | NOT NULL, DEFAULT true | 공개 여부 |
| createdAt | DATETIME | NOT NULL | |
| updatedAt | DATETIME | NULL | |

### Sermon (설교 동영상)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| title | VARCHAR(200) | NOT NULL | |
| preacher | VARCHAR(100) | NOT NULL | |
| sermonDate | DATE | NOT NULL | |
| biblePassage | VARCHAR(200) | NOT NULL | 성경 본문 |
| videoUrl | VARCHAR | NULL | `/uploads/sermon/video-{uuid}.ext` |
| thumbnailUrl | VARCHAR | NULL | `/uploads/sermon/thumb-{uuid}.ext` |
| description | TEXT | NULL | |
| createdAt | DATETIME | NOT NULL | |

### MainImage (메인 슬라이드 이미지)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| title | VARCHAR | NOT NULL | |
| description | VARCHAR | NULL | |
| imageUrl | VARCHAR | NOT NULL | |
| displayOrder | INT | NOT NULL | |
| isActive | BOOLEAN | NOT NULL | |
| createdAt | DATETIME | NOT NULL | |
| updatedAt | DATETIME | NULL | |

### Location (교회 위치 정보)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| churchName | VARCHAR(100) | NOT NULL | |
| address | VARCHAR(200) | NOT NULL | |
| latitude | DOUBLE | NULL | 위도 (Kakao Maps) |
| longitude | DOUBLE | NULL | 경도 |
| phone / fax / email | VARCHAR | NULL | |
| subwayInfo / busInfo / carInfo / parkingInfo | TEXT | NULL | 교통 안내 |
| isActive | BOOLEAN | NOT NULL | 단일 활성 레코드 |
| createdAt / updatedAt | DATETIME | | |

### User (관리자 계정)
| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| id | BIGINT | PK, AUTO | |
| username | VARCHAR | UNIQUE, NOT NULL | |
| password | VARCHAR | NOT NULL | BCrypt 해싱 |
| role | VARCHAR | NOT NULL | `ROLE_ADMIN` 고정 |
| enabled | BOOLEAN | NOT NULL, DEFAULT true | |
| createdAt | DATETIME | NOT NULL | |

---

## 6. URL 맵핑

### 공개 페이지
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/` | 홈 (슬라이드, 공지, 설교, 사역, 지도) |
| GET | `/about/church` | 교회 소개 |
| GET | `/about/pastor` | 담임목사 소개 |
| GET | `/about/staff` | 교역자 소개 |
| GET | `/worship/schedule` | 예배 시간 안내 |
| GET | `/worship/department` | 부서별 예배 |
| GET | `/notice/list?page={n}` | 공지사항 목록 (페이지네이션) |
| GET | `/notice/detail/{id}` | 공지사항 상세 + 조회수 증가 |
| GET | `/ministry` | 사역소개 갤러리 |
| GET | `/sermon?biblePassage=&page={n}` | 설교 동영상 목록 |
| GET | `/sermon/{id}` | 설교 동영상 상세/재생 |
| GET | `/location` | 오시는 길 (Kakao Maps) |
| GET | `/new-family` | 새가족 등록 폼 |
| POST | `/new-family/submit` | 새가족 등록 처리 |
| GET | `/new-family/complete` | 등록 완료 페이지 |
| GET | `/login` | 로그인 페이지 |
| POST | `/logout` | 로그아웃 |

### 관리자 (`/admin/**`, ROLE_ADMIN 필요)
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/admin` | 대시보드 |
| GET | `/admin/notices?keyword=&popupStatus=` | 공지사항 목록 (검색) |
| GET | `/admin/notices/new` | 공지사항 등록 폼 |
| GET | `/admin/notices/edit/{id}` | 공지사항 수정 폼 |
| POST | `/admin/notices/save` | 공지사항 저장 (등록/수정) |
| POST | `/admin/notices/delete/{id}` | 공지사항 삭제 |
| GET | `/admin/ministry` | 사역사진 목록 (카테고리 탭) |
| GET | `/admin/ministry/new` | 사역사진 등록 폼 |
| GET | `/admin/ministry/edit/{id}` | 사역사진 수정 폼 |
| POST | `/admin/ministry/save` | 사역사진 저장 (등록/수정, 파일 업로드) |
| POST | `/admin/ministry/delete/{id}` | 사역사진 삭제 |
| GET | `/admin/new-family?keyword=&status=&page={n}` | 새가족 목록 (검색+페이지) |
| GET | `/admin/new-family/{id}` | 새가족 상세 |
| POST | `/admin/new-family/check/{id}` | 확인 처리 |
| POST | `/admin/new-family/uncheck/{id}` | 확인 취소 |
| POST | `/admin/new-family/admin-memo/{id}` | 관리자 메모 저장 |
| POST | `/admin/new-family/delete/{id}` | 새가족 삭제 |
| GET | `/admin/sermon?biblePassage=&page={n}` | 설교 목록 |
| GET | `/admin/sermon/new` | 설교 등록 폼 |
| GET | `/admin/sermon/edit/{id}` | 설교 수정 폼 |
| POST | `/admin/sermon/save` | 설교 저장 (동영상/썸네일 업로드) |
| POST | `/admin/sermon/delete/{id}` | 설교 삭제 |
| GET | `/admin/account` | 계정 목록 + 비밀번호 변경 + 계정 추가 |
| POST | `/admin/account/change-password` | 비밀번호 변경 |
| POST | `/admin/account/create` | 계정 추가 |
| POST | `/admin/account/toggle/{id}` | 계정 활성/비활성 토글 |
| POST | `/admin/account/delete/{id}` | 계정 삭제 |
| GET | `/admin/settings` | 교회 정보 설정 폼 |
| POST | `/admin/settings/save` | 교회 정보 저장 |

---

## 7. 보안 설계

### 인증·인가
- **Form Login** 방식, 로그인 페이지: `/login`
- 로그인 성공 → `/admin` 리다이렉트
- 로그인 실패 → `/login?error=true`
- `/admin/**` — `ROLE_ADMIN` 필수
- 그 외 모든 경로 — 공개 허용
- 동시 세션 1개 제한, 만료 시 `/login?expired=true`
- 로그아웃 후 세션 무효화 + `JSESSIONID` 쿠키 삭제

### 비밀번호 정책
- 최소 8자, 대문자·소문자·숫자 각 1개 이상 포함
- BCrypt 해싱 (PasswordEncoder)
- 서버·클라이언트(HTML `pattern` 속성) 양쪽 검증

### 파일 업로드 보안
| 파일 유형 | 허용 확장자 | 허용 MIME |
|-----------|-------------|-----------|
| 사역사진 | .jpg .jpeg .png .gif .webp | image/* |
| 설교 동영상 | .mp4 .webm .mov .avi .mkv | video/* |
| 설교 썸네일 | .jpg .jpeg .png .gif .webp | image/* |
- UUID 기반 파일명 저장 (원본명 사용 안 함)
- 경로 traversal 방지: URL prefix 검증 후 삭제

### HTTP 보안 헤더
- `Cache-Control: no-cache, no-store, max-age=0, must-revalidate` — 로그아웃 후 백버튼 방지
- CSRF 보호: Thymeleaf `th:action` 자동 토큰 삽입

### 로그 보안
- 로그인 실패 시 username 미기록 (사용자 열거 공격 방지)

---

## 8. 파일 업로드 / 정적 자원

### 업로드 경로 설정
| 환경 | 사역사진 | 설교 파일 |
|------|---------|---------|
| 개발 | `{user.dir}/src/main/resources/static/uploads/ministry` | `.../uploads/sermon` |
| 운영 | `${UPLOAD_DIR:/var/church/uploads/ministry}` | `${SERMON_UPLOAD_DIR:/var/church/uploads/sermon}` |

### 정적 자원 서빙 (WebMvcConfig)
- `/uploads/ministry/**` → 파일시스템 `file:{ministryUploadDir}/`
- `/uploads/sermon/**` → 파일시스템 `file:{sermonUploadDir}/`
- `/css/**, /js/**, /images/**` → classpath 기본 처리

### 파일 삭제 정책
- DB 삭제/갱신 성공 후 구 파일 삭제 (DB 실패 시 파일 보존)
- IO 오류는 warn 로그만 기록하고 비즈니스 흐름 중단 안 함

---

## 9. QueryDSL 적용 현황

| Repository | 동적 쿼리 내용 |
|------------|---------------|
| `NoticeRepositoryImpl` | keyword(제목 like) + popupStatus(on/off) 조합 검색 |
| `NewFamilyRepositoryImpl` | keyword(이름 like) + status(checked/unchecked) + 페이지네이션 |
| `SermonRepositoryImpl` | biblePassage(대소문자 무시 like) + 페이지네이션 |

공통 패턴: `BooleanBuilder` → 조건이 없으면 전체 조회, 있으면 AND 추가.

---

## 10. 환경별 설정

### 필수 환경변수
| 변수명 | 설명 | 예시 |
|--------|------|------|
| `DB_USERNAME` | MySQL 사용자명 | `church_user` |
| `DB_PASSWORD` | MySQL 비밀번호 | |
| `KAKAO_MAP_API_KEY` | 카카오맵 JavaScript 키 | |
| `UPLOAD_DIR` | 사역사진 업로드 경로 (운영) | `/var/church/uploads/ministry` |
| `SERMON_UPLOAD_DIR` | 설교 파일 업로드 경로 (운영) | `/var/church/uploads/sermon` |

### 프로파일별 주요 설정 차이
| 설정 | 개발 (default) | 운영 (prod) |
|------|--------------|------------|
| `ddl-auto` | update | validate |
| `show-sql` | true | false |
| `thymeleaf.cache` | false | true |
| 파일 업로드 경로 | src 디렉토리 내 | 외부 경로 |
| 로그 레벨 | DEBUG | INFO/WARN |

### 실행 방법
```bash
# 개발 환경 (application-local.properties 필요)
./gradlew bootRun

# 운영 환경
java -jar church-website.jar --spring.profiles.active=prod
```

---

## 11. 데이터 초기화 (DataInitializer)

앱 시작 시 `ApplicationRunner`로 실행:

1. **DDL 보정** — `location.latitude / longitude` 컬럼을 NULL 허용으로 변경  
   (JPA `ddl-auto=update`가 기존 컬럼 nullable 변경을 지원하지 않으므로 JDBC로 직접 처리)

2. **기본 데이터 삽입** — `location` 테이블이 비어있으면 샘물교회 기본 정보 삽입  
   (최초 배포 직후에도 오시는길 지도가 표시되도록 하는 fallback)

---

## 12. 요청 로깅 (RequestLoggingInterceptor)

모든 HTTP 요청을 `preHandle` / `afterCompletion`으로 인터셉트하여 기록:
- 정상 요청: `[IP] METHOD URI → STATUS (Nms)` — INFO 레벨
- 예외 요청: 동일 포맷 — ERROR 레벨
- Nginx 등 프록시 경유 시 `X-Forwarded-For` 헤더에서 실제 클라이언트 IP 추출
- 정적 자원(`/css/**, /js/**, /images/**, /uploads/**`)은 로깅 제외

---

## 13. 알려진 제한 사항 / 미구현 기능

| 항목 | 설명 |
|------|------|
| MainImage 관리자 UI 없음 | `MainImage` 엔티티·서비스·레포지토리는 구현되어 있으나 관리자 CRUD 화면 미구현. 현재는 DB 직접 조작 필요. |
| 낙관적 잠금 없음 | 동일 공지사항·설교를 두 관리자가 동시 수정 시 Last-Write-Wins 발생 가능 |
| 감사 로그 없음 | 중요 데이터 변경 이력(누가 언제 수정/삭제했는지) 추적 불가 |
| 파일 트랜잭션 보장 없음 | DB 커밋 후 파일 삭제 순서를 보장하지만, 트랜잭션 롤백 엣지 케이스에서 파일 잔류 가능 |
| 이미지 리사이징 없음 | 원본 이미지 그대로 저장, 대용량 이미지 업로드 시 페이지 로딩 영향 가능 |
| 설교 동영상 스트리밍 미최적화 | 로컬 파일 서빙 방식이므로 대용량 파일 동시 접근 시 성능 한계 존재 |

---

## 14. 의존성 목록

```groovy
// QueryDSL
implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'

// Spring Boot Starters
implementation 'spring-boot-starter-data-jpa'
implementation 'spring-boot-starter-security'
implementation 'spring-boot-starter-validation'
implementation 'spring-boot-starter-webmvc'
implementation 'spring-boot-starter-thymeleaf'
implementation 'thymeleaf-extras-springsecurity6'

// 유틸
compileOnly + annotationProcessor 'lombok'
developmentOnly 'spring-boot-devtools'

// DB
runtimeOnly 'mysql-connector-j'

// 테스트
testImplementation 'spring-boot-starter-test'
testImplementation 'spring-boot-starter-security-test'
testRuntimeOnly 'junit-platform-launcher'
```
