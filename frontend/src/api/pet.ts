import { apiClient } from './client';
import type { Pet, PetListResponse, CreatePetInput } from '@/types/pet';
import type { LikeToggleResponse } from '@/types/free';

export async function fetchPets(): Promise<PetListResponse> {
  const res = await apiClient.get<PetListResponse>('/api/pets');
  return res.data;
}

export async function fetchPet(id: number): Promise<Pet> {
  const res = await apiClient.get<Pet>(`/api/pets/${id}`);
  return res.data;
}

export async function createPet(input: CreatePetInput): Promise<Pet> {
  const res = await apiClient.post<Pet>('/api/pets', input);
  return res.data;
}

export async function togglePetLike(id: number): Promise<LikeToggleResponse> {
  const res = await apiClient.post<LikeToggleResponse>(`/api/pets/${id}/like`);
  return res.data;
}
