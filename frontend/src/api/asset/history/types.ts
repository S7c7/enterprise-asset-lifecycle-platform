export interface AssetHistoryVO {
  historyId: string | number;
  assetId: string | number;
  assetCode: string;
  assetName: string;
  businessType: string;
  businessId: string | number;
  beforeStatus?: string;
  afterStatus?: string;
  beforeDeptName?: string;
  afterDeptName?: string;
  beforeKeeperName?: string;
  afterKeeperName?: string;
  beforeLocation?: string;
  afterLocation?: string;
  operatorName?: string;
  operationTime: string;
  description?: string;
}

export interface AssetHistoryQuery extends PageQuery {
  assetId?: string | number;
  assetCode?: string;
  assetName?: string;
  businessType?: string;
  businessId?: string | number;
}
