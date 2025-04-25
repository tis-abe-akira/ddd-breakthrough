import React from 'react';
import BorrowerForm from '../components/borrower/BorrowerForm';

const NewBorrowerPage: React.FC = () => {
  return (
    <div>
      <BorrowerForm isEditMode={false} />
    </div>
  );
};

export default NewBorrowerPage;
