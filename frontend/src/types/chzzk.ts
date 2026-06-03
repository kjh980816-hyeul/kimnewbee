export interface LiveStatus {
  isLive: boolean;
  title: string;
  viewerCount: number;
  startedAt: string | null;
  channelUrl: string;
  thumbnailUrl: string | null;
}
