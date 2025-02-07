import React from 'react';
import './TagForm.css';

const Checkbox = ({ 
  checked,
  onChange,
  label,
  name,
  className = ''
}) => {
  return (
    <label className={`tag-checkbox-label ${className}`}>
      <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        name={name}
        className="tag-checkbox"
      />
      <span className="tag-checkbox-text">{label}</span>
    </label>
  );
};

export default Checkbox;
