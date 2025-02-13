import { useState } from 'react';
import { LlamaChat } from '../../api/testAPI';
import Chat from '../../components/chatform/Chat';

const Llama = () => {
  const [messages, setMessages] = useState([]);
  const [isChatLoading, setIsChatLoading] = useState(false);

  const handleSendMessage = async (message) => {
    setMessages(prev => [...prev, { role: 'user', content: message }]);
    setIsChatLoading(true);

    try {
      const data = await LlamaChat(message);
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: data.response 
      }]);
      console.log(data.response);
    } catch (error) {
      console.error('LLaMA 채팅 에러:', error);
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
      title="LLaMA 2 7B Chat 사용 예정? 파인튜닝용으로 아래 모델 사용할수도 있음"
      subtitle="[현재 모델 : TinyLlama-1.1B-Chat-v1.0]"
      messages={messages}
      onSendMessage={handleSendMessage}
      isLoading={isChatLoading}
    />
  );
};

export default Llama;
    
