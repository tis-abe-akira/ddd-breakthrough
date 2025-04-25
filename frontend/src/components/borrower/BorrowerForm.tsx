import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Borrower, NewBorrowerInput } from '../../types/borrower';
import { createBorrower, updateBorrower } from '../../services/borrowerService';

interface BorrowerFormProps {
  borrowerId?: number;
  initialData?: Borrower;
  isEditMode?: boolean;
}

const BorrowerForm: React.FC<BorrowerFormProps> = ({
  borrowerId,
  initialData,
  isEditMode = false,
}) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<NewBorrowerInput>({
    name: '',
    creditRating: '',
    industry: '',
    contactInfo: '',
    companyType: '',
    version: undefined, // versionフィールドを初期化
  });

  // 編集モードの場合、初期データをフォームにセット
  useEffect(() => {
    if (isEditMode && initialData) {
      setFormData({
        name: initialData.name || '',
        creditRating: initialData.creditRating || '',
        industry: initialData.industry || '',
        contactInfo: initialData.contactInfo || '',
        companyType: initialData.companyType || '',
        version: initialData.version, // 初期データからversionを設定
      });
      console.log('初期データのversion:', initialData.version); // デバッグ用ログ
    }
  }, [isEditMode, initialData]);

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      if (isEditMode && borrowerId) {
        // 編集モード - versionも含めて送信
        console.log('更新データ:', formData); // デバッグ用ログ
        await updateBorrower(borrowerId, formData);
        navigate(`/borrowers/${borrowerId}`);
      } else {
        // 新規作成モード
        const newBorrower = await createBorrower(formData);
        navigate(`/borrowers/${newBorrower.id}`);
      }
    } catch (err) {
      console.error('Error:', err);
      setError(
        isEditMode ? '借入人の更新に失敗しました' : '借入人の登録に失敗しました'
      );
    } finally {
      setLoading(false);
    }
  };

  const pageTitle = isEditMode ? '借入人編集' : '借入人登録';
  const submitButtonText = isEditMode ? '更新する' : '登録する';

  return (
    <div className='max-w-2xl mx-auto p-6 bg-white rounded-lg shadow-md'>
      <h1 className='text-2xl font-bold mb-6'>{pageTitle}</h1>

      {error && (
        <div className='mb-4 p-3 bg-red-100 text-red-700 rounded-md'>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* version情報を隠しフィールドとして追加 */}
        {isEditMode && initialData?.version !== undefined && (
          <input type='hidden' name='version' value={formData.version || ''} />
        )}

        <div className='mb-4'>
          <label
            htmlFor='name'
            className='block text-sm font-medium text-gray-700 mb-1'
          >
            借入人名 <span className='text-red-500'>*</span>
          </label>
          <input
            type='text'
            id='name'
            name='name'
            value={formData.name}
            onChange={handleChange}
            required
            className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200'
          />
        </div>

        <div className='mb-4'>
          <label
            htmlFor='companyType'
            className='block text-sm font-medium text-gray-700 mb-1'
          >
            企業形態 <span className='text-red-500'>*</span>
          </label>
          <select
            id='companyType'
            name='companyType'
            value={formData.companyType}
            onChange={handleChange}
            required
            className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200'
          >
            <option value=''>選択してください</option>
            <option value='株式会社'>株式会社</option>
            <option value='合同会社'>合同会社</option>
            <option value='合名会社'>合名会社</option>
            <option value='合資会社'>合資会社</option>
            <option value='有限会社'>有限会社</option>
            <option value='個人事業主'>個人事業主</option>
            <option value='その他'>その他</option>
          </select>
        </div>

        <div className='mb-4'>
          <label
            htmlFor='creditRating'
            className='block text-sm font-medium text-gray-700 mb-1'
          >
            信用格付
          </label>
          <select
            id='creditRating'
            name='creditRating'
            value={formData.creditRating}
            onChange={handleChange}
            className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200'
          >
            <option value=''>選択してください</option>
            <option value='AAA'>AAA</option>
            <option value='AA'>AA</option>
            <option value='A'>A</option>
            <option value='BBB'>BBB</option>
            <option value='BB'>BB</option>
            <option value='B'>B</option>
            <option value='CCC'>CCC</option>
            <option value='CC'>CC</option>
            <option value='C'>C</option>
            <option value='D'>D</option>
          </select>
        </div>

        <div className='mb-4'>
          <label
            htmlFor='industry'
            className='block text-sm font-medium text-gray-700 mb-1'
          >
            業種 <span className='text-red-500'>*</span>
          </label>
          <input
            type='text'
            id='industry'
            name='industry'
            value={formData.industry}
            onChange={handleChange}
            required
            className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200'
          />
        </div>

        <div className='mb-6'>
          <label
            htmlFor='contactInfo'
            className='block text-sm font-medium text-gray-700 mb-1'
          >
            連絡先情報
          </label>
          <textarea
            id='contactInfo'
            name='contactInfo'
            value={formData.contactInfo}
            onChange={handleChange}
            rows={3}
            className='w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:ring-blue-200'
          />
        </div>

        <div className='flex justify-end space-x-3'>
          <button
            type='button'
            onClick={() =>
              navigate(
                isEditMode && borrowerId
                  ? `/borrowers/${borrowerId}`
                  : '/borrowers'
              )
            }
            className='px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring focus:ring-gray-200'
          >
            キャンセル
          </button>
          <button
            type='submit'
            disabled={loading}
            className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring focus:ring-blue-200 disabled:opacity-70'
          >
            {loading
              ? isEditMode
                ? '更新中...'
                : '登録中...'
              : submitButtonText}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BorrowerForm;
