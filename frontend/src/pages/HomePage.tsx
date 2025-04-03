import React from 'react';
import { Link } from 'react-router-dom';

const HomePage: React.FC = () => {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h1 className="text-3xl font-bold text-blue-600 mb-6">シンジケートローン管理システム</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-blue-50 p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-blue-700 mb-3">借入人管理</h2>
          <p className="text-gray-600 mb-4">借入人の登録・編集・検索ができます。</p>
          <Link to="/borrowers" className="text-blue-500 hover:text-blue-700 font-medium">
            借入人一覧へ →
          </Link>
        </div>
        
        <div className="bg-green-50 p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-green-700 mb-3">投資家管理</h2>
          <p className="text-gray-600 mb-4">投資家の登録・編集・検索ができます。</p>
          <Link to="/investors" className="text-green-500 hover:text-green-700 font-medium">
            投資家一覧へ →
          </Link>
        </div>
        
        <div className="bg-purple-50 p-6 rounded-lg shadow-md">
          <h2 className="text-xl font-semibold text-purple-700 mb-3">ローン管理</h2>
          <p className="text-gray-600 mb-4">ローンの登録・編集・検索ができます。</p>
          <Link to="/loans" className="text-purple-500 hover:text-purple-700 font-medium">
            ローン一覧へ →
          </Link>
        </div>
      </div>
    </div>
  );
};

export default HomePage;