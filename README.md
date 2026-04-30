# 김늉비 팬카페 "고추밭" — Claude Code 핸드오프 패키지

이 폴더 전체를 **프로젝트 루트로 그대로 복사**한 다음 Claude Code를 실행하세요.
Claude Code는 루트의 `CLAUDE.md`를 자동으로 읽고 작업 컨텍스트로 사용합니다.

---

## 📦 폴더 구성

```
gochubat-handoff/
├── CLAUDE.md                    ← Claude Code가 자동으로 읽는 메인 가이드 (제약/컨벤션/작업순서)
├── README.md                    ← 이 파일
├── .claude/
│   └── settings.json            ← ⭐ 응답 끝낼 때마다 typecheck/lint 자동 실행 (자잘 버그 방지)
└── docs/
    ├── 고추밭_기획서.md          ← 비즈니스 기획서 (왜 만드는가)
    └── screenshots/             ← UI 레퍼런스 8장
        ├── 02-home.jpg               홈 (다크 + LIVE 배너)
        ├── 03-board-list.jpg         자유게시판 목록
        ├── 04-post-detail.jpg        게시글 상세 + 댓글
        ├── 05-fanart.jpg             팬아트 갤러리
        ├── 06-admin-dashboard.jpg    관리자 대시보드
        ├── 07-admin-boards.jpg       ⭐ 관리자 게시판 관리 (드래그 정렬)
        ├── 08-admin-editor.jpg       ⭐ 관리자 게시판 편집/만들기
        └── 09-login.jpg              네이버 소셜 로그인
```

> ⭐ 표시된 두 장은 핵심 차별 기능(동적 게시판)의 UI 레퍼런스라 특히 중요합니다.

---

## 🚀 Claude Code 시작 방법

### 1. 프로젝트 폴더 만들기

```bash
mkdir gochubat && cd gochubat
# 이 핸드오프 폴더의 모든 파일을 여기로 복사
# 결과적으로 ./CLAUDE.md, ./README.md, ./docs/ 가 루트에 위치
```

### 2. Claude Code 실행

```bash
claude
```

### 3. 첫 작업 프롬프트 (복사해서 사용)

**처음 셋업할 때:**
```
CLAUDE.md와 docs/고추밭_기획서.md를 먼저 끝까지 읽어줘.
docs/screenshots/도 한 번 훑고 시작.

읽고 나서, 어떤 순서로 작업할 계획인지 1~2문단으로
요약해서 보여준 뒤 내 승인을 받고 시작해줘.

지금 목표는 CLAUDE.md §17 스프린트 1의 항목 1~3을 진행하는 거야:
1. 모노레포 셋업 (pnpm workspace, husky, lint-staged, prettier, eslint)
2. NestJS 베이스 + TypeORM + PostgreSQL docker-compose
3. Next.js 14 베이스 + Tailwind 토큰 (CLAUDE.md §6.1) + 폰트

작업 후 디렉토리 트리, docker compose up 방법,
pnpm dev 실행법을 알려줘.
```

**이후 작업할 때 (예시):**
```
스프린트 1의 항목 4~6을 이어서 진행해줘:
4. 네이버 OAuth 로그인 풀 흐름 (/v1/auth/*)
5. JWT Guard, TierGuard, OwnerGuard
6. 레이아웃 셸 (Header / Sidebar / 빈 페이지)

스크린샷 02-home.jpg의 사이드바 톤이랑 메뉴 카운터를 참고해서
레이아웃 셸을 만들어줘.
```

---

## 🐛 버그가 자꾸 나면 (자잘한 버그 박멸 모드)

Claude Code 작업 시 **자잘한 타입/import/null 처리 실수**가 누적되는 게 가장 큰 사고 원인이에요.
이 패키지는 두 가지 안전장치를 미리 넣어뒀어요:

### 1. CLAUDE.md §20 "품질 보증 루프"
- 모든 변경 후 typecheck/lint/test를 자동으로 돌리는 사이클
- 자주 나는 자잘한 버그 18종 카탈로그 + 방지법
- 변경 보고 형식 강제 (검증 결과 없는 보고 금지)
- 위험 변경 추가 검증 규칙
- 회귀 테스트 추가 강제

### 2. `.claude/settings.json` 자동 검증 hook ⭐
응답 끝낼 때마다 Claude Code가 **자동으로 typecheck/lint를 실행**합니다.
실패하면 결과를 보고 스스로 다음 턴에 수정해요.

**활성화 시점:**
- 스프린트 1에서 `package.json`에 `typecheck`, `lint` 스크립트가 추가된 **직후**부터 작동
- 그 전엔 명령이 실패하므로, 셋업 끝날 때까지는 일단 둬도 무해 (`|| true`로 응답 차단 안 함)

**확인 프롬프트:**
```
.claude/settings.json의 hook이 정상 작동하는지 확인해줘.
응답 끝낼 때 pnpm typecheck가 돌아가는 게 보여야 함.
```

### 버그 발견 시 던질 프롬프트

```
버그 발견:
- 어디서: /boards/free 진입 시
- 증상: "Cannot read property 'tag' of undefined" 에러
- 재현: 새 계정으로 로그인 후 자유게시판 클릭

CLAUDE.md §20.9 디버깅 모드대로 진행해줘:
1. 먼저 재현 조건 확정
2. 에러 스택 읽고 가설 세우기
3. 최소 변경으로 검증
4. 수정 후 회귀 테스트 추가 (§20.7)
5. 보고는 §20.5 형식으로
```

### 막혔을 때 던질 프롬프트

```
방금 만든 코드를 CLAUDE.md §20.4 자주 나는 버그 카탈로그 18종에 비추어
스스로 self-review해줘. 의심 가는 패턴이 있으면 §20.5 형식으로 보고.
```

---

## 💡 작업 팁

### CLAUDE.md를 진실 공급원으로 쓰세요
- 기획서와 충돌하면 **CLAUDE.md가 우선** (§0에 명시)
- 비즈니스 의도가 헷갈리면 기획서, 구현 제약은 CLAUDE.md

### 스코프 관리
- Phase 1 MVP를 끝내기 전엔 Phase 2/3 기능에 손대지 마세요
- 새 기능이 필요하면 먼저 백로그에 적어두고 나중에 검토

### 변경 검토
- Claude Code가 새 의존성을 추가하려고 하면 **CLAUDE.md §4의 금지 목록**과 비교
- 권한/등급 관련 코드는 단위 테스트 필수 (§19)

### 막히면
- "CLAUDE.md §11(동적 게시판)을 다시 읽고 너가 작성한 코드가 §11.1 원칙을 지키는지 검토해줘" 처럼 섹션 번호로 지목하면 효과적
- "§20.4 카탈로그 #N번 패턴에 해당하는지 확인해줘" 처럼 버그 패턴 번호로 지목하면 더 효과적

---

## 📌 핵심 차별 기능 미리보기

이 프로젝트의 **차별점은 동적 게시판 시스템**(CLAUDE.md §11)입니다.

- 게시판은 **데이터**다. 코드에 게시판 이름/구조 하드코딩 금지
- 6가지 형식(list / gallery / card / video / letter / rank)은 렌더링 컴포넌트만 다름
- 권한은 등급(seed → pepper → corn → owner)으로 게시판마다 개별 설정
- 관리자가 드래그&드롭으로 순서 변경, 형식 변경, 권한 변경 (`07-admin-boards.jpg`, `08-admin-editor.jpg`)

이 부분이 잘 안 풀리면 Phase 2 전체가 무너지므로, 스키마 설계 단계에서 충분히 시간을 쓰세요.

---

## 🛑 흔한 실수 빨리 잡기

| 증상 | 어디 보면 됨 |
|---|---|
| 게시판 이름이 코드에 박혀있음 | CLAUDE.md §11.1, §18 |
| 권한이 프론트에서만 막혀있음 | CLAUDE.md §15, §18 |
| `synchronize: true`로 DB 만들고 있음 | CLAUDE.md §4, §18 |
| 라이트 모드 작업 중 | CLAUDE.md §6.4 (Phase 1엔 다크만) |
| Pages Router 쓰고 있음 | CLAUDE.md §4 |
| axios 설치함 | CLAUDE.md §4 금지 목록 |
| 자잘한 타입 에러가 누적됨 | CLAUDE.md §20.4 카탈로그 |
| 변경 후 검증 안 하고 보고함 | CLAUDE.md §20.3, §20.5 |
| 같은 버그가 또 발생함 | CLAUDE.md §20.7 회귀 테스트 |
| 에러 메시지 안 읽고 추측으로 고침 | CLAUDE.md §20.9 디버깅 모드 |

---

## 🔄 변경/업데이트

- 기획 변경이 생기면 **CLAUDE.md를 먼저 수정**한 뒤 Claude Code에 알리기
- 기획서는 비즈니스 설명용이라 가끔 뒤처져도 큰 문제 없음 (단, 컬러/디자인 토큰은 CLAUDE.md §6.1 기준)

---

*이 패키지는 2026-04-26 기준입니다. CLAUDE.md §21에 변경 이력 누적 권장.*
