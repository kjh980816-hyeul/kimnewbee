export interface CafeConfig {
  heroBannerUrl: string | null;
  heroHeadline: string;
  heroSubtext: string | null;
  footerText: string | null;
  chzzkChannelId: string | null;
  updatedAt: string;
}

export interface CafeConfigUpdateInput {
  heroBannerUrl: string;
  heroHeadline: string;
  heroSubtext: string;
  footerText: string;
  chzzkChannelId: string;
}
