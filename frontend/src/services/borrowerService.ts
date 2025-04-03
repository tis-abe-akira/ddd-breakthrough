import axios from 'axios';
import { Borrower } from '../types/borrower';

const API_URL = 'http://localhost:8080/api';

export const BorrowerService = {
  // 全件取得
  getAllBorrowers: async (): Promise<Borrower[]> => {
    try {
      const response = await axios.get(`${API_URL}/borrowers`);
      return response.data;
    } catch (error) {
      console.error('Error fetching borrowers:', error);
      throw error;
    }
  },

  // 1件取得
  getBorrowerById: async (id: number): Promise<Borrower> => {
    try {
      const response = await axios.get(`${API_URL}/borrowers/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching borrower with id ${id}:`, error);
      throw error;
    }
  },

  // 新規作成
  createBorrower: async (borrower: Borrower): Promise<Borrower> => {
    try {
      const response = await axios.post(`${API_URL}/borrowers`, borrower);
      return response.data;
    } catch (error) {
      console.error('Error creating borrower:', error);
      throw error;
    }
  },

  // 更新
  updateBorrower: async (id: number, borrower: Borrower): Promise<Borrower> => {
    try {
      const response = await axios.put(`${API_URL}/borrowers/${id}`, borrower);
      return response.data;
    } catch (error) {
      console.error(`Error updating borrower with id ${id}:`, error);
      throw error;
    }
  },

  // 削除
  deleteBorrower: async (id: number): Promise<void> => {
    try {
      await axios.delete(`${API_URL}/borrowers/${id}`);
    } catch (error) {
      console.error(`Error deleting borrower with id ${id}:`, error);
      throw error;
    }
  },

  // 検索
  searchBorrowers: async (params: { name?: string, creditRating?: string, industry?: string }): Promise<Borrower[]> => {
    try {
      const response = await axios.get(`${API_URL}/borrowers/search`, { params });
      return response.data;
    } catch (error) {
      console.error('Error searching borrowers:', error);
      throw error;
    }
  }
};