import { useState } from 'react';
import { CloudChatBotChat } from '../../api/testAPI';
import Chat from '../../components/chatform/Chat';

const CloudChatBot = () => {
    const [messages, setMessages] = useState([]);
    const [isChatLoading, setIsChatLoading] = useState(false);
  
    const handleSendMessage = async (message) => {
      setMessages(prev => [...prev, { role: 'user', content: message }]);
      setIsChatLoading(true);
  
      try {
        const data = await CloudChatBotChat(message);
        setMessages(prev => [...prev, { 
          role: 'assistant', 
          content: data.response 
        }]);
        console.log(data.response);
      } catch (error) {
        console.error('Cloud ChatBot 채팅 에러:', error);
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
        title="Cloud ChatBot"
        subtitle="클라우드 챗봇"
        messages={messages}
        onSendMessage={handleSendMessage}
        isLoading={isChatLoading}
      />
    );
  };

export default CloudChatBot;
