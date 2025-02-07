import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './Main.css';
import Header from './Header';
import Home from '../home/Home';
import Test from '../test/Test';
import Test2 from '../test/Test2';

const Main = () => {
  return (
    <div>
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/test" element={<Test />} />
          <Route path="/test2" element={<Test2 />} />
        </Routes>
      </main>
    </div>
  );
};

export default Main;
