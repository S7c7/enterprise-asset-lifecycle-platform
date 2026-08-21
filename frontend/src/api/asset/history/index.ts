import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetHistoryQuery, AssetHistoryVO } from './types';

export const listAssetHistory = (query: AssetHistoryQuery): AxiosPromise<AssetHistoryVO[]> =>
  request({ url: '/asset/history/list', method: 'get', params: query });
