# 고추밭 Frontend

Vue 3 + Vite + TypeScript (strict) + Pinia + Vue Router + Axios + MSW + Tailwind + Vitest.

## 셋업

```bash
npm install
```

## 개발

```bash
npm run dev
```

`.env.local`의 `VITE_USE_MOCK=true`면 MSW 활성화, `false`면 실 백엔드 호출.

## 검증

```bash
npm run typecheck   # vue-tsc strict
npm run build       # 프로덕션 빌드
npm run test        # vitest 단위 테스트
npx ts-prune        # 죽은 코드 검출
```

## 환경변수 (`.env.local`)

| 키 | 설명 |
|---|---|
| `VITE_API_URL` | 백엔드 베이스 URL (개발: http://localhost:8080) |
| `VITE_USE_MOCK` | `true`면 MSW 활성화, `false`면 실 API |
| `VITE_NAVER_CLIENT_ID` | 네이버 OAuth Client ID (Secret은 백엔드만) |

## 폴더 구조

`CLAUDE.md §4` 참고. 주요 원칙:
- `src/api/`: axios 함수만. 컴포넌트는 여기서만 호출
- `src/mocks/`: MSW handler/data — **컴포넌트에 더미 박지 마라**
- `src/assets/tokens.css`: 컬러 CSS 변수 — 직 hex 금지
