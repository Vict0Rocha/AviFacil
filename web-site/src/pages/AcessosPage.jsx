import React, { useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import { Users, ShieldCheck, Mail, Calendar, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

const AcessosPage = () => {
  const { isAdmin, loading: authLoading } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const formatDate = (dateVal) => {
    if (!dateVal) return "---";
    if (dateVal.toDate) return dateVal.toDate().toLocaleDateString('pt-BR');
    if (dateVal instanceof Date) return dateVal.toLocaleDateString('pt-BR');
    const d = new Date(dateVal);
    return isNaN(d.getTime()) ? "---" : d.toLocaleDateString('pt-BR');
  };

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const snapshot = await getDocs(collection(db, 'avicultores'));
        const userList = snapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        }));
        setUsers(userList);
      } catch (error) {
        console.error("Erro ao buscar usuários:", error);
      } finally {
        setLoading(false);
      }
    };

    if (!authLoading && isAdmin) {
      fetchUsers();
    } else if (!authLoading && !isAdmin) {
      setLoading(false);
    }
  }, [isAdmin, authLoading]);

  if (authLoading) return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: '#F8FAFB' }}>
      <RefreshCw className="animate-spin" size={48} color="#008858" />
    </div>
  );

  if (!isAdmin) return <Navigate to="/dashboard" />;

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <div className="top-bar-scientific">
          <div>
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>Gestão de Acessos</h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Usuários com acesso ao aplicativo mobile</p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--primary-green)', fontWeight: '700' }}>
            <ShieldCheck size={20} /> Modo Administrador
          </div>
        </div>

        <div className="card-scientific">
          <div className="table-responsive">
            <table className="info-table">
              <thead>
                <tr>
                  <th>Nome do Produtor</th>
                  <th>E-mail</th>
                  <th>Propriedade</th>
                  <th>Data de Cadastro</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '40px' }}>
                      <RefreshCw className="animate-spin" style={{ margin: '0 auto' }} size={24} color="#008858" />
                    </td>
                  </tr>
                ) : users.length > 0 ? (
                  users.map(u => (
                    <tr key={u.id}>
                      <td><strong>{u.nome || "Não informado"}</strong></td>
                      <td><div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}><Mail size={14}/> {u.email || "---"}</div></td>
                      <td>{u.nomePropriedade || "---"}</td>
                      <td><div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}><Calendar size={14}/> {formatDate(u.dataCadastro)}</div></td>
                      <td><span className="badge ATIVO">Ativo</span></td>
                    </tr>
                  ))
                ) : (
                  <tr><td colSpan="5" style={{ textAlign: 'center', padding: '20px' }}>Nenhum usuário encontrado.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AcessosPage;
