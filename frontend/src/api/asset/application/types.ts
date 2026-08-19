export interface AssetApplicationItemForm {
  itemId?: string | number;
  assetId?: string | number;
  categoryId?: string | number;
  categoryName?: string;
  itemName: string;
  specification?: string;
  quantity: number;
  unit: string;
  estimatedUnitPrice: number;
  targetDeptId?: string | number;
  targetDeptName?: string;
  targetLocation?: string;
  remark?: string;
}

export interface AssetApplicationVO extends BaseEntity {
  applicationId: string | number;
  applicationNo: string;
  applicationType: string;
  title: string;
  applicantId: string | number;
  applicantName?: string;
  applyDeptId: string | number;
  applyDeptName?: string;
  totalAmount: number;
  expectedDate?: string;
  reason: string;
  status: string;
  flowCode: string;
  items: AssetApplicationItemForm[];
}

export interface AssetApplicationForm extends BaseEntity {
  applicationId?: string | number;
  applicationType: string;
  title: string;
  expectedDate?: string;
  reason: string;
  remark?: string;
  items: AssetApplicationItemForm[];
}

export interface AssetApplicationQuery extends PageQuery {
  applicationNo?: string;
  title?: string;
  applicationType?: string;
  status?: string;
}
