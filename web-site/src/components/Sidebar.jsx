import { LayoutDashboard, Users, Database, ClipboardList, LogOut, CheckCircle2 } from 'lucide-react';
import { useNavigate, NavLink } from 'react-router-dom';
import { auth } from '../firebase';
import { signOut } from 'firebase/auth';

const Sidebar = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    await signOut(auth);
    navigate('/');
  };

  return (
    <aside className="sidebar">
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px', padding: '0 12px' }}>
        <div className="login-logo-box" style={{ width: '40px', height: '40px', margin: 0 }}>
          <CheckCircle2 size={24} color="var(--primary-green)" />
        </div>
        <div>
          <h1 style={{ fontSize: '18px', color: 'var(--primary-navy)', fontWeight: '900' }}>AviFácil</h1>
          <p style={{ fontSize: '9px', color: 'var(--text-muted)', fontWeight: '800', letterSpacing: '0.5px' }}>PRO BI & ANALYTICS</p>
        </div>
      </div>

      <div className="sidebar-label">Propriedade em Análise</div>
      <div className="card-scientific" style={{ padding: '16px', marginBottom: '24px', borderRadius: '12px' }}>
        <p className="text-bold" style={{ fontSize: '13px', color: 'var(--primary-navy)' }}>Granja Central - UNEMAT</p>
        <p style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '600' }}>Galpão: <span style={{ color: 'var(--primary-green)', fontWeight: '800' }}>Climatizado 03 ●</span></p>
      </div>

      <div className="sidebar-label">Painel Analítico</div>
      <nav>
        <NavLink to="/dashboard" className={({ isActive }) => isActive ? "nav-link active-green" : "nav-link"}>
          <LayoutDashboard size={18} />
          <span>Dashboard Decisório</span>
        </NavLink>
        <NavLink to="/registros" className="nav-link">
          <Database size={18} />
          <span>Consulta de Registros</span>
        </NavLink>
      </nav>

      <div className="sidebar-label">Segurança e Campo</div>
      <nav>
        <NavLink to="/acessos" className="nav-link">
          <Users size={18} />
          <span>Acessos App Mobile</span>
        </NavLink>
        <NavLink to="/guias" className="nav-link">
          <ClipboardList size={18} />
          <span>Guias de Decisão</span>
        </NavLink>
      </nav>

      <div className="nav-link" onClick={handleLogout} style={{ marginTop: 'auto', cursor: 'pointer', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
        <LogOut size={18} />
        <span>Sair do Sistema</span>
      </div>
    </aside>
  );
};

export default Sidebar;
