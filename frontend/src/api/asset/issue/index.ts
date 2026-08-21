import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { AssetIssueOrderQuery, AssetIssueOrderVO } from './types';

export const listAssetIssueOrder = (query: AssetIssueOrderQuery): AxiosPromise<AssetIssueOrderVO[]> =>
  request({ url: '/asset/issue/list', method: 'get', params: query });

export const getAssetIssueOrder = (issueId: string | number): AxiosPromise<AssetIssueOrderVO> =>
  request({ url: `/asset/issue/${issueId}`, method: 'get' });

export const executeAssetIssueOrder = (issueId: string | number) => request({ url: `/asset/issue/${issueId}/execute`, method: 'post' });
