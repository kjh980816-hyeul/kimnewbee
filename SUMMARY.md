# 자율 모드 진행 요약 (Phase 1 Mock-First → Phase 2 Backend)

> 2026-05-01 ~ 05-04 자율 모드 결과.
> Phase 1: 12/14 frontend 단위 완료 (1-13/1-14 미완).
> Phase 2: 2-1 인프라 + 2-2 User+OAuth+JWT 완료, 2-3부터 진행 중.

---

## ✅ 완료된 단위 (12개)

| Phase | 내용 | 핵심 산출물 | 커밋 |
|---|---|---|---|
| 1-1 | 프론트 인프라 셋업 | Vue3 + Vite6 + TS strict + Pinia + MSW + Tailwind + Vitest + ts-prune. 폴더 구조 / axios client / tokens.css (12 컬러 변수) / .env.example | 4ec1036 |
| 1-2 | 공지사항 | NoticeList / NoticeDetail (List type) | 81d45b2 |
| 1-3 | 자유게시판 | FreeList/Detail/Write + 댓글/대댓글/좋아요. 싱글톤 mock store | 6c12d66 |
| 1-4 | 팬아트 갤러리 | FanartList/Detail/Write (Gallery type, picsum.photos 이미지) | 8556966 |
| 1-5 | 영상/클립 + CommentSection 추출 | ClipList/Detail/Write (URL 임베드 youtube/chzzk). 3rd-use 추상화로 `components/post/CommentSection.vue` 분리 | 0fad582 |
| 1-6 | 팬레터 + 관리자 권한 게이트 | LetterList/Detail/Write. mock-is-admin 토글로 본문 403 분기 | 0fcdd7b |
| 1-7 | 반려동물 갤러리 + PostArticle 추출 | PetList/Detail/Write. 4th-use 추상화로 `components/post/PostArticle.vue` 분리 (4개 디테일 뷰 공통) | bc2f78a |
| 1-8 | 노래추천 (투표형) | SongList(인라인 투표)/SongAdd. 디테일 페이지 없음 (rank type) | 98b3925 |
| 1-9 | 오프후기 + 옥수수 등급 게이트 + isHttpStatus 헬퍼 | OfflineList/Detail/Write (Card type). corn(2) 미만 list/detail/create/like 모두 403 | 453260f |
| 1-10 | 메인 홈 | LiveBanner (chzzk 토글) + 6 board WidgetCard 동시 fetch | f84a4b1 |
| 1-11 | 회원가입/로그인 | LoginView (네이버 OAuth mock 단일 CTA) | a7bc0d4 |
| 1-12 | 마이페이지 | MyPageView (등급 뱃지/포인트/활동 통계 4종/로그아웃) | fe23a52 |

---

## ⚠️ 미완 단위 (2개)

### Phase 1-13: 글쓰기/수정 폼 (게시판별)
**상태**: 글쓰기 폼은 1-3~1-9에서 모두 완료. **수정(Edit) 폼은 자유 1보드 완료, 4보드 미진행.**

**1-13a (자유) 완료** (d0239cb): FreeEdit.vue + api.updateFreePost + mock PATCH handler(404/401/403/200) + router /free/:id/edit + FreeDetail isOwner 수정 버튼.

**1-13b (남은 4보드) 필요 작업** (Fanart/Clip/Pet/Offline):
- 동일 패턴 4번 반복 (FreeEdit 복제 + 도메인별 필드 차이 적용)
  - Fanart: title/imageUrl/content
  - Clip: title/videoUrl/description
  - Pet: title/imageUrl/content
  - Offline: title/location/meetupDate/imageUrl/content
- 각 mock handler PATCH 추가 (readMockUser 재사용)
- 각 router /:id/edit 라우트 + Detail에 isOwner 수정 버튼

**5번째 use 트리거**: 5보드 같은 author check + 401/403 분기 → `mocks/utils/createOwnerOnlyPatch.ts` 또는 composable 검토.

### Phase 1-14: 관리자 페이지
**상태**: 미착수.

**필요 작업** (CLAUDE.md §9 기준):
- 관리자 대시보드 (`AdminDashboard.vue`) — 회원/게시글 통계
- 게시판 관리 (`AdminBoards.vue`) — 드래그&드롭 정렬 (07-admin-boards.jpg 참고)
- 회원 관리 (`AdminMembers.vue`) — 등급 변경/정지
- 카페 꾸미기 (`AdminCafe.vue`) — 메뉴/배너 커스텀

**의존**: owner 등급 게이트 필요 (현재 mock-tier로 시뮬 가능). 동적 게시판 시스템(CLAUDE.md §11) 검토 필요.

---

## ✅ Phase 2 진척도 (2026-05-04)

| Phase | 내용 | 핵심 산출물 | 커밋 |
|---|---|---|---|
| 2-1 | Spring Boot 인프라 | Spring Boot 3.5 + Java 21 (Foojay) + JPA/MyBatis 하이브리드 + Spring Security stateless + JJWT + GlobalExceptionHandler / ErrorCode / CustomException / ErrorResponse + JwtUtil / JwtAuthenticationFilter / JwtProperties + actuator | aa69167 |
| 2-2 | User 도메인 + 네이버 OAuth + JWT 발급 | User entity + UserRepository + AuthService + NaverOauthClient + AuthController (cookie 기반) + UserController + SecurityConfig 인증 게이트 + JwtAuthenticationFilter cookie/header + JWT typ claim 분리 | 0b29064 |
| 2-3 | Notice 도메인 (CRUD) | Notice entity (FK author, viewCount auto-inc) + NoticeRepository (JOIN FETCH 보조 정렬키 `id desc`) + NoticeService + NoticeController (GET 공개, POST/PATCH/DELETE @PreAuthorize OWNER) + @EnableMethodSecurity + AuthorizationDeniedException 핸들러 | cd15af0 |
| 2-4 | Post 공통 도메인 (자유/팬아트/영상) | 단일 Post entity (BoardType enum + media_url + clip_source) + ClipSource.detect (youtube/chzzk/other) + PostRepository (타입 격리 쿼리) + PostService + 3 컨트롤러(Free/Fanart/Clip) + 보드별 DTO 6종 + ListResponse<T> + AuthenticatedController 추상화 (User/Notice/3보드 5컨트롤러 공유) | bcea157 |
| 2-5 | Comment 도메인 (대댓글) | Comment entity (postId/parentId Long FK + soft delete) + 배치 카운트 projection + CommentService (parent-post 일치 검증, owner-only soft delete) + Post DTO 6개 commentCount 연동 + 컨트롤러 PostBoardAssembler 패턴 도입 | fa9936b |
| 2-6 | Like 도메인 | PostLike entity (unique post_id+user_id) + 배치 countByPostIds + LikeService (BoardType 검증 + 토글) + LikeController 3 endpoints + PostBoardAssembler 추출 (4번째 use 트리거, Comment+Like 통합 조립) + Post Detail DTOs likeCount/likedByMe 진짜 값 | 8a93de5 |
| 2-7 | FanLetter 도메인 (관리자 권한 필터) | Letter entity (preview/readByAdmin idempotent 마크) + LetterService.detailForAdmin (write tx + 사이드이펙트) + LetterController (GET 공개 / detail @PreAuthorize OWNER + read 자동 마크 / POST 인증) + /api/me/admin (anon false / OWNER true) | ccab8e0 |
| 2-8 | 특수 게시판 (반려동물/노래/오프후기) | Post에 PET/OFFLINE BoardType + nullable location/meetupDate + preview() 60자 + Pet/Offline 컨트롤러(Offline 클래스 레벨 @PreAuthorize CORN/OWNER 등급 게이트) + LikeController Pet/Offline 토글 + Song 별도 도메인(SongVote unique + voteCount 배치 + votedByMe per-user) | ab7ba0e |
| 2-9 | File 업로드 | **BLOCKED** — 가비아 파일 저장 정책 외부 정보 필요 (BLOCKED.md 참고). 사용자 결정 후 진행. | - |
| 2-10 | 등급/포인트 도메인 | PointReason enum + PointPolicy(@ConfigurationProperties yml: post=10/comment=2/likeReceived=1/pepper=100/corn=500) + PointService.award (auto tier 승급, OWNER 고정, floor 0) + User.addPoints/promoteTo + UserService.getStats 진짜 카운트 + Post/Comment/Like service에 award 연동 | 6621c0a |
| 2-11 | Admin API | AdminService(dashboard 4 카운트 / listUsers desc 결정성 / changeTier 강등 가능 / adjustPoints floor 0) + AdminController 클래스 레벨 @PreAuthorize OWNER + User.changeTier 추가 (강등용, promoteTo와 의미 분리) + 4 endpoints | 83de213 |

**reviewer 결과**:
- 2-2: 1차 FAIL (NaverTokenResponse dead 4필드 / IllegalArgumentException / RuntimeException catch-all / JWT typ claim / 헤더 path 테스트) → 2차 PASS
- 2-3: 1차 FAIL (NoticeRepository 정렬 비결정성 / GlobalExceptionHandler dead AccessDeniedException) → 2차 PASS
- 2-4: 1차 FAIL (`requireUserId` 5중복) → 2차 PASS
- 2-5: 1차 PASS
- 2-6: 1차 FAIL (likedPostIdsByMe + findLikedPostIds dead code) → 2차 PASS
- 2-7: 1차 PASS
- 2-8: 1차 PASS
- 2-10: 1차 FAIL (UserStatsResponse.zero() dead) → 2차 PASS
- 2-11: 1차 PASS

**테스트**: 백엔드 30 spec / 174 tests (Phase 2 시작 7/25 → +23 spec / +149 tests). **Phase 2 백엔드 10/11 완료** (2-9만 BLOCKED).

**다음 권장 단위**: **Phase 3-1 (도메인별 MSW 끄고 실 API 전환)** — INTEGRATION 단계 시작. Notice → User → Post 순으로 `VITE_USE_MOCK=false` 전환 + CORS/JWT 쿠키 흐름 점검. 또는 Phase 1 미완(1-13 Edit 폼 / 1-14 admin 페이지) 처리. cleanup 후보: NoticeListResponse + ListResponse<T> 통합 → global/dto/ListResponse<T>. AdminController self-protection (자기 자신 강등 차단)은 1-14 admin UI 단위에서 함께 추가.

---

## 📊 테스트 / 코드 통계 (1-12 시점)

```
15 spec files / 62 tests passed
- api-client.spec.ts: 3
- api/notice.spec.ts: 4
- api/free.spec.ts: 4
- api/comment.spec.ts: 5
- api/fanart.spec.ts: 4
- api/clip.spec.ts: 6
- api/letter.spec.ts: 5
- api/pet.spec.ts: 4
- api/song.spec.ts: 4
- api/offline.spec.ts: 7
- api/error.spec.ts: 3
- api/chzzk.spec.ts: 2
- api/auth.spec.ts: 2
- api/me.spec.ts: 4
- components/PostArticle.spec.ts: 5
```

빌드 산출물:
- 메인 인덱스: 97.74 kB (gzip 38 kB)
- MSW worker (browser): 288 kB (dev only, 프로덕션 빌드에선 분리됨)
- 보드별 청크: 각 1~6 kB

---

## 🎨 추출된 공통 컴포넌트

| 컴포넌트 | 사용처 | 추출 시점 |
|---|---|---|
| `components/post/CommentSection.vue` | FreeDetail / FanartDetail / ClipDetail / PetDetail / OfflineDetail (5개) | 3rd-use 트리거 (1-5) |
| `components/post/PostArticle.vue` (named slot `media`) | FreeDetail / FanartDetail / ClipDetail / PetDetail / OfflineDetail (5개) | 4th-use 트리거 (1-7) |
| `components/home/LiveBanner.vue` | HomeView 전용 | 1-10 |
| `components/home/WidgetCard.vue` | HomeView 6개 위젯 | 1-10 |
| `api/error.ts` (`isHttpStatus`) | LetterDetail / OfflineList/Detail/Write / MyPageView (5 호출처) | 6th-use 트리거 (1-9) |

LetterDetail은 PostArticle 비대상 (좋아요/조회수/미디어 모두 없고 권한 분기 별개).

---

## 🌗 디자인 시스템 준수

- 다크 모드 기본 (`<html class="dark">`, `bg-paper #000000`)
- 모든 컬러는 `tokens.css` CSS 변수 → `tailwind.config.js`에서 매핑
- 컴포넌트에 hex 직박 0건 (reviewer F3 게이트 매 단위 통과)
- 모바일 반응형 (`grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4` 등)
- 외부 브랜드 강제 컬러: `naver` (#03C75A), `chzzk` (#00FFA3)

---

## 🔒 권한 모델 (Mock)

자율 모드는 인증/등급을 다음 localStorage 키로 시뮬레이션. **인증 단위(Phase 2-2 백엔드 OAuth) 도입 시 모두 제거 예정.**

| 키 | 값 | 효과 | 사용 단위 |
|---|---|---|---|
| `mock-user` | JSON CurrentUser | 로그인 세션 | 1-11, 1-12 |
| `mock-tier` | seed/pepper/corn/owner | 등급 게이트 | 1-9 (corn+ 만 오프후기) |
| `mock-is-admin` | true/false | 관리자 게이트 | 1-6 (admin 만 팬레터 본문) |
| `mock-is-live` | true/false | 치지직 라이브 토글 | 1-10 LiveBanner |

**Note**: `mock-is-admin`(1-6)과 owner tier(1-9)는 별도 키로 관리 중. 인증 단위에서 통합 검토 필요.

---

## 📁 v2 잔재 처리

- `gochubat/apps/` (NestJS) + `gochubat/packages/shared/` + `pnpm-workspace.yaml` + 루트 `package.json` + `pnpm-lock.yaml` + `tsconfig.base.json` 모두 **gitignore 처리** (v3 셋업 직후)
- 디스크에는 남아있음. 사용자 직접 정리 권장.

---

## 🚀 다음 세션 재개 방법

CLAUDE.md §1-4 프로토콜 그대로:

```
자율 모드 이어서. CLAUDE.md §1-4 따라서 진행.
```

→ git log 보고 1-13부터 자동 이어감.

특정 단위만 시키려면:
```
1-13(글쓰기/수정 폼)부터 진행해줘. 자유게시판 Edit View 1개부터 패턴 굳히고 확장.
```

또는:
```
1-14(관리자 페이지)부터 진행해줘. owner 등급 게이트 도입 + 게시판 관리 드래그&드롭.
```

---

## 🛠️ 사용자 본인 액션 (Phase 2 백엔드 진입 전)

1. **Java 21 설치** (현재 17, Spring Boot 3 권장)
2. **Gradle 8.x** 또는 gradlew wrapper로 우회
3. **MySQL 로컬 셋업** + `gochubat` DB 생성
4. **`backend/src/main/resources/application-local.yml`** placeholder 채우기:
   - `spring.datasource.url/username/password`
   - `naver.client-id/client-secret` (developers.naver.com 발급)
   - `jwt.secret`은 자율 모드에서 64-byte 랜덤 생성하여 이미 채워둠
5. **`frontend/.env.local`** `VITE_NAVER_CLIENT_ID` 채우기
6. **네이버 개발자센터 앱 등록** (서비스 URL `http://localhost:5173`, Callback `http://localhost:8080/api/auth/naver/callback`)

---

## 🤖 reviewer 운용 노트

매 단위 reviewer 호출 결과 통계:
- PASS 일발: 1-1, 1-2, 1-3, 1-4, 1-5(2차), 1-6, 1-7(2차), 1-8, 1-9(2차), 1-10, 1-11(2차), 1-12
- FAIL → 수정 → 재검증 PASS: 1-5 (allowReplies/count-change), 1-7 (PostArticle 추출), 1-9 (isHttpStatus + ViewerTier 죽은코드), 1-11 (Pinia store 과한 추상화 + fetchMe/logout 사전작성)

reviewer는 NAJACKS의 두 실패 패턴(부족 / 과함)을 매번 강제로 잡아냄. 특히 "1-11 스코프 밖 함수 사전 작성" 같은 미세한 과코딩까지 검출. **3회 연속 FAIL 종료 조건 발동 없음**.

이 세션은 user-level `~/.claude/agents/reviewer.md`를 새 세션에서 인식 못 해서 `general-purpose` agent에 reviewer.md 지침을 매번 inject하는 방식으로 작동. **다음 세션은 Claude Code 재시작 후 `subagent_type: reviewer`로 직접 호출 가능.**
