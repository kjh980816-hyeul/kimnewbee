# 🌙 자율 모드 시작 프롬프트 (한 번 던지고 자면 끝)

> 이 텍스트 블록 통째로 복사해서 Claude Code에 붙여넣고 자러 가.

---

## 복붙용 프롬프트

```
자율 작업 모드를 시작한다. 깨우지 마. 끝까지 알아서 가.

【1단계: 헌법 숙지】
다음 파일을 통째로 다 읽어:
1. CLAUDE.md (작업 헌법)
2. .claude/agents/reviewer.md (검증 게이트)
3. docs/고추밭_기획서.md (디자인/IA/컬러)

읽고 나면 CLAUDE.md §1(자율 모드 운영 원칙), §2(절대 원칙 5개), §9(작업 큐) 세 섹션을 머릿속에 새기고 시작.

【2단계: 환경 점검】
- `git log --oneline -20` 으로 어디까지 했는지 확인
- `ls -la BLOCKED.md` 있으면 읽고 차단 사유 파악
- `frontend/.env.local` 과 `backend/src/main/resources/application-local.yml` 존재 여부 확인 (없어도 일단 시작 — Phase 1 프론트는 환경변수 없어도 됨)

【3단계: 작업 큐 소화】
CLAUDE.md §9의 작업 큐를 위에서 아래로 차례대로 소화.
- 이미 끝난 단위(git 히스토리에서 확인)는 스킵하고 첫 미완 단위부터.
- Phase 4(가비아 배포)는 외부 정보 필요 → 도달하면 BLOCKED.md에 기록 후 종료.

각 단위마다:
1. 단위 시작 선언 (어떤 파일을 만들/수정할지 한 줄)
2. 코드 작업 진행
3. 끝나면 Task tool로 reviewer 호출:
   subagent_type: reviewer
   prompt: "[단위명] 완료. [한 일 요약 1-2문장]. [FRONT|BACKEND|INTEGRATION] 검증 부탁."
4. PASS 받으면:
   - `git add . && git commit -m "..." && git push origin main`
   - 곧바로 다음 단위 시작 (사용자 호출 X)
5. FAIL 받으면:
   - 지적 항목 전부 수정
   - reviewer 재호출
   - PASS 받을 때까지 반복

【4단계: 종료 조건】
다음 중 하나면 멈추고 종료:
- 작업 큐 모두 완료 → 최종 요약을 SUMMARY.md에 작성
- 같은 게이트에서 reviewer가 3회 연속 FAIL → BLOCKED.md에 상황 기록 후 종료
- 외부 정보 필요한 단위 (DB 비번/도메인 등) → BLOCKED.md에 기록하고 다음 단위로 (Phase 4는 모두 SKIP)
- 같은 파일을 5회 이상 수정 중 → 멈추고 BLOCKED.md에 접근 방식 재고 사유 기록 후 종료

종료 시 반드시 마지막 commit 메시지에 "[autonomous-end] ..." prefix.

【5단계: 작업 원칙 (귀찮아도 절대 양보 X)】
A. 인라인 더미 데이터 절대 금지 — MSW로 빼라
B. 시크릿(네이버 Secret/JWT Secret 등) 코드/문서에 박지 마라 — .env / application-local.yml만
C. 작업 단위 끝나면 무조건 reviewer 호출
D. reviewer FAIL → 무조건 수정 (양보 X)
E. 과코딩 금지 — 요구사항에 없는 추상화/일반화/추가 기능 X
   - "혹시 나중에..." 라는 이유로 만들지 마
   - 인터페이스 + 구현체 1개? 만들지 마
   - generic 한 곳에서만 쓰임? 일반화하지 마
   - 헬퍼 1번만 호출? 인라인이 낫다
   - 같은 패턴 3번 반복돼야 추상화
F. 모든 컬러는 CSS 변수로
G. TypeScript strict 유지, any 금지
H. "TODO: 나중에" 류 미래시제 주석 금지
I. 단위 테스트 매번 작성 (가짜 테스트 X)
J. 커밋 메시지 컨벤션 준수 (feat/fix/chore/refactor/test/docs)

【6단계: 출력 형식】
- 단위 시작 시: "▶ [단위명] 시작" 한 줄
- 단위 종료 시: "■ [단위명] PASS — commit: {hash}" 한 줄
- FAIL 시: "✗ [단위명] FAIL — 사유: {요약}" + 수정 진행
- 진행 중간 사용자에게 묻는 것 일절 금지 (자율 모드)

자, 시작.
```

---

## 자고 일어나서 확인할 것

1. **`git log --oneline -50`** — 어디까지 갔는지
2. **`BLOCKED.md`** 있으면 → 어디서 막혔는지
3. **`SUMMARY.md`** 있으면 → 큐 다 끝남 (대박)
4. **현재 세션 마지막 메시지** — 자율 모드가 정상 종료됐는지, 토큰 한도로 끊긴 건지

## 토큰 한도로 끊긴 경우 다음 세션 시작

```
자율 모드 이어서. CLAUDE.md §1-4 (다음 세션 재개 방식) 따라서 진행.
```

이거 한 줄이면 git log 보고 알아서 이어감.

## 잘 안 되면 (예: 같은 단위에서 계속 빠꾸)

```
멈춰. BLOCKED.md 있으면 읽어주고, 마지막 reviewer 빠꾸 사유랑 너의 시도 요약해줘. 내가 판단할게.
```

## 처음 자기 전 한 번 점검

자기 전에 30분 정도 앉아서 첫 1-2개 단위가 잘 진행되는지 보는 거 추천. 첫 단위(인프라 셋업)에서 reviewer가 통과하는 흐름이 자리 잡으면 그 다음부터는 같은 패턴 반복이라 안심하고 잘 수 있음.
