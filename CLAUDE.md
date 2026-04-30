# 고추밭 (김늉비 팬카페) — Claude Code 작업 헌법

> 모든 작업 시작 전 **이 문서 통째로 + `.claude/agents/reviewer.md` + `docs/고추밭_기획서.md`** 다 읽기.
> 이 문서는 자율 작업 모드를 전제로 작성됨. 사용자(준하)는 자고 있다고 가정.

---

## 0. 프로젝트 개요

- **이름**: 고추밭 (김늉비 팬카페)
- **GitHub**: https://github.com/kjh980816-hyeul/kimnewbee.git (public)
- **목표**: 가비아 호스팅 + 가비아 MySQL 실서비스 배포
- **현재**: 프론트 인라인 더미 상태 → Mock-First로 갈아엎고 백엔드 붙이기

---

## 1. 자율 작업 모드 (Autonomous Mode) ★ 핵심

### 1-1. 운영 원칙
사용자는 자고 있다. 깨우지 마. **각 단위 끝날 때마다 사용자에게 "다음 진행할까요?" 묻지 않는다.**
- reviewer가 PASS → 자동 커밋/푸시 → **자동으로 다음 단위 시작**
- 작업 큐(§9) 끝까지 알아서 진행
- 외부 정보 필수인 단위만 SKIP하고 다음으로 (사람만 줄 수 있는 거: DB 비번, 가비아 SSH, 도메인 등)

### 1-2. 종료 조건 (이때만 멈춤)
- 작업 큐 모두 완료
- 같은 게이트에서 reviewer가 **3회 연속 FAIL** → 구조적 문제. 멈추고 `BLOCKED.md`에 상황 적고 종료
- 외부 정보 필요한 단위 (DB 비번 같은) 만남 → SKIP하고 `BLOCKED.md`에 기록 후 다음 단위로
- 세션 토큰 한도 도달 → Claude Code가 자동 종료. 다음 세션에서 `git log` 보고 이어서 시작

### 1-3. 무한 루프 방지
- 같은 게이트에서 3회 연속 FAIL → 멈춤
- 같은 파일을 5회 이상 수정 중 → 잠시 멈추고 접근 방식 재고
- reviewer가 모순된 지적을 두 번 연속 → 메인이 reviewer에게 명확화 요청 1회 → 그래도 모순이면 SKIP하고 `BLOCKED.md`

### 1-4. 다음 세션 재개 방식
세션이 끊겼다 다시 시작될 때:
1. `git log --oneline -20` 으로 어디까지 했는지 확인
2. `BLOCKED.md` 있으면 읽고 차단 사유 확인
3. CLAUDE.md §9 작업 큐와 git 히스토리 비교 → 다음 미완 단위로 점프
4. 묻지 말고 그냥 진행

---

## 2. 절대 원칙 (위반 시 reviewer 무조건 FAIL)

### 2-1. 인라인 하드코딩 금지
컴포넌트 파일에 데이터 박지 마라. 모든 데이터는 API 호출로.
```ts
// ❌
const posts = [{ id: 1, title: '...' }, ...]

// ✅
const posts = ref<Notice[]>([])
onMounted(async () => { posts.value = await fetchNotices() })
```

### 2-2. Mock-First (백엔드 없는 시점 대응)
- MSW로 가짜 API 응답
- 컴포넌트는 진짜 axios 호출
- mock 데이터는 `src/mocks/data/`에만 (컴포넌트 X)
- 백엔드 완성되면 `VITE_USE_MOCK=false` 만 바꾸면 끝

### 2-3. 시크릿 절대 코드/문서에 박지 않기 ★
- API 키, OAuth Secret, JWT Secret, DB 비번 → **무조건 환경변수**
- `.env.local` (gitignore) 에만 실제 값
- `.env.example` 에는 placeholder만
- 코드 안에 `"K3S6JmEQ..."` 같은 거 박혀있으면 **즉시 FAIL + 작업 중단**

### 2-4. 과코딩 방지 (NAJACKS 두 번째 실패 패턴) ★★★
NAJACKS 때 또 다른 실패는 "과한 추상화"였다. 이번엔 막는다.

**금지 패턴:**
- 요구사항에 없는 기능 추가 X ("혹시 나중에 쓸지도..." 라는 이유 금지)
- 디자인 패턴은 실제 필요할 때만 (Strategy/Factory/Observer 등 남발 X)
- 인터페이스/추상 클래스가 구현체 1개뿐인 상태 X (불필요)
- generic 타입이 한 곳에서만 쓰이면 일반화하지 마
- 헬퍼 함수가 1번만 쓰이면 일반화하지 마 (인라인이 낫다)
- 200줄 넘는 컴포넌트 → 분리 검토. 단, 억지 분리 X
- 미사용 export, 호출 안 되는 함수 → 즉시 삭제
- "TODO: 나중에 X 추가" 같은 미래시제 주석 X (지금 안 할 거면 적지도 마)
- 죽은 import, 죽은 변수 X
- 반대로 같은 패턴 3번 반복되는데 추상화 안 한 것도 FAIL

**원칙:** "필요한 것만, 필요한 만큼." 두 번째 사용 사례가 생길 때 추상화. 지금 추상화하면 잘못된 추상화일 가능성이 높음.

### 2-5. 작업 단위 끝나면 reviewer 호출
- 임의로 "끝났음" 선언 X
- Task tool → reviewer sub-agent → 검증 → PASS 받아야 다음
- FAIL 시 지적 항목 전부 수정 후 재호출

---

## 3. 기술 스택

### Frontend
Vue 3 (Composition API + `<script setup>`), TypeScript (strict), Vite, Pinia, Vue Router, Axios, **MSW**, Tailwind CSS, Vitest, **ts-prune** (죽은 코드 검출용)

### Backend
Spring Boot 3, Java 21, Gradle 8.x, Spring Security + JWT, Spring Data JPA + MyBatis 하이브리드, MySQL, JUnit 5

### 배포
프론트/백엔드/DB 모두 가비아

---

## 4. 폴더 구조

### Frontend
```
frontend/
├── src/
│   ├── api/              # axios + API 함수
│   │   ├── client.ts
│   │   └── notice.ts ...
│   ├── components/
│   ├── views/            # 페이지
│   ├── stores/           # Pinia
│   ├── router/
│   ├── types/            # TypeScript 인터페이스
│   ├── mocks/            # MSW (★ mock 격리)
│   │   ├── browser.ts
│   │   ├── handlers/
│   │   └── data/
│   ├── composables/
│   ├── assets/
│   │   └── tokens.css    # 컬러 CSS 변수
│   ├── App.vue
│   └── main.ts
├── public/
│   └── mockServiceWorker.js
└── tests/
```

### Backend
```
backend/
├── src/main/java/com/gochubat/
│   ├── domain/{notice,user,post,...}/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── dto/
│   └── global/{jwt,oauth,security,exception,config}/
└── src/main/resources/
    ├── application.yml
    └── mapper/
```

---

## 5. 환경 변수 (★ 시크릿 처리 강제)

### 5-1. 사용자가 직접 만들어야 하는 파일

**`frontend/.env.local`** (사용자가 직접 작성, gitignore)
```bash
VITE_API_URL=http://localhost:8080
VITE_USE_MOCK=true
VITE_NAVER_CLIENT_ID=__사용자가_직접_입력__
```

**`backend/src/main/resources/application-local.yml`** (사용자 작성, gitignore)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gochubat
    username: __직접_입력__
    password: __직접_입력__

naver:
  client-id: __직접_입력__
  client-secret: __직접_입력__
  callback-url: http://localhost:8080/api/auth/naver/callback

jwt:
  secret: __직접_입력_64자_이상_랜덤__
  access-expiration: 1800000
  refresh-expiration: 1209600000
```

### 5-2. .env.example / application-example.yml
위 파일들의 **placeholder 버전**은 git에 commit. 실제 값 들어간 파일은 절대 push X.

### 5-3. .gitignore 필수 항목
```
# env
.env
.env.local
.env.*.local
**/application-local.yml

# 빌드
node_modules/
dist/
build/
.gradle/

# IDE
.idea/
.vscode/
*.iml

# OS
.DS_Store
```

### 5-4. 자율 모드에서 시크릿 처리
사용자가 미리 `.env.local` / `application-local.yml` 채워뒀다고 가정. 비어있으면 그 단위는 SKIP하고 `BLOCKED.md`에 기록.

---

## 6. 네이버 OAuth 등록 정보

네이버 개발자센터(developers.naver.com) → 애플리케이션 등록 시:

### 로컬 개발용
- **사용 API**: 네이버 로그인
- **서비스 URL**: `http://localhost:5173`
- **Callback URL**: `http://localhost:8080/api/auth/naver/callback`

### 가비아 배포 후 (도메인 발급 후 추가 등록)
- **서비스 URL**: `https://(가비아도메인)`
- **Callback URL**: `https://(가비아도메인)/api/auth/naver/callback`

> Client Secret은 백엔드만 보유. 프론트 `.env.local`에는 Client ID만.

---

## 7. 자동 리뷰 루프

### 7-1. 작업 단위 정의
- **FRONT 단위**: 한 화면(들)의 인라인 더미 제거 + axios + MSW handler + 타입 + 단위 테스트
- **BACKEND 단위**: 한 도메인 Controller/Service/Repo/Entity/DTO + 단위 테스트
- **INTEGRATION 단위**: MSW 끄고 진짜 백엔드와 프론트 붙이기 (도메인별)

### 7-2. 단위 끝났을 때
1. Task tool로 reviewer 호출
   ```
   subagent_type: reviewer
   prompt: "[단위명] 완료. [한 일 요약]. [FRONT|BACKEND|INTEGRATION] 검증 부탁."
   ```
2. PASS → `git add . && git commit -m "..." && git push origin main` → **자동으로 다음 단위 시작**
3. FAIL → 지적 항목 전부 수정 → 재호출

### 7-3. 절대 금지
- reviewer 안 거치고 다음 단위로 가기
- "이 부분만 임시로" 인라인 더미 남기기
- 테스트 미작성 + "나중에"
- reviewer 빠꾸 무시
- reviewer에게 "그냥 통과시켜줘" 요청

---

## 8. 디자인 시스템

자세한 건 `docs/고추밭_기획서.md`. 핵심:

- 다크모드 기본
- Primary: `#7ED05A` (Pepper)
- Point: `#F7D14A` (Corn)
- Background: `#000000`
- Naver Green: `#03C75A`
- Chzzk Green: `#00FFA3`

**컬러 직접 hex로 박지 마. 무조건 CSS 변수.**

```css
:root {
  --color-pepper: #7ED05A;
  --color-pepper-deep: #5FAB3F;
  --color-corn: #F7D14A;
  --color-cheek: #F58FA8;
  --color-paper: #000000;
  --color-surface: #141414;
  --color-elevated: #1F1F1F;
  --color-border: #2A2A2A;
  --color-ink: #FAFAFA;
  --color-ink-muted: #A1A1AA;
  --color-chzzk: #00FFA3;
  --color-naver: #03C75A;
}
```

---

## 9. 작업 큐 (자율 모드가 차례대로 소화할 리스트)

> ⚠️ 이 큐를 순서대로 소화. 각 단위 끝나면 reviewer → PASS → commit/push → 다음.

### Phase 1: 프론트 Mock-First 리팩토링
- [ ] **1-1**: 프론트 인프라 셋업 (msw/vitest/ts-prune 의존성, 폴더 구조, axios client, MSW 셋업, .env.example, 컬러 CSS 변수, .gitignore, README)
- [ ] **1-2**: 공지사항 (NoticeList, NoticeDetail) — 인라인 더미 제거 + 타입 + api + MSW handler + mock 데이터 + 단위 테스트
- [ ] **1-3**: 자유게시판 (List, Detail, Write) + 댓글/대댓글 + 좋아요
- [ ] **1-4**: 팬아트 (갤러리형) + 이미지 mock URL
- [ ] **1-5**: 영상/클립 (URL 임베드 — 치지직/유튜브)
- [ ] **1-6**: 팬레터 (편지형, 관리자만 열람 — mock 권한)
- [ ] **1-7**: 반려동물 사진 (갤러리형)
- [ ] **1-8**: 노래추천 (투표형)
- [ ] **1-9**: 오프후기 (카드형, 옥수수 등급 이상)
- [ ] **1-10**: 메인 홈 (각 게시판 최신글 위젯, LIVE 배너)
- [ ] **1-11**: 회원가입/로그인 화면 (네이버 OAuth 버튼 — mock 응답)
- [ ] **1-12**: 마이페이지 (활동/등급/포인트)
- [ ] **1-13**: 글쓰기/수정 폼 (게시판별)
- [ ] **1-14**: 관리자 페이지 (게시판 관리, 회원 관리, 카페 꾸미기)

### Phase 2: 백엔드 구축
- [ ] **2-1**: Spring Boot 프로젝트 셋업 + JPA/MyBatis 설정 + JWT 유틸 + Security Config + 글로벌 예외 처리
- [ ] **2-2**: User 도메인 + 네이버 OAuth + JWT 발급
- [ ] **2-3**: Notice 도메인 (CRUD)
- [ ] **2-4**: Post 공통 도메인 (자유/팬아트/영상 공유)
- [ ] **2-5**: Comment 도메인 (대댓글)
- [ ] **2-6**: Like 도메인
- [ ] **2-7**: FanLetter 도메인 (관리자 권한 필터)
- [ ] **2-8**: 특수 게시판 (반려동물/노래/오프후기)
- [ ] **2-9**: File 업로드 (이미지)
- [ ] **2-10**: 등급/포인트 도메인
- [ ] **2-11**: Admin API

### Phase 3: 프론트-백엔드 연동
- [ ] **3-1**: 도메인별 MSW 끄고 실 API 전환 (Notice → User → Post → ...)
- [ ] **3-2**: 통합 테스트 1 사이클

### Phase 4: 가비아 배포 (외부 정보 필요 → SKIP 가능)
- [ ] **4-1**: 빌드 산출물 작성 + 배포 가이드 README — 실 배포는 사용자 수동

---

## 10. Git 워크플로우

- Repo: https://github.com/kjh980816-hyeul/kimnewbee.git
- 브랜치: `main` 단일
- 커밋 단위: reviewer PASS = 1커밋 = 1 push
- 컨벤션:
  - `feat: 공지사항 화면 Mock-First 전환`
  - `feat(backend): notice domain CRUD`
  - `fix: 페이지네이션 인덱스 오류`
  - `chore: msw 의존성 추가`
  - `test: notice api 단위 테스트`
  - `refactor: api/client 인터셉터 분리`

자율 모드에서 push 실패 시 (네트워크/권한) → `BLOCKED.md`에 기록 후 다음 단위로.

---

## 11. 자주 하는 실수 (회피 목록)

- ❌ "테스트는 일단 스킵하고 나중에 한번에"
- ❌ "이건 단순해서 reviewer 안 거쳐도 됨"
- ❌ "MSW 셋업 귀찮으니 인라인으로"
- ❌ "타입 일단 any로"
- ❌ "컬러 일단 hex로 박고 나중에 변수화"
- ❌ "혹시 나중에 쓸지 모르니 generic하게"
- ❌ "추상 인터페이스 미리 만들어두자"
- ❌ "주석에 TODO: 나중에 X 추가"
- ❌ 시크릿 코드에 박기
- ❌ git push를 모아서 한번에

전부 NAJACKS 패턴이다. 각 게이트가 검출함.
