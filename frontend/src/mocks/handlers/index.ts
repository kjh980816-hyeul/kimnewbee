import type { HttpHandler } from 'msw';
import { noticeHandlers } from './notice';
import { freeHandlers } from './free';
import { commentHandlers } from './comment';
import { fanartHandlers } from './fanart';
import { clipHandlers } from './clip';
import { letterHandlers } from './letter';
import { petHandlers } from './pet';
import { songHandlers } from './song';

export const handlers: HttpHandler[] = [
  ...noticeHandlers,
  ...freeHandlers,
  ...commentHandlers,
  ...fanartHandlers,
  ...clipHandlers,
  ...letterHandlers,
  ...petHandlers,
  ...songHandlers,
];
