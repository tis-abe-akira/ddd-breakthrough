/**
 * @fileoverview 借入人関連の型定義
 * @module types/borrower
 */

/**
 * 借入人データの型定義
 *
 * @typedef {Object} Borrower
 * @property {number} id - 借入人ID
 * @property {string} name - 借入人名
 * @property {string} industry - 業種
 * @property {string} companyType - 企業形態
 * @property {string} [creditRating] - 信用格付け（オプショナル）
 * @property {Date} createdAt - 作成日時
 * @property {Date} updatedAt - 更新日時
 */
export interface Borrower {
  id: number;
  name: string;
  industry: string;
  companyType: string;
  creditRating?: string;
  createdAt: Date;
  updatedAt: Date;
}

/**
 * 新規借入人登録用の入力データ型定義
 *
 * @typedef {Object} NewBorrowerInput
 * @property {string} name - 借入人名
 * @property {string} industry - 業種
 * @property {string} companyType - 企業形態
 * @property {string} [creditRating] - 信用格付け（オプショナル）
 */
export interface NewBorrowerInput {
  name: string;
  industry: string;
  companyType: string;
  creditRating?: string;
}

/**
 * 借入人検索条件の型定義
 *
 * @typedef {Object} BorrowerSearchCriteria
 * @property {string} [name] - 借入人名（部分一致）
 * @property {string} [industry] - 業種（完全一致）
 * @property {string} [creditRating] - 信用格付け（完全一致）
 */
export interface BorrowerSearchCriteria {
  name?: string;
  industry?: string;
  creditRating?: string;
}
