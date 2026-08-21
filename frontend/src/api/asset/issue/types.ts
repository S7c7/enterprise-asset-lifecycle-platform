import { AssetApplicationItemForm } from '@/api/asset/application/types';

export interface AssetIssueOrderVO extends BaseEntity {
  issueId: string | number;
  issueNo: string;
  applicationId: string | number;
  applicationNo?: string;
  applicationTitle?: string;
  recipientId: string | number;
  recipientName?: string;
  recipientDeptId: string | number;
  recipientDeptName?: string;
  status: string;
  issuedBy?: string | number;
  issuedByName?: string;
  issuedTime?: string;
  version: number;
  items?: AssetApplicationItemForm[];
}

export interface AssetIssueOrderQuery extends PageQuery {
  issueNo?: string;
  status?: string;
  recipientId?: string | number;
  recipientDeptId?: string | number;
}
