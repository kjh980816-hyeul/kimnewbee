export type HeroBannerPosition = 'top' | 'center' | 'bottom' | 'left' | 'right';

export interface CafeConfig {
  heroBannerUrl: string | null;
  heroBannerPosition: HeroBannerPosition | null;
  heroHeadline: string;
  heroSubtext: string | null;
  footerText: string | null;
  chzzkChannelId: string | null;
  updatedAt: string;
}

export interface CafeConfigUpdateInput {
  heroBannerUrl: string;
  heroBannerPosition: HeroBannerPosition;
  heroHeadline: string;
  heroSubtext: string;
  footerText: string;
  chzzkChannelId: string;
}
