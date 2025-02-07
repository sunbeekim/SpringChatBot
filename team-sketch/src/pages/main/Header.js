import React from 'react';
import { useNavigate } from 'react-router-dom';
import NavigationBar from '../../components/naviform/NavigationBar';

const Header = () => {
  const navigate = useNavigate();

  // 네비게이션 아이템 정의
  const navigationItems = [
    {
      label: '홈',
      onClick: () => navigate('/')
    },
    {
      label: '테스트',
      onClick: () => navigate('/test')
    },
    {
      label: '테스트2',
      onClick: () => navigate('/test2')
    },
    {
      label: '테스트3',
      onClick: () => navigate('/test3')
    }
  ];


  return (
    <header>
      <NavigationBar items={navigationItems} />
    </header>
  );
};

export default Header;
