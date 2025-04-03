import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Borrower } from '../../types/borrower';
import { BorrowerService } from '../../services/borrowerService';

const BorrowerList: React.FC = () => {
  const [borrowers, setBorrowers] = useState<Borrower[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

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

  const handleDelete = async (id?: number) => {
    if (!id) return;
    
    if (window.confirm('本当に削除しますか？')) {
      try {
        await BorrowerService.deleteBorrower(id);
        setBorrowers(borrowers.filter(borrower => borrower.id !== id));
      } catch (err) {
        setError('削除に失敗しました。');
        console.error(err);
      }
    }
  };

  if (loading) {
    return <div className="text-center p-4">読み込み中...</div>;
  }

  if (error) {
    return <div className="text-red-500 p-4">{error}</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">借入人一覧</h1>
        <Link 
          to="/borrowers/new" 
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
        >
          新規登録
        </Link>
      </div>

      {borrowers.length === 0 ? (
        <p className="text-gray-500 text-center py-4">借入人データがありません。</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">名前</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">信用格付</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">業種</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">企業形態</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">アクション</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {borrowers.map((borrower) => (
                <tr key={borrower.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{borrower.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{borrower.name}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{borrower.creditRating}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{borrower.industry || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{borrower.companyType || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <Link to={`/borrowers/${borrower.id}`} className="text-blue-600 hover:text-blue-900 mr-3">
                      詳細
                    </Link>
                    <Link to={`/borrowers/${borrower.id}/edit`} className="text-green-600 hover:text-green-900 mr-3">
                      編集
                    </Link>
                    <button 
                      onClick={() => handleDelete(borrower.id)} 
                      className="text-red-600 hover:text-red-900"
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