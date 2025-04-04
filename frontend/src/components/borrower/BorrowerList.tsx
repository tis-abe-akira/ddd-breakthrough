/**
 * @fileoverview 借入人一覧を表示するコンポーネント
 * @module components/borrower/BorrowerList
 */

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Borrower } from '../../types/borrower';
import { BorrowerService } from '../../services/borrowerService';

/**
 * 借入人一覧コンポーネント
 *
 * @component
 * @returns {JSX.Element} 借入人一覧を表示するコンポーネント
 */
const BorrowerList: React.FC = () => {
  const [borrowers, setBorrowers] = useState<Borrower[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  /**
   * コンポーネントマウント時に借入人データを取得する
   */
  useEffect(() => {
    const fetchBorrowers = async () => {
      try {
        const data = await BorrowerService.getAllBorrowers();
        setBorrowers(data);
        setLoading(false);
      } catch (err) {
        setError('借入人データの取得に失敗しました。');
        setLoading(false);
        console.error(err);
      }
    };

    fetchBorrowers();
  }, []);

  /**
   * 借入人を削除する
   *
   * @param {number} id - 借入人ID
   */
  const handleDelete = async (id?: number) => {
    if (!id) return;

    if (window.confirm('本当に削除しますか？')) {
      try {
        await BorrowerService.deleteBorrower(id);
        setBorrowers(borrowers.filter((borrower) => borrower.id !== id));
      } catch (err) {
        setError('削除に失敗しました。');
        console.error(err);
      }
    }
  };

  if (loading) {
    return <div className='text-center py-4'>読み込み中...</div>;
  }

  if (error) {
    return <div className='text-red-500 py-4'>{error}</div>;
  }

  return (
    <div className='card'>
      <div className='card-header'>
        <h1 className='card-title'>借入人一覧</h1>
        <Link to='/borrowers/new' className='btn btn-primary'>
          新規登録
        </Link>
      </div>

      {borrowers.length === 0 ? (
        <p className='text-gray-500 text-center py-4'>
          借入人データがありません。
        </p>
      ) : (
        <div className='overflow-x-auto'>
          <table className='table'>
            <thead>
              <tr>
                <th>ID</th>
                <th>名前</th>
                <th>信用格付</th>
                <th>業種</th>
                <th>企業形態</th>
                <th>アクション</th>
              </tr>
            </thead>
            <tbody>
              {borrowers.map((borrower) => (
                <tr key={borrower.id}>
                  <td>{borrower.id}</td>
                  <td>{borrower.name}</td>
                  <td>{borrower.creditRating}</td>
                  <td>{borrower.industry || '-'}</td>
                  <td>{borrower.companyType || '-'}</td>
                  <td>
                    <Link
                      to={`/borrowers/${borrower.id}`}
                      className='link mr-3'
                    >
                      詳細
                    </Link>
                    <Link
                      to={`/borrowers/${borrower.id}/edit`}
                      className='link mr-3'
                    >
                      編集
                    </Link>
                    <button
                      onClick={() => handleDelete(borrower.id)}
                      className='link link-danger'
                    >
                      削除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default BorrowerList;
