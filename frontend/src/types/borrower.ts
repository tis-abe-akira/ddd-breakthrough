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
 * @property {string} [contactInfo] - 連絡先情報（オプショナル）
 * @property {string} [createdAt] - 作成日時（オプショナル）
 * @property {string} [updatedAt] - 更新日時（オプショナル）
 * @property {number} [version] - バージョン情報（オプティミスティックロック用）
 */
export interface Borrower {
  id: number;
  name: string;
  industry: string;
  companyType: string;
  creditRating?: string;
  contactInfo?: string;
  createdAt?: string;
  updatedAt?: string;
  version?: number; // バージョン情報を追加
}

/**
 * 新規借入人登録用の入力データ型定義
 *
 * @typedef {Object} NewBorrowerInput
 * @property {string} name - 借入人名
 * @property {string} industry - 業種
 * @property {string} companyType - 企業形態
 * @property {string} [creditRating] - 信用格付け（オプショナル）
 * @property {string} [contactInfo] - 連絡先情報（オプショナル）
 * @property {number} [version] - バージョン情報（更新時に必要）
 */
export interface NewBorrowerInput {
  name: string;
  industry: string;
  companyType: string;
  creditRating?: string;
  contactInfo?: string;
  version?: number; // バージョン情報を追加
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
