import { useState, useEffect } from 'react';
import './LoadingSVG.css';

const LoadingSVG = () => {
  const [planets, setPlanets] = useState([
    { id: 'sun', r: 6, orbit: 0, speed: 0, color: '#FDB813', angle: 0 }, // 태양
    { id: 'mercury', r: 3, orbit: 25, speed: 4.1, color: '#A0522D', angle: 0 }, // 수성
    { id: 'venus', r: 4, orbit: 35, speed: 1.6, color: '#FFA07A', angle: 0 }, // 금성
    { id: 'earth', r: 5, orbit: 45, speed: 1, color: '#4B9CD3', angle: 0 }, // 지구
    { id: 'mars', r: 4, orbit: 55, speed: 0.5, color: '#CD5C5C', angle: 0 } // 화성
  ]);

  useEffect(() => {
    let animationFrameId;
    
    const tick = () => {
      setPlanets(prevPlanets => 
        prevPlanets.map(planet => ({
          ...planet,
          angle: planet.angle + planet.speed * 0.02
        }))
      );
      animationFrameId = requestAnimationFrame(tick);
    };

    tick();
    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, []);

  const centerX = 60;
  const centerY = 60;

  return (
    <div className="loading">
      Loading...
      <svg width="120" height="120">
        {planets.map((planet) => {
          const x = centerX + planet.orbit * Math.cos(planet.angle);
          const y = centerY + planet.orbit * Math.sin(planet.angle);

          return (
            <g key={planet.id}>
              {/* 궤도 */}
              {planet.orbit > 0 && (
                <circle
                  cx={centerX}
                  cy={centerY}
                  r={planet.orbit}
                  fill="none"
                  stroke="#333"
                  strokeWidth="0.5"
                />
              )}
              {/* 행성 */}
              <circle r={planet.r} cx={x} cy={y} fill={planet.color} />
            </g>
          );
        })}
      </svg>
    </div>
  );
};

export default LoadingSVG;
