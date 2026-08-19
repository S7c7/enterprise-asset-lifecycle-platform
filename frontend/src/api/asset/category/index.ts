import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetCategoryForm, AssetCategoryQuery, AssetCategoryVO } from './types';

export const listAssetCategory = (query?: AssetCategoryQuery): AxiosPromise<AssetCategoryVO[]> => {
  return request({ url: '/asset/category/list', method: 'get', params: query });
};

export const listAssetCategoryOptions = (): AxiosPromise<AssetCategoryVO[]> => {
  return request({ url: '/asset/category/options', method: 'get' });
};

export const getAssetCategory = (categoryId: string | number): AxiosPromise<AssetCategoryVO> => {
  return request({ url: `/asset/category/${categoryId}`, method: 'get' });
};

export const addAssetCategory = (data: AssetCategoryForm) => {
  return request({ url: '/asset/category', method: 'post', data });
};

export const updateAssetCategory = (data: AssetCategoryForm) => {
  return request({ url: '/asset/category', method: 'put', data });
};

export const delAssetCategory = (categoryIds: string | number | Array<string | number>) => {
  return request({ url: `/asset/category/${categoryIds}`, method: 'delete' });
};
