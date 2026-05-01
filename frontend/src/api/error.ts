import axios from 'axios';

export function isHttpStatus(e: unknown, status: number): boolean {
  return axios.isAxiosError(e) && e.response?.status === status;
}
