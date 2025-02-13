import { useState } from 'react';
import { DeepSeekChat } from '../../api/testAPI';
import Chat from '../../components/chatform/Chat';

const DeepSeek = () => {
  const [messages, setMessages] = useState([]);
  const [isChatLoading, setIsChatLoading] = useState(false);

  const handleSendMessage = async (message) => {
    setMessages(prev => [...prev, { role: 'user', content: message }]);
    setIsChatLoading(true);

    try {
      const data = await DeepSeekChat(message);
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: data.response 
      }]);
    } catch (error) {
      console.error('DeepSeek 채팅 에러:', error);
      setMessages(prev => [...prev, { 
        role: 'system', 
        content: '죄송합니다. 오류가 발생했습니다.' 
      }]);
    } finally {
      setIsChatLoading(false);
    }
  };

  return (
    <Chat
      title="TheBloke/deepseek-coder-6.7B-instruct-GGUF"
      subtitle="DeepSeek-R1 로컬 사양 부족해서 사용 X"
      messages={messages}
      onSendMessage={handleSendMessage}
      isLoading={isChatLoading}
    />
  );
};

export default DeepSeek;
    
