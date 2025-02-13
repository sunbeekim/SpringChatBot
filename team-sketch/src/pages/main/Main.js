import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './Main.css';
import Header from './Header';
import Home from '../home/Home';
import Crawling from '../test/Crwaling';
import Llama from '../test/Llama';
import DeepSeek from '../test/DeepSeek';
import Rule from '../test/Rule';
import ChatBot from '../test/ChatBot';
import AudioUpload from '../test/AudioUpload';
import PictureUpload from '../test/PictureUpload';
import CloudChatBot from '../test/CloudChatBot';

const Main = () => {
  return (
    <div>
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/chatbot" element={<ChatBot />} />
          <Route path="/rule" element={<Rule />} />
          <Route path="/crawling" element={<Crawling />} />
          <Route path="/llama" element={<Llama />} />
          <Route path="/deepseek" element={<DeepSeek />} />
          <Route path="/audio-upload" element={<AudioUpload />} />
          <Route path="/picture-upload" element={<PictureUpload />} />
          <Route path="/cloudchatbot" element={<CloudChatBot />} />
        </Routes>
      </main>
    </div>
  );
};

export default Main;
