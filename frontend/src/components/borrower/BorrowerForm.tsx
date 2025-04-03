import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Borrower } from '../../types/borrower';
import { BorrowerService } from '../../services/borrowerService';

const BorrowerForm: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [borrower, setBorrower] = useState<Borrower>({
    name: '',
    creditRating: '',
    industry: '',
    companyType: '',
    contactInformation: '',
    financialStatements: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setBorrower(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      await BorrowerService.createBorrower(borrower);
      setLoading(false);
      navigate('/borrowers');
    } catch (err) {
      setError('借入人の登録に失敗しました');
      setLoading(false);
      console.error(err);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">借入人登録</h1>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="mb-4">
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">名前 <span className="text-red-500">*</span></label>
            <input
              type="text"
              id="name"
              name="name"
              value={borrower.name}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="mb-4">
            <label htmlFor="creditRating" className="block text-sm font-medium text-gray-700 mb-1">信用格付 <span className="text-red-500">*</span></label>
            <select
              id="creditRating"
              name="creditRating"
              value={borrower.creditRating}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">選択してください</option>
              <option value="AAA">AAA</option>
              <option value="AA">AA</option>
              <option value="A">A</option>
              <option value="BBB">BBB</option>
              <option value="BB">BB</option>
              <option value="B">B</option>
              <option value="CCC">CCC</option>
              <option value="CC">CC</option>
              <option value="C">C</option>
              <option value="D">D</option>
            </select>
          </div>
          
          <div className="mb-4">
            <label htmlFor="industry" className="block text-sm font-medium text-gray-700 mb-1">業種</label>
            <input
              type="text"
              id="industry"
              name="industry"
              value={borrower.industry || ''}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="mb-4">
            <label htmlFor="companyType" className="block text-sm font-medium text-gray-700 mb-1">企業形態</label>
            <input
              type="text"
              id="companyType"
              name="companyType"
              value={borrower.companyType || ''}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="mb-4 md:col-span-2">
            <label htmlFor="contactInformation" className="block text-sm font-medium text-gray-700 mb-1">連絡先情報</label>
            <textarea
              id="contactInformation"
              name="contactInformation"
              value={borrower.contactInformation || ''}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div className="mb-4 md:col-span-2">
            <label htmlFor="financialStatements" className="block text-sm font-medium text-gray-700 mb-1">財務情報</label>
            <textarea
              id="financialStatements"
              name="financialStatements"
              value={borrower.financialStatements || ''}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>
        
        <div className="flex justify-end mt-6">
          <button
            type="button"
            onClick={() => navigate('/borrowers')}
            className="bg-gray-200 text-gray-700 px-4 py-2 rounded-md mr-2 hover:bg-gray-300"
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={loading}
            className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 disabled:bg-blue-300"
          >
            {loading ? '登録中...' : '登録する'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BorrowerForm;