import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetApplicationForm, AssetApplicationQuery, AssetApplicationVO } from './types';

export const listAssetApplication = (query: AssetApplicationQuery): AxiosPromise<AssetApplicationVO[]> =>
  request({ url: '/asset/application/list', method: 'get', params: query });

export const getAssetApplication = (applicationId: string | number, taskId?: string | number): AxiosPromise<AssetApplicationVO> =>
  request({ url: `/asset/application/${applicationId}`, method: 'get', params: { taskId } });

export const addAssetApplication = (data: AssetApplicationForm): AxiosPromise<AssetApplicationVO> =>
  request({ url: '/asset/application', method: 'post', data });

export const updateAssetApplication = (data: AssetApplicationForm): AxiosPromise<AssetApplicationVO> =>
  request({ url: '/asset/application', method: 'put', data });

export const submitAssetApplication = (applicationId: string | number) =>
  request({ url: `/asset/application/${applicationId}/submit`, method: 'post' });

export const delAssetApplication = (applicationIds: string | number | Array<string | number>) =>
  request({ url: `/asset/application/${applicationIds}`, method: 'delete' });
