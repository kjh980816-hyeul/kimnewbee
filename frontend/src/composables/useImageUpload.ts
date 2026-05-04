import { ref, type Ref } from 'vue';
import { uploadImage } from '@/api/upload';
import { isHttpStatus } from '@/api/error';

export function useImageUpload(imageUrlRef: Ref<string>) {
  const uploading = ref(false);
  const uploadError = ref<string | null>(null);

  async function pickAndUpload(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    uploading.value = true;
    uploadError.value = null;
    try {
      const res = await uploadImage(file);
      imageUrlRef.value = res.url;
    } catch (e) {
      if (isHttpStatus(e, 401)) {
        uploadError.value = '로그인이 필요해요';
      } else if (isHttpStatus(e, 400)) {
        uploadError.value = '이미지 형식 또는 크기를 확인해주세요 (10MB, png/jpg/webp/gif)';
      } else {
        uploadError.value = e instanceof Error ? e.message : '업로드에 실패했어요';
      }
    } finally {
      uploading.value = false;
      input.value = '';
    }
  }

  return { uploading, uploadError, pickAndUpload };
}
