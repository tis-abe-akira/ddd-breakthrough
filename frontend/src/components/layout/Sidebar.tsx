import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { HomeIcon, UserGroupIcon, ArrowTrendingUpIcon, BuildingLibraryIcon, BanknotesIcon } from '@heroicons/react/24/outline';

const Sidebar: React.FC = () => {
  const location = useLocation();
  
  const isActive = (path: string) => {
    return location.pathname === path ? 'active' : '';
  };

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h1 className="sidebar-title">シンジケートローン</h1>
      </div>
      
      <nav className="sidebar-nav">
        <ul>
          <li>
            <Link to="/" className={isActive('/')}>
              <HomeIcon className="w-5 h-5 mr-3" />
              <span>ホーム</span>
            </Link>
          </li>
          
          <li>
            <Link to="/borrowers" className={isActive('/borrowers')}>
              <UserGroupIcon className="w-5 h-5 mr-3" />
              <span>借入人</span>
            </Link>
          </li>
          
          <li>
            <Link to="/investors" className={isActive('/investors')}>
              <BuildingLibraryIcon className="w-5 h-5 mr-3" />
              <span>投資家</span>
            </Link>
          </li>
          
          <li>
            <Link to="/loans" className={isActive('/loans')}>
              <BanknotesIcon className="w-5 h-5 mr-3" />
              <span>ローン</span>
            </Link>
          </li>
          
          <li>
            <Link to="/facilities" className={isActive('/facilities')}>
              <ArrowTrendingUpIcon className="w-5 h-5 mr-3" />
              <span>ファシリティ</span>
            </Link>
          </li>
        </ul>
      </nav>
      
      <div className="sidebar-footer">
        <p>© 2025 Syndicated Loan</p>
      </div>
    </div>
  );
};

export default Sidebar;
