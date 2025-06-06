import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import HomePage from './pages/HomePage';
import BorrowersPage from './pages/BorrowersPage';
import NewBorrowerPage from './pages/NewBorrowerPage';
import BorrowerDetailPage from './pages/BorrowerDetailPage';
import EditBorrowerPage from './pages/EditBorrowerPage';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path='/' element={<HomePage />} />
          <Route path='/borrowers' element={<BorrowersPage />} />
          <Route path='/borrowers/new' element={<NewBorrowerPage />} />
          <Route path='/borrowers/:id' element={<BorrowerDetailPage />} />
          <Route path='/borrowers/:id/edit' element={<EditBorrowerPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
