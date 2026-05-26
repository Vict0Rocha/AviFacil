import React from 'react';

const IndicatorCard = ({ title, value, unit, icon: Icon, trend }) => {
  return (
    <div className="card-scientific">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
        <div style={{ background: '#F1F5F9', padding: '10px', borderRadius: '12px', color: 'var(--primary-navy)' }}>
          <Icon size={22} />
        </div>
        {trend && (
          <span style={{
            fontSize: '12px',
            fontWeight: '800',
            color: trend.startsWith('+') ? '#ef4444' : 'var(--primary-green)',
            background: trend.startsWith('+') ? '#fee2e2' : '#E6F4EF',
            padding: '4px 10px',
            borderRadius: '20px'
          }}>
            {trend}
          </span>
        )}
      </div>

      <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-secondary)', marginBottom: '4px' }}>{title}</p>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px' }}>
        <h3 style={{ fontSize: '32px', fontWeight: '900', color: 'var(--primary-navy)', letterSpacing: '-1px' }}>{value}</h3>
        <span style={{ fontSize: '14px', fontWeight: '700', color: 'var(--text-muted)' }}>{unit}</span>
      </div>
    </div>
  );
};

export default IndicatorCard;
