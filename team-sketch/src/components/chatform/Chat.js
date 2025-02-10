import { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import './Chat.css';

const Chat = ({ 
  title, 
  subtitle, 
  messages, 
  onSendMessage, 
  isLoading 
}) => {
  const [newMessage, setNewMessage] = useState('');
  const chatContainerRef = useRef(null);

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
    
    await onSendMessage(newMessage);
    setNewMessage('');
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        {title}
        {subtitle && <div>{subtitle}</div>}
      </div>

      <div className="messages-container" ref={chatContainerRef}>
        {messages.map((message, index) => (
          <div key={index} className={`message-bubble ${message.role}`}>
            {message.content}
          </div>
        ))}
        {isLoading && <div className="loading-dots">...</div>}
      </div>

      <form className="chat-form" onSubmit={handleSubmit}>
        <input
          type="text"
          className="chat-input"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          placeholder="메시지를 입력하세요..."
          disabled={isLoading}
        />
        <button 
          type="submit" 
          className="send-button"
          disabled={isLoading || !newMessage.trim()}
        >
          전송
        </button>
      </form>
    </div>
  );
};

Chat.propTypes = {
  title: PropTypes.string.isRequired,
  subtitle: PropTypes.string,
  messages: PropTypes.arrayOf(
    PropTypes.shape({
      role: PropTypes.string.isRequired,
      content: PropTypes.string.isRequired
    })
  ).isRequired,
  onSendMessage: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired
};

export default Chat;