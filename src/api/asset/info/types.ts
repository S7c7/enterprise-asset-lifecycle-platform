export interface AssetInfoVO extends BaseEntity {
  assetId: string | number;
  assetCode: string;
  assetName: string;
  categoryId: string | number;
  categoryName?: string;
  specification?: string;
  brand?: string;
  unit?: string;
  purchaseDate?: string;
  originalValue?: number;
  assetStatus: string;
  deptId?: string | number;
  deptName?: string;
  keeperId?: string | number;
  keeperName?: string;
  location?: string;
  warrantyExpiryDate?: string;
  remark?: string;
}

export interface AssetInfoForm extends BaseEntity {
  assetId?: string | number;
  assetCode: string;
  assetName: string;
  categoryId?: string | number;
  specification?: string;
  brand?: string;
  unit?: string;
  purchaseDate?: string;
  originalValue?: number;
  assetStatus: string;
  deptId?: string | number;
  keeperId?: string | number;
  keeperName?: string;
  location?: string;
  warrantyExpiryDate?: string;
  remark?: string;
}

export interface AssetInfoQuery extends PageQuery {
  assetCode?: string;
  assetName?: string;
  categoryId?: string | number;
  assetStatus?: string;
  deptId?: string | number;
}
