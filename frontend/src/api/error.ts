import axios from 'axios';

export function isHttpStatus(e: unknown, status: number): boolean {
  return axios.isAxiosError(e) && e.response?.status === status;
}

// axios 기본 메시지("Request failed with status code 401")를 그대로 보여주지 않고,
// 상태코드별 친화적 한국어 메시지로 바꾼다. 400은 백엔드의 사용자용 메시지가 깔끔하면 그걸 쓰고
// (필드명이 섞인 검증 메시지 "title: ..."는 제외) 아니면 fallback을 쓴다.
export function errorMessage(e: unknown, fallback: string): string {
  if (!axios.isAxiosError(e)) {
    return fallback;
  }
  if (!e.response) {
    return '네트워크 연결을 확인해주세요.';
  }
  const status = e.response.status;
  if (status === 401) return '로그인이 필요해요. 다시 로그인해주세요.';
  if (status === 403) return '권한이 없어요.';
  if (status === 413) return '파일이 너무 커요 (최대 10MB).';
  if (status >= 500) return '서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요.';

  const backendMessage = (e.response.data as { message?: string } | undefined)?.message;
  if (backendMessage && !backendMessage.includes(':')) {
    return backendMessage;
  }
  return fallback;
}
