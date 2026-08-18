import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetInfoForm, AssetInfoQuery, AssetInfoVO } from './types';

export const listAssetInfo = (query: AssetInfoQuery): AxiosPromise<AssetInfoVO[]> => {
  return request({ url: '/asset/info/list', method: 'get', params: query });
};

export const getAssetInfo = (assetId: string | number): AxiosPromise<AssetInfoVO> => {
  return request({ url: `/asset/info/${assetId}`, method: 'get' });
};

export const addAssetInfo = (data: AssetInfoForm) => {
  return request({ url: '/asset/info', method: 'post', data });
};

export const updateAssetInfo = (data: AssetInfoForm) => {
  return request({ url: '/asset/info', method: 'put', data });
};

export const delAssetInfo = (assetIds: string | number | Array<string | number>) => {
  return request({ url: `/asset/info/${assetIds}`, method: 'delete' });
};
