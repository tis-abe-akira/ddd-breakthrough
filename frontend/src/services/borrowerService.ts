/**
 * @fileoverview 借入人関連のAPI通信を行うサービス
 * @module services/borrowerService
 */

import axios from 'axios';
import { Borrower, NewBorrowerInput } from '../types/borrower';

const API_BASE_URL = '/api/borrowers';

/**
 * すべての借入人データを取得する
 *
 * @async
 * @function getBorrowers
 * @returns {Promise<Borrower[]>} 借入人データの配列
 * @throws {Error} API通信に失敗した場合
 */
export const getBorrowers = async (): Promise<Borrower[]> => {
  try {
    const response = await axios.get<Borrower[]>(API_BASE_URL);
    return response.data;
  } catch (error) {
    console.error('借入人データの取得に失敗しました', error);
    throw error;
  }
};

/**
 * 指定IDの借入人データを取得する
 *
 * @async
 * @function getBorrowerById
 * @param {number} id - 取得する借入人のID
 * @returns {Promise<Borrower>} 借入人データ
 * @throws {Error} API通信に失敗した場合
 */
export const getBorrowerById = async (id: number): Promise<Borrower> => {
  try {
    const response = await axios.get<Borrower>(`${API_BASE_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error(`ID ${id} の借入人データの取得に失敗しました`, error);
    throw error;
  }
};

/**
 * 新しい借入人を登録する
 *
 * @async
 * @function createBorrower
 * @param {NewBorrowerInput} borrowerData - 新規登録する借入人データ
 * @returns {Promise<Borrower>} 登録された借入人データ
 * @throws {Error} API通信に失敗した場合
 */
export const createBorrower = async (
  borrowerData: NewBorrowerInput
): Promise<Borrower> => {
  try {
    const response = await axios.post<Borrower>(API_BASE_URL, borrowerData);
    return response.data;
  } catch (error) {
    console.error('借入人の登録に失敗しました', error);
    throw error;
  }
};
