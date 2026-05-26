import React from 'react';

const IndicatorCard = ({ title, value, unit, valueColor }) => {
  return (
    <div className="card-scientific" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
      <p style={{ fontSize: '14px', fontWeight: '700', color: 'var(--text-muted)', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
        {title}
      </p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
        <h3 style={{
          fontSize: '36px',
          fontWeight: '900',
          color: valueColor || 'var(--primary-navy)',
          letterSpacing: '-1px',
          margin: 0
        }}>
          {value}
        </h3>
        {unit && <span style={{ fontSize: '16px', fontWeight: '700', color: 'var(--text-muted)' }}>{unit}</span>}
      </div>
    </div>
  );
};

export default IndicatorCard;
