import { AssetApplicationItemForm } from '@/api/asset/application/types';

export interface AssetReturnOrderVO extends BaseEntity {
  returnId: string | number;
  returnNo: string;
  applicationId: string | number;
  applicationNo?: string;
  applicationTitle?: string;
  returnerId: string | number;
  returnerName?: string;
  returnDeptId: string | number;
  returnDeptName?: string;
  status: string;
  returnedBy?: string | number;
  returnedByName?: string;
  returnedTime?: string;
  version: number;
  items?: AssetApplicationItemForm[];
}

export interface AssetReturnOrderQuery extends PageQuery {
  returnNo?: string;
  status?: string;
  returnerId?: string | number;
  returnDeptId?: string | number;
}
