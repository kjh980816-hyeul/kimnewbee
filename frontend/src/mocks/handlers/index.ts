import type { HttpHandler } from 'msw';
import { noticeHandlers } from './notice';
import { freeHandlers } from './free';
import { commentHandlers } from './comment';

export const handlers: HttpHandler[] = [
  ...noticeHandlers,
  ...freeHandlers,
  ...commentHandlers,
];
