import { useState, useRef, useEffect } from 'react';
import { DeepSeekChat } from '../../api/testAPI';
import './Test2.css';


const Test3 = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isChatLoading, setIsChatLoading] = useState(false);
  const chatContainerRef = useRef(null);

  // 채팅창 자동 스크롤
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [messages]);

  // 메시지 전송 핸들러
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    // 사용자 메시지 추가
    setMessages(prev => [...prev, { role: 'user', content: newMessage }]);
    setIsChatLoading(true);

    try {
      const data = await DeepSeekChat(newMessage);
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
      setNewMessage('');
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">TheBloke/deepseek-coder-6.7B-instruct-GGUF
        <div>DeepSeek-R1 로컬 사양 부족해서 사용 X</div>
      </div>
      <div className="messages-container" ref={chatContainerRef}>
        {messages.map((message, index) => (
          <div key={index} className={`message-bubble ${message.role}`}>
            {message.content}
          </div>
        ))}
        {isChatLoading && <div className="loading-dots">...</div>}
      </div>
      <form className="chat-form" onSubmit={handleSendMessage}>
        <input
          type="text"
          className="chat-input"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="메시지를 입력하세요..."
          disabled={isChatLoading}
        />
        <button 
          type="submit" 
          className="send-button"
          disabled={isChatLoading || !newMessage.trim()}
        >
          전송
        </button>
      </form>
    </div>
  );
};

export default Test3;
    
