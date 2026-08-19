export interface AssetCategoryVO extends BaseEntity {
  categoryId: string | number;
  parentId: string | number;
  parentName?: string;
  categoryCode: string;
  categoryName: string;
  depreciationYears: number;
  orderNum: number;
  status: string;
  remark?: string;
  children?: AssetCategoryVO[];
}

export interface AssetCategoryForm extends BaseEntity {
  categoryId?: string | number;
  parentId?: string | number;
  categoryCode: string;
  categoryName: string;
  depreciationYears: number;
  orderNum: number;
  status: string;
  remark?: string;
}

export interface AssetCategoryQuery {
  categoryCode?: string;
  categoryName?: string;
  status?: string;
}
