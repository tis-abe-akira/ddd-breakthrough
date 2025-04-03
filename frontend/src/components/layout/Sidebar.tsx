import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { HomeIcon, UserGroupIcon, ArrowTrendingUpIcon, BuildingLibraryIcon, BanknotesIcon } from '@heroicons/react/24/outline';

const Sidebar: React.FC = () => {
  const location = useLocation();
  
  const isActive = (path: string) => {
    return location.pathname === path ? 'bg-blue-100 text-blue-600' : 'text-gray-600 hover:bg-gray-100';
  };

  return (
    <div className="w-64 h-screen bg-white border-r border-gray-200 flex flex-col">
      <div className="p-4 border-b border-gray-200">
        <h1 className="text-xl font-bold text-blue-600">シンジケートローン</h1>
      </div>
      
      <nav className="flex-1 overflow-y-auto p-4">
        <ul className="space-y-2">
          <li>
            <Link to="/" className={`flex items-center p-2 rounded-md ${isActive('/')}`}>
              <HomeIcon className="w-5 h-5 mr-3" />
              <span>ホーム</span>
            </Link>
          </li>
          
          <li>
            <Link to="/borrowers" className={`flex items-center p-2 rounded-md ${isActive('/borrowers')}`}>
              <UserGroupIcon className="w-5 h-5 mr-3" />
              <span>借入人</span>
            </Link>
          </li>
          
          <li>
            <Link to="/investors" className={`flex items-center p-2 rounded-md ${isActive('/investors')}`}>
              <BuildingLibraryIcon className="w-5 h-5 mr-3" />
              <span>投資家</span>
            </Link>
          </li>
          
          <li>
            <Link to="/loans" className={`flex items-center p-2 rounded-md ${isActive('/loans')}`}>
              <BanknotesIcon className="w-5 h-5 mr-3" />
              <span>ローン</span>
            </Link>
          </li>
          
          <li>
            <Link to="/facilities" className={`flex items-center p-2 rounded-md ${isActive('/facilities')}`}>
              <ArrowTrendingUpIcon className="w-5 h-5 mr-3" />
              <span>ファシリティ</span>
            </Link>
          </li>
        </ul>
      </nav>
      
      <div className="p-4 border-t border-gray-200">
        <p className="text-xs text-gray-500">© 2025 Syndicated Loan</p>
      </div>
    </div>
  );
};

export default Sidebar;