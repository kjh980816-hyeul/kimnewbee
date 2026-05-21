export interface SearchHit {
  type: 'notice' | 'post';
  id: number;
  title: string;
  snippet: string;
  boardSlug: string;
  boardLabel: string;
  author: string;
  createdAt: string;
}

export interface SearchResponse {
  query: string;
  total: number;
  hits: SearchHit[];
}
