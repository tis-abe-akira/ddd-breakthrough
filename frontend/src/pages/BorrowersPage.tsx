import React from 'react';
import BorrowerList from '../components/borrower/BorrowerList';
import ErrorBoundary from '../components/error/ErrorBoundary';

const BorrowersPage: React.FC = () => {
  return (
    <ErrorBoundary>
      <BorrowerList />
    </ErrorBoundary>
  );
};

export default BorrowersPage;
