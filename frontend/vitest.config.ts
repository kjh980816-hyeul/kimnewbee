import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'happy-dom',
      globals: true,
      setupFiles: ['./tests/setup.ts'],
      include: ['tests/**/*.spec.ts', 'src/**/*.spec.ts'],
    },
  }),
);
