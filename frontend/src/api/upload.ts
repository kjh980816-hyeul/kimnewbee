import { apiClient } from './client';
import type { UploadResponse } from '@/types/upload';

export async function uploadImage(file: File): Promise<UploadResponse> {
  const form = new FormData();
  form.append('file', file);
  const res = await apiClient.post<UploadResponse>('/api/files', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}
