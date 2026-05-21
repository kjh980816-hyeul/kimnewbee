import { apiClient } from './client';
import type { AttendanceStatus, CheckInResult } from '@/types/attendance';

export async function fetchAttendanceStatus(): Promise<AttendanceStatus> {
  const res = await apiClient.get<AttendanceStatus>('/api/me/checkin/status');
  return res.data;
}

export async function postCheckIn(): Promise<CheckInResult> {
  const res = await apiClient.post<CheckInResult>('/api/me/checkin');
  return res.data;
}
