/**
 * @fileoverview アプリケーションのヘッダーコンポーネント
 * @module components/layout/Header
 */

import React from 'react';
import { Link } from 'react-router-dom';
// ...existing code...

/**
 * ヘッダーコンポーネントのプロップス
 *
 * @typedef {Object} HeaderProps
 * @property {string} [title] - ヘッダーに表示するタイトル（デフォルト: "シンジケートローン管理"）
 * @property {boolean} [showNav=true] - ナビゲーションメニューを表示するかどうか
 */

/**
 * アプリケーションのヘッダーコンポーネント
 *
 * @component
 * @param {HeaderProps} props - コンポーネントのプロップス
 * @returns {JSX.Element} ヘッダーコンポーネント
 */
const Header: React.FC<HeaderProps> = ({
  title = 'シンジケートローン管理',
  showNav = true,
}) => {
  // ...existing code...

  return (
    <header className='bg-blue-600 text-white p-4'>
      <div className='container mx-auto flex justify-between items-center'>
        <h1 className='text-2xl font-bold'>{title}</h1>
        {showNav && <nav>{/* ...existing code... */}</nav>}
      </div>
    </header>
  );
};

export default Header;
