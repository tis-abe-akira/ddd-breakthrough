/**
 * @fileoverview 借入人詳細情報を表示するページコンポーネント
 * @module pages/BorrowerDetailPage
 */

import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getBorrowerById, deleteBorrower } from '../services/borrowerService';
import { Borrower } from '../types/borrower';
// ...existing code...

/**
 * 借入人詳細ページコンポーネント
 * URLパラメータから借入人IDを取得し、詳細情報を表示する
 *
 * @component
 * @returns {JSX.Element} 借入人詳細ページ
 */
const BorrowerDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [borrower, setBorrower] = useState<Borrower | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  /**
   * コンポーネントマウント時およびIDが変更された時に借入人データを取得する
   */
  useEffect(() => {
    if (!id) {
      setError('借入人IDが指定されていません');
      setLoading(false);
      return;
    }

    const fetchBorrower = async () => {
      try {
        const data = await getBorrowerById(parseInt(id, 10));
        setBorrower(data);
      } catch (error) {
        setError('借入人データの取得に失敗しました');
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    fetchBorrower();
  }, [id]);

  /**
   * 借入人データの削除を処理する
   *
   * @async
   * @function handleDelete
   */
  const handleDelete = async () => {
    if (!borrower) return;

    if (window.confirm('この借入人を削除してもよろしいですか？')) {
      try {
        await deleteBorrower(borrower.id);
        navigate('/borrowers');
      } catch (error) {
        console.error('借入人の削除に失敗しました', error);
        alert('削除処理に失敗しました。再度お試しください。');
      }
    }
  };

  // ...existing code...
};

export default BorrowerDetailPage;
