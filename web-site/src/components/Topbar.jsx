import { Bell, Search, ChevronDown } from 'lucide-react';

const Topbar = ({ title, subtitle, user }) => {
  return (
    <header style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginBottom: '32px',
      background: 'white',
      padding: '16px 24px',
      borderRadius: '12px',
      boxShadow: 'var(--shadow-sm)',
      border: '1px solid var(--border-color)'
    }}>
      <div>
        <h2 style={{ fontSize: '18px', fontWeight: '800', color: 'var(--primary-navy)' }}>{title}</h2>
        {subtitle && <p style={{ color: 'var(--text-muted)', fontSize: '12px', fontWeight: '500' }}>{subtitle}</p>}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div style={{ position: 'relative', display: 'none', md: 'block' }}>
          <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            placeholder="Pesquisar..."
            style={{
              padding: '10px 12px 10px 40px',
              borderRadius: '8px',
              border: '1px solid var(--border-color)',
              fontSize: '14px',
              width: '200px',
              background: '#F8FAFC'
            }}
          />
        </div>

        <div style={{ position: 'relative', cursor: 'pointer' }}>
          <Bell size={20} color="var(--text-muted)" />
          <span style={{ position: 'absolute', top: '-2px', right: '-2px', width: '8px', height: '8px', background: 'var(--error-red)', borderRadius: '50%', border: '2px solid white' }}></span>
        </div>

        <div style={{ width: '1px', height: '24px', background: 'var(--border-color)' }}></div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '10px',
            background: 'var(--primary-navy)',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px',
            fontWeight: '700'
          }}>
            {user?.nome?.charAt(0) || 'U'}
          </div>
          <div className="user-info-hide" style={{ display: 'flex', flexDirection: 'column' }}>
             <span style={{ fontSize: '13px', fontWeight: '700' }}>{user?.nome || 'Usuário'}</span>
             <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Administrador</span>
          </div>
          <ChevronDown size={14} color="var(--text-muted)" />
        </div>
      </div>
    </header>
  );
};

export default Topbar;
