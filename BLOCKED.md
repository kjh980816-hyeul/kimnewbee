# 자율 모드 차단 사유 (2026-05-04 업데이트)

## Phase 2-9: File 업로드 (이미지) — BLOCKED

### 필요한 결정
- **파일 저장 정책**: 다음 중 하나
  - (A) 가비아 g클라우드 VM 로컬 디스크 (`/var/uploads/...`) + nginx static serve
  - (B) AWS S3 또는 NCP Object Storage 등 외부 객체 스토리지
  - (C) DB BLOB 저장 (이미지 사이즈 제한)
- **이미지 크기 제한** + **허용 MIME**
- **CDN 사용 여부** (가비아 자체 CDN? Cloudflare?)
- **가비아 VM 디스크 용량** (`1.201.126.43` 의 가용 GB)

### 자율 모드가 결정하지 못하는 이유
사용자의 가비아 호스팅 정책 + 비용 제약 + DNS/CDN 운영 방침이 외부 정보. 임의 선택 시 운영 전환 시 재작업 발생 위험.

---

## Phase 1-14b: 게시판 관리 / 카페 꾸미기 — BLOCKED

### 필요한 정보
- **동적 게시판 시스템 명세** — CLAUDE.md "v3 핵심 차별점"으로 언급되지만 기획서 부재. 스크린샷(`docs/screenshots/07-admin-boards.jpg`)만 존재
- 신규 백엔드 도메인 필요:
  - Board entity (id, name, slug, type[list/gallery/card/video/letter/rank], 정렬 순서, 권한 등급)
  - 게시판 CRUD + 드래그&드롭 정렬 API
  - 카페 메뉴/배너 customization 도메인
- frontend admin views (AdminBoards.vue, AdminCafe.vue)

### 자율 모드가 결정하지 못하는 이유
- 동적 게시판 type 6종 각각의 필드 schema 부재
- 정렬 순서 저장 방식 (orderIndex vs 명시적 sequence)
- 권한 등급 매핑 방식 (게시판별 read/write tier 다중)
- 카페 꾸미기는 "메뉴/배너 커스텀" 외 구체 스펙 부재

### 권장 처리
사용자에게 6 게시판 type별 필드 스펙 + 권한 매트릭스 + 카페 꾸미기 범위 확정 요청.

---

## Phase 3-1 진짜 통합 검증 — SKIP (사용자 클릭 영역)

### 필요한 행동
- 사용자가 `frontend/.env.local`에서 `VITE_USE_MOCK=false` 설정
- 백엔드 bootRun + frontend dev server 동시 실행
- 브라우저에서 도메인별 클릭 검증

### 자율 모드 처리
- 백엔드 CORS (3-0 4b0266e) 완료 → INTEGRATION 토대 마련
- 매 도메인 단위 reviewer가 B6/F9로 mock contract ↔ backend response 일치 검증함
- 진짜 클릭 검증은 사용자 영역 (자율 모드 §1-2 외부 정보)

---

## Phase 4-1 가비아 배포 — 외부 작업

### 필요한 정보
- 가비아 g클라우드 SSH credentials (메모리에 일부 있음: 1.201.126.43, root, 비번 메모파일)
- 도메인 DNS 등록 (corncorntea.com — "엘리브" 등록업체 추가 정보)
- MySQL 운영 비번 결정
- HTTPS 인증서 (Let's Encrypt 등)

### 권장
사용자가 가비아 콘솔 + DNS 등록업체 작업 직접 수행. 자율 모드는 빌드 산출물 + 배포 가이드 README만 작성 가능.

---

## 다음 세션 진입점

**자율 모드로 가능**: 거의 없음 (큐 마무리됨)
- chore 후보: 추가 발견 시 처리

**사용자 결정 후 진행 가능**:
- 2-9 File 업로드 (위 정책 결정)
- 1-14b 동적 게시판 시스템 (스펙 확정)
- Phase 3-1 통합 (사용자 클릭 검증)
- Phase 4-1 배포 (가비아 작업)
