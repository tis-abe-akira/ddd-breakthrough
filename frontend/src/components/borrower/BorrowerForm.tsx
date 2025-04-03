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
    <div className="card">
      <h1 className="card-title mb-6">借入人登録</h1>
      
      {error && (
        <div className="form-error">
          {error}
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="name" className="form-label">
              名前 <span style={{ color: '#ef4444' }}>*</span>
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={borrower.name}
              onChange={handleChange}
              required
              className="form-control"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="creditRating" className="form-label">
              信用格付 <span style={{ color: '#ef4444' }}>*</span>
            </label>
            <select
              id="creditRating"
              name="creditRating"
              value={borrower.creditRating}
              onChange={handleChange}
              required
              className="form-control"
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
          
          <div className="form-group">
            <label htmlFor="industry" className="form-label">
              業種
            </label>
            <input
              type="text"
              id="industry"
              name="industry"
              value={borrower.industry || ''}
              onChange={handleChange}
              className="form-control"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="companyType" className="form-label">
              企業形態
            </label>
            <input
              type="text"
              id="companyType"
              name="companyType"
              value={borrower.companyType || ''}
              onChange={handleChange}
              className="form-control"
            />
          </div>
          
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label htmlFor="contactInformation" className="form-label">
              連絡先情報
            </label>
            <textarea
              id="contactInformation"
              name="contactInformation"
              value={borrower.contactInformation || ''}
              onChange={handleChange}
              rows={3}
              className="form-control"
            />
          </div>
          
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label htmlFor="financialStatements" className="form-label">
              財務情報
            </label>
            <textarea
              id="financialStatements"
              name="financialStatements"
              value={borrower.financialStatements || ''}
              onChange={handleChange}
              rows={3}
              className="form-control"
            />
          </div>
        </div>
        
        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/borrowers')}
            className="btn"
            style={{ backgroundColor: '#e5e7eb', color: '#374151', marginRight: '0.5rem' }}
          >
            キャンセル
          </button>
          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ opacity: loading ? 0.7 : 1 }}
          >
            {loading ? '登録中...' : '登録する'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default BorrowerForm;
