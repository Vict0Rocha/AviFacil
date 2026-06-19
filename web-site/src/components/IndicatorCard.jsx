import React from 'react';

const IndicatorCard = ({ title, value, unit, valueColor, small }) => {
  return (
    <div className="card-scientific" style={{
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      padding: small ? '16px' : '24px',
      minHeight: small ? 'auto' : '120px'
    }}>
      <p style={{
        fontSize: small ? '11px' : '14px',
        fontWeight: '800',
        color: 'var(--text-muted)',
        marginBottom: small ? '4px' : '8px',
        textTransform: 'uppercase',
        letterSpacing: '0.5px'
      }}>
        {title}
      </p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
        <h3 style={{
          fontSize: small ? '24px' : '36px',
          fontWeight: '900',
          color: valueColor || 'var(--primary-navy)',
          letterSpacing: '-1px',
          margin: 0
        }}>
          {value}
        </h3>
        {unit && <span style={{ fontSize: small ? '12px' : '16px', fontWeight: '700', color: 'var(--text-muted)' }}>{unit}</span>}
      </div>
    </div>
  );
};

export default IndicatorCard;
