import React from 'react';
import './TagForm.css';

const Button = ({ type = 'button', onClick, children, className = '' }) => {
  return (
    <button type={type} onClick={onClick} className={`tag-button ${className}`}>
      {children}
    </button>
  );
};

export default Button;
