import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Borrower } from '../types/borrower';
import { getBorrowers } from '../services/borrowerService';

const BorrowersPage: React.FC = () => {
  const [borrowers, setBorrowers] = useState<Borrower[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBorrowers = async () => {
      try {
        const data = await getBorrowers();
        setBorrowers(data);
      } catch (error) {
        console.error('Error fetching borrowers:', error);
        setError('借入人データの取得に失敗しました');
      } finally {
        setLoading(false);
      }
    };

    fetchBorrowers();
  }, []);

  if (loading) {
    return <div className='text-center py-4'>読み込み中...</div>;
  }

  if (error) {
    return <div className='text-red-500 text-center py-4'>{error}</div>;
  }

  return (
    <div className='container mx-auto px-4 py-8'>
      <div className='flex justify-between items-center mb-6'>
        <h1 className='text-2xl font-bold'>借入人一覧</h1>
        <Link
          to='/borrowers/new'
          className='bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded'
        >
          新規登録
        </Link>
      </div>

      {borrowers.length === 0 ? (
        <div className='text-center py-4'>
          借入人データがありません。新規登録ボタンから登録してください。
        </div>
      ) : (
        <div className='overflow-x-auto'>
          <table className='min-w-full bg-white rounded-lg overflow-hidden'>
            <thead className='bg-gray-100'>
              <tr>
                <th className='px-4 py-2 text-left'>ID</th>
                <th className='px-4 py-2 text-left'>名前</th>
                <th className='px-4 py-2 text-left'>企業形態</th>
                <th className='px-4 py-2 text-left'>業種</th>
                <th className='px-4 py-2 text-left'>信用格付</th>
                <th className='px-4 py-2 text-left'>アクション</th>
              </tr>
            </thead>
            <tbody className='divide-y divide-gray-200'>
              {borrowers.map((borrower) => (
                <tr key={borrower.id} className='hover:bg-gray-50'>
                  <td className='px-4 py-2'>{borrower.id}</td>
                  <td className='px-4 py-2'>{borrower.name}</td>
                  <td className='px-4 py-2'>{borrower.companyType}</td>
                  <td className='px-4 py-2'>{borrower.industry}</td>
                  <td className='px-4 py-2'>{borrower.creditRating || '-'}</td>
                  <td className='px-4 py-2'>
                    <Link
                      to={`/borrowers/${borrower.id}`}
                      className='text-blue-500 hover:text-blue-700 mr-2'
                    >
                      詳細
                    </Link>
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

export default BorrowersPage;
