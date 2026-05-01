export interface PetListItem {
  id: number;
  title: string;
  author: string;
  thumbnailUrl: string;
  createdAt: string;
  likeCount: number;
  commentCount: number;
}

export interface Pet extends PetListItem {
  imageUrl: string;
  content: string;
  viewCount: number;
  updatedAt: string;
  likedByMe: boolean;
}

export interface PetListResponse {
  data: PetListItem[];
  total: number;
}

export interface CreatePetInput {
  title: string;
  imageUrl: string;
  content: string;
}
