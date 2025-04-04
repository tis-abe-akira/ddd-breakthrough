/**
 * @fileoverview 借入人詳細情報を表示するページコンポーネント
 * @module pages/BorrowerDetailPage
 */

import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getBorrowerById } from '../services/borrowerService';
import { Borrower } from '../types/borrower';

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
        // 削除APIは未実装のため、一旦コメントアウト
        // await deleteBorrower(borrower.id);
        alert('削除機能は現在実装されていません');
        // 削除成功時は一覧ページに戻る
        // navigate('/borrowers');
      } catch (error) {
        console.error('借入人の削除に失敗しました', error);
        alert('削除処理に失敗しました。再度お試しください。');
      }
    }
  };

  // ローディング中の表示
  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg font-medium text-gray-500">読み込み中...</div>
      </div>
    );
  }

  // エラー時の表示
  if (error) {
    return (
      <div className="bg-red-50 p-4 rounded-md">
        <div className="text-red-700">{error}</div>
        <Link to="/borrowers" className="text-blue-600 hover:underline mt-2 inline-block">
          借入人一覧に戻る
        </Link>
      </div>
    );
  }

  // データが取得できなかった場合
  if (!borrower) {
    return (
      <div className="bg-yellow-50 p-4 rounded-md">
        <div className="text-yellow-700">借入人が見つかりませんでした</div>
        <Link to="/borrowers" className="text-blue-600 hover:underline mt-2 inline-block">
          借入人一覧に戻る
        </Link>
      </div>
    );
  }

  // 借入人詳細の表示
  return (
    <div className="p-4 bg-white rounded-lg shadow">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">借入人詳細</h1>
        <div className="space-x-2">
          <Link
            to={`/borrowers/${borrower.id}/edit`}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            編集
          </Link>
          <button
            onClick={handleDelete}
            className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            削除
          </button>
          <Link
            to="/borrowers"
            className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300"
          >
            一覧に戻る
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-4">
          <div>
            <h2 className="text-sm font-medium text-gray-500">借入人ID</h2>
            <p className="text-lg">{borrower.id}</p>
          </div>
          <div>
            <h2 className="text-sm font-medium text-gray-500">借入人名</h2>
            <p className="text-lg">{borrower.name}</p>
          </div>
          <div>
            <h2 className="text-sm font-medium text-gray-500">借入人コード</h2>
            <p className="text-lg">{borrower.code}</p>
          </div>
        </div>
        
        <div className="space-y-4">
          <div>
            <h2 className="text-sm font-medium text-gray-500">業種</h2>
            <p className="text-lg">{borrower.industry || '未設定'}</p>
          </div>
          <div>
            <h2 className="text-sm font-medium text-gray-500">信用格付け</h2>
            <p className="text-lg">{borrower.creditRating || '未設定'}</p>
          </div>
        </div>
      </div>

      <div className="mt-8 border-t pt-6">
        <h2 className="text-xl font-semibold mb-4">タイムスタンプ情報</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <h3 className="text-sm font-medium text-gray-500">作成日時</h3>
            <p>{new Date(borrower.createdAt).toLocaleString()}</p>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500">更新日時</h3>
            <p>{new Date(borrower.updatedAt).toLocaleString()}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BorrowerDetailPage;
