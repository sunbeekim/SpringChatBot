import React from 'react';
import './TagForm.css';

const Input = ({ 
  type = 'text',
  value,
  onChange,
  placeholder,
  name,
  className = ''
}) => {
  return (
    <input
      type={type}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      name={name}
      className={`tag-input ${className}`}
    />
  );
};

export default Input;
