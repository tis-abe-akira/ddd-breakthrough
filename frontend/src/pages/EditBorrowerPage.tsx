import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BorrowerForm from '../components/borrower/BorrowerForm';
import { getBorrowerById } from '../services/borrowerService';
import { Borrower } from '../types/borrower';

const EditBorrowerPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [borrower, setBorrower] = useState<Borrower | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBorrower = async () => {
      if (!id) {
        setError('借入人IDが指定されていません');
        setLoading(false);
        return;
      }

      try {
        const borrowerId = parseInt(id, 10);
        if (isNaN(borrowerId)) {
          setError('無効な借入人IDです');
          setLoading(false);
          return;
        }

        const data = await getBorrowerById(borrowerId);
        setBorrower(data);
      } catch (error) {
        console.error('Error fetching borrower:', error);
        setError('借入人データの取得に失敗しました');
      } finally {
        setLoading(false);
      }
    };

    fetchBorrower();
  }, [id]);

  if (loading) {
    return (
      <div className='flex justify-center items-center h-40'>
        <div className='text-lg font-semibold text-gray-600'>読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className='max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md'>
        <div className='p-4 bg-red-100 text-red-700 rounded-md mb-4'>
          <p>{error}</p>
        </div>
        <button
          onClick={() => navigate('/borrowers')}
          className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
        >
          借入人一覧に戻る
        </button>
      </div>
    );
  }

  if (!borrower) {
    return (
      <div className='max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md'>
        <div className='p-4 bg-yellow-100 text-yellow-700 rounded-md mb-4'>
          <p>指定された借入人が見つかりませんでした。</p>
        </div>
        <button
          onClick={() => navigate('/borrowers')}
          className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700'
        >
          借入人一覧に戻る
        </button>
      </div>
    );
  }

  return (
    <div>
      <BorrowerForm
        borrowerId={borrower.id}
        initialData={borrower}
        isEditMode={true}
      />
    </div>
  );
};

export default EditBorrowerPage;
