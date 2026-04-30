import type { HttpHandler } from 'msw';
import { noticeHandlers } from './notice';
import { freeHandlers } from './free';
import { commentHandlers } from './comment';
import { fanartHandlers } from './fanart';

export const handlers: HttpHandler[] = [
  ...noticeHandlers,
  ...freeHandlers,
  ...commentHandlers,
  ...fanartHandlers,
];
