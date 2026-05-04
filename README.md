# 김늉비 팬카페 "고추밭"

치지직 스트리머 김늉비의 팬(초록고추) 커뮤니티 팬카페 — Spring Boot + Vue 3, 가비아 g클라우드 배포.

> 핵심 차별점: **동적 게시판 시스템** (admin이 게시판 추가/순서/권한/카페 꾸미기 직접 관리).

---

## 진척

- ✅ **Phase 1 프론트**: 14/14 완료 (인프라 → 8 보드 + 메인홈 + 로그인 + 마이페이지 + 글쓰기/수정 + 관리자 페이지 4종)
- ✅ **Phase 2 백엔드**: 11/11 완료 (Spring Boot 인프라 / User OAuth / Notice / Post 공통 / Comment / Like / FanLetter / 특수 게시판 / File 업로드 / 등급포인트 / Admin API)
- ✅ **Phase 3-0**: INTEGRATION 인프라 (CORS + .env.example)
- ⏳ Phase 3-1: 사용자 클릭 검증 영역 (`.env.local`에서 `VITE_USE_MOCK=false`로 토글)
- ⏳ Phase 4-1: 가비아 실 배포 (사용자 콘솔 + DNS 작업, `docs/DEPLOY_OPS.md` 절차 따라)

**테스트**: 백엔드 34 spec / 200 tests + 프론트 19 spec / 92 tests = **53 spec / 292 tests** (모두 GREEN).

---

## 빠른 시작 (로컬 개발)

### 사전 요구
- Java 21 (Gradle wrapper가 자동 다운로드)
- Node.js 20+
- (선택) MySQL 8 — 기본은 H2 in-memory

### 1. 백엔드

```bash
cd backend
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
# application-local.yml의 placeholder (네이버 client_id/secret 등) 채우기
./gradlew bootRun
# http://localhost:8080/actuator/health → {"status":"UP"}
```

### 2. 프론트엔드

```bash
cd frontend
cp .env.example .env.local
# VITE_USE_MOCK=true (mock-first 개발) 또는 false (실 백엔드 연결)
npm install
npm run dev
# http://localhost:5173
```

### 3. 테스트

```bash
cd backend && ./gradlew test
cd frontend && npx vitest run
```

---

## 구조

```
gochubat/
├── CLAUDE.md                ← 작업 헌법 (자율 모드 + 절대 원칙 + 작업 큐)
├── SUMMARY.md               ← 진척도 스냅샷
├── BLOCKED.md               ← 차단 사유
├── README.md                ← 이 파일
├── application-local.example.yml
├── backend/                 ← Spring Boot 3.5 + Java 21
│   └── src/main/java/com/gochubat/
│       ├── domain/{user,notice,post,comment,like,letter,song,board,cafe,admin,point}/
│       └── global/{config,exception,jwt,oauth,security,upload,dto}/
├── frontend/                ← Vue 3.5 + Vite 6 + TS strict + MSW + Tailwind 3
│   └── src/
│       ├── api/             ← axios 함수
│       ├── views/           ← 화면 (admin/auth/board별)
│       ├── components/{post,home}/
│       ├── composables/     ← useIsOwner, useImageUpload
│       ├── mocks/           ← MSW handlers + fixtures
│       ├── router/
│       ├── types/
│       └── stores/
└── docs/
    ├── DEPLOY_GABIA.md      ← 사용자 콘솔 작업 체크리스트 (사용자 작성)
    ├── DEPLOY_OPS.md        ← 정보 수령 후 SSH/배포 절차 (운영자용)
    └── screenshots/         ← UI 레퍼런스 8장
```

---

## 스택

**Backend**: Spring Boot 3.5 / Java 21 / Spring Security stateless + JJWT 0.12 (cookie + Authorization header) / JPA/MyBatis 하이브리드 / MySQL (운영) / H2 (로컬 dev)

**Frontend**: Vue 3.5 + Composition API / Vite 6 / TypeScript strict / Pinia / Vue Router / Axios / MSW / Tailwind CSS / Vitest / ts-prune

**Auth**: 네이버 OAuth 2.0 단일 (HttpOnly 쿠키 + JWT typ access/refresh 분리)

**배포**: 가비아 g클라우드 VM (Ubuntu) + nginx (SPA fallback + /api 프록시 + /uploads 정적) + Let's Encrypt HTTPS

---

## 등급 시스템

- 🌱 **SEED** — 새 가입자 (기본)
- 🌶️ **PEPPER** — 활동 누적 100 points
- 🌽 **CORN** — 활동 누적 500 points (오프후기 게시판 접근)
- 👑 **OWNER** — 관리자 수동 지정 (대시보드/회원관리/게시판/카페 꾸미기)

포인트 자동 적립 (yml 외부화 정책): 글 작성 +10 / 댓글 +2 / 좋아요 받음 +1.

---

## 동적 게시판

8 시드 (notice / free / fanart / clips / letters / pets / songs / offline). 6 레이아웃 (LIST / GALLERY / CARD / VIDEO / LETTER / RANK). 각 게시판별 read/write tier 매트릭스. admin이 추가/삭제/순서/권한/active 토글 가능.

> 풀 동적 Post.boardId FK 매핑은 별도 마이그레이션 단위로 분리. 현재 5 콘텐츠 보드(Free/Fanart/Clip/Pet/Offline)는 컨트롤러 하드코딩 + Board는 메타데이터 관리 전용.

---

## 자율 모드 (Claude Code)

`CLAUDE.md` §1-4가 작업 헌법.

- 매 단위 reviewer sub-agent 호출 → PASS 받아야 다음
- 1-13a/1-14a/1-14b-1/카페 display 사례에서 발견: **Edit 도구가 success 응답을 줘도 실제 파일에 적용 안 될 수 있음** → 같은 파일 다회 Edit 시 grep 검증 필수
- vue-tsc `--noEmit` 통과 ≠ `npm run build` 통과 → 자가 점검에 build 명시
- 외부 정보 필요하면 SKIP하고 BLOCKED.md 기록 후 다음 단위로 (자율 §1-2)

다음 세션 진입:
```
자율모드 이어서 진입
```
→ git log + SUMMARY.md + BLOCKED.md 보고 자동 이어감.

---

## 사용자 영역 (자율 불가)

1. **Phase 3-1 진짜 통합 검증**: `.env.local` `VITE_USE_MOCK=false` 토글 → 양쪽 dev server 동시 → 브라우저 클릭 검증
2. **Phase 4-1 실 배포**: `docs/DEPLOY_GABIA.md` 가비아 콘솔 작업 → §10 정보 →  `docs/DEPLOY_OPS.md` 절차 SSH 실행
3. **첫 OWNER 등록**: 네이버 로그인 후 MySQL `UPDATE users SET tier='OWNER' WHERE id=N` (`DEPLOY_OPS.md §7`)
