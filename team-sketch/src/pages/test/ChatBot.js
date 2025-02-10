import { useState } from 'react';
import { ChatBotChat } from '../../api/testAPI';
import Chat from '../../components/chatform/Chat';

const ChatBot = () => {
  const [messages, setMessages] = useState([]);
  const [isChatLoading, setIsChatLoading] = useState(false);

  const handleSendMessage = async (message) => {
    setMessages(prev => [...prev, { role: 'user', content: message }]);
    setIsChatLoading(true);

    try {
      const data = await ChatBotChat(message);
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: data.response 
      }]);
    } catch (error) {
      console.error('ChatBot 에러:', error);
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
      title="ChatBot"
      subtitle="규칙 기반 챗봇"
      messages={messages}
      onSendMessage={handleSendMessage}
      isLoading={isChatLoading}
    />
  );
};

export default ChatBot;