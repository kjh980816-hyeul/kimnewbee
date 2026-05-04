import type { HttpHandler } from 'msw';
import { noticeHandlers } from './notice';
import { freeHandlers } from './free';
import { commentHandlers } from './comment';
import { fanartHandlers } from './fanart';
import { clipHandlers } from './clip';
import { letterHandlers } from './letter';
import { petHandlers } from './pet';
import { songHandlers } from './song';
import { offlineHandlers } from './offline';
import { chzzkHandlers } from './chzzk';
import { authHandlers } from './auth';
import { adminHandlers } from './admin';
import { uploadHandlers } from './upload';

export const handlers: HttpHandler[] = [
  ...noticeHandlers,
  ...freeHandlers,
  ...commentHandlers,
  ...fanartHandlers,
  ...clipHandlers,
  ...letterHandlers,
  ...petHandlers,
  ...songHandlers,
  ...offlineHandlers,
  ...chzzkHandlers,
  ...authHandlers,
  ...adminHandlers,
  ...uploadHandlers,
];
