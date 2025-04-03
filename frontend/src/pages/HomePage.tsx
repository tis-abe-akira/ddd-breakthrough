import React from 'react';
import { Link } from 'react-router-dom';

const HomePage: React.FC = () => {
  return (
    <div className="card">
      <h1 className="card-title mb-6">シンジケートローン管理システム</h1>
      
      <div className="home-grid">
        <div className="feature-card feature-card-blue">
          <h2>借入人管理</h2>
          <p>借入人の登録・編集・検索ができます。</p>
          <Link to="/borrowers" className="link">
            借入人一覧へ →
          </Link>
        </div>
        
        <div className="feature-card feature-card-green">
          <h2>投資家管理</h2>
          <p>投資家の登録・編集・検索ができます。</p>
          <Link to="/investors" className="link">
            投資家一覧へ →
          </Link>
        </div>
        
        <div className="feature-card feature-card-purple">
          <h2>ローン管理</h2>
          <p>ローンの登録・編集・検索ができます。</p>
          <Link to="/loans" className="link">
            ローン一覧へ →
          </Link>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
