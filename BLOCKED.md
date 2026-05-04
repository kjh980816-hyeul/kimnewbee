# 자율 모드 차단 사유 (2026-05-04 업데이트)

## ✅ 해소된 BLOCK

- **Phase 2-9 File 업로드** (385f12b) — 합리적 기본값(VM 로컬 디스크 + yml 외부화)으로 진행. 운영 정책 변경 시 yml + UploadService impl만 교체 가능.

---

## ⏳ 남은 BLOCK

### Phase 1-14b: 게시판 관리 / 카페 꾸미기

**필요한 정보**:
- 동적 게시판 시스템 명세 — CLAUDE.md "v3 핵심 차별점" 언급되지만 기획서 부재. 스크린샷(`docs/screenshots/07-admin-boards.jpg`)만 존재
- 신규 백엔드 도메인 필요:
  - Board entity (id/name/slug/type[list/gallery/card/video/letter/rank]/정렬순서/권한등급)
  - 게시판 CRUD + 드래그&드롭 정렬 API
  - 카페 메뉴/배너 customization
- frontend admin views (AdminBoards.vue, AdminCafe.vue)

**미결정 사항**:
- 6 게시판 type별 필드 schema
- 정렬 순서 저장 방식 (orderIndex vs 명시적 sequence)
- 권한 등급 매핑 (게시판별 read/write tier 다중)
- 카페 꾸미기 범위 (메뉴/배너 외)

### Phase 3-1 진짜 통합 검증 — SKIP (사용자 클릭 영역)

- 사용자가 `frontend/.env.local`에서 `VITE_USE_MOCK=false` 설정
- 백엔드 bootRun + frontend dev server 동시 실행
- 브라우저에서 도메인별 클릭 검증
- mock contract ↔ backend response 일치는 매 단위 reviewer가 B6/F9로 검증함

### Phase 4-1 가비아 배포

- 가비아 g클라우드 SSH credentials (메모리: 1.201.126.43, root + 메모파일)
- 도메인 DNS 등록 (corncorntea.com — "엘리브" 등록업체)
- MySQL 운영 비번 결정
- HTTPS 인증서 (Let's Encrypt 등)
- 사용자가 가비아 콘솔 + DNS 등록업체 작업 직접 수행

---

## 다음 세션 진입점

**자율 모드로 가능**: 거의 없음 (자율 가능한 큐 마무리됨)
- 필요 시 chore (cleanup/refactor)

**사용자 결정 후 진행 가능**:
- 1-14b 동적 게시판 시스템 (스펙 확정)
- Phase 3-1 통합 (사용자 클릭 검증)
- Phase 4-1 배포 (가비아 작업)

**선택적 frontend 확장 (자율 가능)**:
- 5보드 Write/Edit 폼에 file picker 추가 (이미지 URL 직접 입력 → /api/files 업로드 → URL 자동 채움). 1-13/1-14a 패턴 enhancement.
