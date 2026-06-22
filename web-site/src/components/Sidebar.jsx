import { LayoutDashboard, Users, Database, ClipboardList, LogOut, CheckCircle2, Menu, X } from 'lucide-react';
import { useNavigate, NavLink } from 'react-router-dom';
import { auth } from '../firebase';
import { signOut } from 'firebase/auth';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';

const Sidebar = () => {
  const navigate = useNavigate();
  const { userData, isAdmin } = useAuth();
  const [isOpen, setIsOpen] = useState(false);

  const handleLogout = async () => {
    await signOut(auth);
    navigate('/');
  };

  const toggleSidebar = () => setIsOpen(!isOpen);

  return (
    <>
      {/* Botão Hambúrguer para Mobile */}
      <button
        className="show-mobile"
        onClick={toggleSidebar}
        style={{
          position: 'fixed',
          top: '15px',
          right: '15px',
          zIndex: 1100,
          background: 'var(--primary-navy)',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          padding: '8px',
          cursor: 'pointer',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
        }}
      >
        {isOpen ? <X size={24} /> : <Menu size={24} />}
      </button>

      {/* Overlay para fechar ao clicar fora no mobile */}
      {isOpen && (
        <div
          className="show-mobile"
          onClick={toggleSidebar}
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            zIndex: 1000
          }}
        />
      )}

      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px', padding: '0 12px' }}>
          <div className="login-logo-box" style={{ width: '40px', height: '40px', margin: 0 }}>
            <CheckCircle2 size={24} color="var(--primary-green)" />
          </div>
          <div>
            <h1 style={{ fontSize: '18px', color: 'var(--primary-navy)', fontWeight: '900' }}>AviFácil</h1>
            <p style={{ fontSize: '9px', color: 'var(--text-muted)', fontWeight: '800', letterSpacing: '0.5px' }}>PRO BI & ANALYTICS</p>
          </div>
        </div>


      <div className="sidebar-label">{isAdmin ? "Visão Administrativa" : "Propriedade em Análise"}</div>
      <div className="card-scientific" style={{ padding: '16px', marginBottom: '24px', borderRadius: '12px' }}>
        {isAdmin ? (
          <>
            <p className="text-bold" style={{ fontSize: '13px', color: 'var(--primary-navy)' }}>
              Painel Global de Controle
            </p>
            <p style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '600' }}>
              Status: <span style={{ color: 'var(--primary-green)', fontWeight: '800' }}>Administrador</span>
            </p>
          </>
        ) : (
          <>
            <p className="text-bold" style={{ fontSize: '13px', color: 'var(--primary-navy)' }}>
              {userData?.nomePropriedade || "Carregando..."}
            </p>
            <p style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: '600' }}>
              Produtor: <span style={{ color: 'var(--primary-green)', fontWeight: '800' }}>{userData?.nome || "---"}</span>
            </p>
          </>
        )}
      </div>

      <div className="sidebar-label">Painel Analítico</div>
      <nav>
        {isAdmin && (
          <NavLink to="/dashboard" className={({ isActive }) => isActive ? "nav-link active-green" : "nav-link"}>
            <LayoutDashboard size={18} />
            <span>Dashboard Geral</span>
          </NavLink>
        )}
        <NavLink to="/registros" className={({ isActive }) => isActive ? "nav-link active-green" : "nav-link"}>
          <Database size={18} />
          <span>Consulta por produtor</span>
        </NavLink>
      </nav>

      {isAdmin && (
        <>
          <div className="sidebar-label">Gestão e Segurança</div>
          <nav>
            <NavLink to="/acessos" className={({ isActive }) => isActive ? "nav-link active-green" : "nav-link"}>
              <Users size={18} />
              <span>Acessos App Mobile</span>
            </NavLink>
          </nav>
        </>
      )}

      <div className="nav-link" onClick={handleLogout} style={{ marginTop: 'auto', cursor: 'pointer', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
        <LogOut size={18} />
        <span>Sair do Sistema</span>
      </div>
    </aside>
    </>
  );
};

export default Sidebar;
