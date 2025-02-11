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
    <div className="card shadow-sm">
      {/* 채팅 헤더 */}
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">{title}</h5>
        {subtitle && <small className="text-white-50">{subtitle}</small>}
      </div>

      {/* 메시지 영역 */}
      <div 
        className="card-body bg-light" 
        style={{ height: '400px', overflowY: 'auto' }}
        ref={chatContainerRef}
      >
        {messages.map((message, index) => (
          <div 
            key={index} 
            className={`d-flex ${message.role === 'user' ? 'justify-content-end' : 'justify-content-start'} mb-3`}
          >
            <div 
              className={`rounded p-3 ${
                message.role === 'user' 
                  ? 'bg-primary text-white' 
                  : 'bg-white border'
              }`}
              style={{ maxWidth: '75%', wordBreak: 'break-word' }}
            >
              {message.content}
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="d-flex justify-content-start mb-3">
            <div className="bg-white border rounded p-3">
              <div className="spinner-grow spinner-grow-sm text-primary me-2" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="spinner-grow spinner-grow-sm text-primary me-2" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="spinner-grow spinner-grow-sm text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 입력 폼 */}
      <div className="card-footer bg-white">
        <form onSubmit={handleSubmit} className="d-flex gap-2">
          <input
            type="text"
            className="form-control"
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            placeholder="메시지를 입력하세요..."
            disabled={isLoading}
          />
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={isLoading || !newMessage.trim()}
          >
            전송
          </button>
        </form>
      </div>
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