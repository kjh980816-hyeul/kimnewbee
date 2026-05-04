import { http, HttpResponse } from 'msw';
import { petFixtures } from '../data/pets';
import { createOwnerOnlyPatch } from '../utils/createOwnerOnlyPatch';
import type { Pet, PetListItem, CreatePetInput } from '@/types/pet';

const API_URL = import.meta.env.VITE_API_URL;

const petStore: Pet[] = petFixtures.map((p) => ({ ...p }));
let nextPetId = 5000;

export const petHandlers = [
  http.get(`${API_URL}/api/pets`, () => {
    const list: PetListItem[] = petStore.map((p) => ({
      id: p.id,
      title: p.title,
      author: p.author,
      thumbnailUrl: p.thumbnailUrl,
      createdAt: p.createdAt,
      likeCount: p.likeCount,
      commentCount: p.commentCount,
    }));
    return HttpResponse.json({ data: list, total: list.length });
  }),

  http.get(`${API_URL}/api/pets/:id`, ({ params }) => {
    const id = Number(params['id']);
    const pet = petStore.find((p) => p.id === id);
    if (!pet) {
      return HttpResponse.json({ message: '사진을 찾을 수 없어요' }, { status: 404 });
    }
    pet.viewCount += 1;
    return HttpResponse.json(pet);
  }),

  http.post(`${API_URL}/api/pets`, async ({ request }) => {
    const body = (await request.json()) as CreatePetInput;
    const now = new Date().toISOString();
    const newPet: Pet = {
      id: nextPetId++,
      title: body.title,
      imageUrl: body.imageUrl,
      thumbnailUrl: body.imageUrl,
      content: body.content,
      author: '초록고추',
      createdAt: now,
      updatedAt: now,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      likedByMe: false,
    };
    petStore.unshift(newPet);
    return HttpResponse.json(newPet, { status: 201 });
  }),

  http.patch(
    `${API_URL}/api/pets/:id`,
    createOwnerOnlyPatch<Pet, CreatePetInput>(petStore, (pet, body) => {
      pet.title = body.title;
      pet.imageUrl = body.imageUrl;
      pet.thumbnailUrl = body.imageUrl;
      pet.content = body.content;
    }),
  ),

  http.post(`${API_URL}/api/pets/:id/like`, ({ params }) => {
    const id = Number(params['id']);
    const pet = petStore.find((p) => p.id === id);
    if (!pet) {
      return HttpResponse.json({ message: '사진을 찾을 수 없어요' }, { status: 404 });
    }
    pet.likedByMe = !pet.likedByMe;
    pet.likeCount += pet.likedByMe ? 1 : -1;
    return HttpResponse.json({ liked: pet.likedByMe, likeCount: pet.likeCount });
  }),
];
