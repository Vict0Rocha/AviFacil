import { Lightbulb, ArrowRight } from 'lucide-react';

const RecommendationPanel = ({ text }) => {
  return (
    <div style={{
      background: 'linear-gradient(90deg, #F0F4F8 0%, #FFFFFF 100%)',
      borderLeft: '4px solid var(--primary-navy)',
      borderRadius: '12px',
      padding: '24px',
      display: 'flex',
      gap: '20px',
      alignItems: 'center',
      marginTop: '32px',
      boxShadow: 'var(--shadow-sm)'
    }}>
      <div style={{ background: 'var(--primary-navy)', color: 'white', padding: '12px', borderRadius: '10px' }}>
        <Lightbulb size={24} />
      </div>
      <div style={{ flex: 1 }}>
        <h4 style={{ color: 'var(--primary-navy)', fontWeight: '700', marginBottom: '4px', fontSize: '15px' }}>Insights do Lote</h4>
        <p style={{ color: 'var(--text-main)', fontSize: '14px', lineHeight: '1.5' }}>{text}</p>
      </div>
      <button style={{
        background: 'transparent',
        border: '1px solid var(--primary-navy)',
        color: 'var(--primary-navy)',
        padding: '8px 16px',
        borderRadius: '8px',
        fontSize: '13px',
        fontWeight: '700',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        cursor: 'pointer'
      }}>
        Ver Detalhes <ArrowRight size={16} />
      </button>
    </div>
  );
};

export default RecommendationPanel;
