import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetReturnOrderQuery, AssetReturnOrderVO } from './types';

export const listAssetReturnOrder = (query: AssetReturnOrderQuery): AxiosPromise<AssetReturnOrderVO[]> =>
  request({ url: '/asset/return/list', method: 'get', params: query });

export const getAssetReturnOrder = (returnId: string | number): AxiosPromise<AssetReturnOrderVO> =>
  request({ url: `/asset/return/${returnId}`, method: 'get' });

export const executeAssetReturnOrder = (returnId: string | number) => request({ url: `/asset/return/${returnId}/execute`, method: 'post' });
