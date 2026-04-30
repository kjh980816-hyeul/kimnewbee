# 🛠 자기 전 셋업 (자율 모드 실행 전 5분 안에 끝)

## 1. zip 풀고 git에 추가

```bash
# 김늉비 프로젝트 루트에서
unzip gochubat-handoff-v3.zip
cp -r handoff-v3/* handoff-v3/.[!.]* .
rm -rf handoff-v3/

# 기존 CLAUDE.md, .gitignore 있으면 백업 후 덮어쓰기
# (이미 v2 받았으면 이걸로 덮어쓰면 됨)
```

## 2. 시크릿 재발급 (강력 권장)

이전 채팅에서 노출된 네이버 시크릿은 **재발급** 해.
- developers.naver.com → 내 애플리케이션 → 재발급

## 3. 환경 파일 만들기 (★ 자율 모드 전 반드시)

### 프론트
```bash
cp .env.example frontend/.env.local
# frontend/.env.local 열어서 VITE_NAVER_CLIENT_ID에 새로 발급받은 값 넣기
```

### 백엔드
```bash
mkdir -p backend/src/main/resources/
cp application-local.example.yml backend/src/main/resources/application-local.yml
# 위 파일 열어서 실제 값들 채우기:
#   - DB url/username/password (가비아 MySQL 정보)
#   - naver.client-id (재발급받은 ID)
#   - naver.client-secret (재발급받은 Secret)
#   - jwt.secret (아래 명령으로 64자 랜덤 만들어서 넣기)
```

JWT Secret 생성 (랜덤 64자):
```bash
openssl rand -base64 64
# 또는
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

## 4. .gitignore 적용 확인 (★ 시크릿이 push 되면 안 됨)

```bash
git status
# .env.local, application-local.yml 이 보이면 안 됨 (untracked로도 X)
git check-ignore frontend/.env.local backend/src/main/resources/application-local.yml
# 둘 다 출력되면 OK (gitignore에 잡힘)
```

## 5. 네이버 개발자센터 등록 (이미 했으면 스킵)

developers.naver.com → 애플리케이션 등록:
- 사용 API: 네이버 로그인
- 서비스 URL: `http://localhost:5173`
- Callback URL: `http://localhost:8080/api/auth/naver/callback`

## 6. Claude Code 실행

```bash
cd kimnewbee
claude
```

`AUTONOMOUS_PROMPT.md` 의 코드블록 통째로 복사해서 붙여넣기 → 엔터.

## 7. 30분 정도 지켜보기 (선택)

첫 단위(인프라 셋업)에서 reviewer가 PASS 주는 흐름이 자리 잡으면 안심하고 자도 됨.
이상하다 싶으면 `Ctrl+C` 로 멈추고 `BLOCKED.md` 확인.

## 8. 자고 일어나서

```bash
git log --oneline -50
ls BLOCKED.md SUMMARY.md 2>/dev/null
```

- 커밋이 줄줄이 있고 SUMMARY.md 있으면 → 큐 다 끝남 🎉
- BLOCKED.md 있으면 → 거기 적힌 사유 보고 그 부분만 사람 판단으로 풀어주고 다시 자율 모드
- 커밋이 몇 개 없고 멈춰있으면 → 토큰 한도 도달. 새 세션에서 "자율 모드 이어서. CLAUDE.md §1-4 따라서 진행." 한 줄로 재개

## 토큰 사용량 주의

자율 모드는 토큰 많이 씀. Claude Pro/Max 구독자도 5시간 윈도우 한도 있음.
한도 도달하면 자동 종료되니 위험은 X. 다만 한 번에 다 못 끝낼 수 있음.
일주일 정도는 매일 밤 한 세션씩 돌리는 거 가정하고 시작.
