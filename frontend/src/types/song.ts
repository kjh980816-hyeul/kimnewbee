export interface SongRecommendation {
  id: number;
  title: string;
  artist: string;
  link: string;
  submittedBy: string;
  voteCount: number;
  votedByMe: boolean;
  createdAt: string;
}

export interface SongListResponse {
  data: SongRecommendation[];
  total: number;
}

export interface CreateSongInput {
  title: string;
  artist: string;
  link: string;
}

export interface VoteToggleResponse {
  voted: boolean;
  voteCount: number;
}
