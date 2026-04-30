export interface LetterListItem {
  id: number;
  author: string;
  preview: string;
  createdAt: string;
}

export interface Letter extends LetterListItem {
  content: string;
  isReadByAdmin: boolean;
  updatedAt: string;
}

export interface LetterListResponse {
  data: LetterListItem[];
  total: number;
}

export interface CreateLetterInput {
  content: string;
}

export interface AdminViewerInfo {
  isAdmin: boolean;
}
