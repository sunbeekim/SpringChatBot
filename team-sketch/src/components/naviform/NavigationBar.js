import React from 'react';
import Button from '../tagform/Button';
import './NavigationBar.css';

const NavigationBar = ({ items }) => {
  return (
    <nav className="navigation-container">
      <ul className="navigation-list">
        {items.map((item, index) => (
          <li key={index} className="navigation-item">
            <Button 
              onClick={item.onClick} 
              className="navigation-button"
            >
              {item.label}
            </Button>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default NavigationBar;
