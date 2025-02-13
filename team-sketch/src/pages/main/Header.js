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
      label: '챗봇',
      onClick: () => navigate('/chatbot')
    },
    {
      label: '규칙 추가',
      onClick: () => navigate('/rule')
    },

    {
      label: '크롤링',
      onClick: () => navigate('/crawling')
    },
    {
      label: 'LlamaAI',
      onClick: () => navigate('/llama')
    },
    {
      label: 'DeepSeekAI',
      onClick: () => navigate('/deepseek')
    },
    {
      label: '음성 파일 업로드',
      onClick: () => navigate('/audio-upload')
    },
    {
      label: '사진 업로드',
      onClick: () => navigate('/picture-upload')
    },
    {
      label: '클라우드 챗봇',
      onClick: () => navigate('/cloudchatbot')
    }
  ];


  return (
    <header>
      <NavigationBar items={navigationItems} />
    </header>
  );
};

export default Header;
