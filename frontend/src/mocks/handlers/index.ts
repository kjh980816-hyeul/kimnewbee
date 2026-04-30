import type { HttpHandler } from 'msw';
import { noticeHandlers } from './notice';

export const handlers: HttpHandler[] = [...noticeHandlers];
