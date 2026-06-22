import React, { useState, useEffect } from 'react';
import { db, firebaseConfig } from '../firebase';
import { collection, getDocs, doc, updateDoc, setDoc, serverTimestamp } from 'firebase/firestore';
import { initializeApp, deleteApp } from 'firebase/app';
import { getAuth, createUserWithEmailAndPassword, sendPasswordResetEmail } from 'firebase/auth';
import Sidebar from '../components/Sidebar';
import {
  Users, RefreshCw, UserPlus, Power, Key, X, User, Home, Eye, EyeOff, ShieldCheck, Mail
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

const AcessosPage = () => {
  const { isAdmin, loading: authLoading } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showPassModal, setShowPassModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [showPassword, setShowPassword] = useState(false);

  const [newUser, setNewUser] = useState({ email: '', senha: '' });
  const [newPass, setNewPass] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const snapshot = await getDocs(collection(db, 'avicultores'));
      const userList = snapshot.docs.map(d => ({
        ...d.data(),
        id: d.id // Garante que o ID do documento Firestore seja o 'id' do objeto, sem ser sobrescrito
      }));
      setUsers(userList);
    } catch (error) {
      console.error("Erro ao buscar usuários:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authLoading && isAdmin) fetchUsers();
  }, [isAdmin, authLoading]);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    if (newUser.senha.length < 6) return alert("A senha deve ter pelo menos 6 caracteres.");

    setActionLoading(true);
    let secondaryApp;
    try {
      const secondaryAppName = "App_Temp_" + Date.now();
      secondaryApp = initializeApp(firebaseConfig, secondaryAppName);
      const secondaryAuth = getAuth(secondaryApp);

      const userCredential = await createUserWithEmailAndPassword(
        secondaryAuth,
        newUser.email.trim(),
        newUser.senha
      );

      await setDoc(doc(db, 'avicultores', userCredential.user.uid), {
        uid: userCredential.user.uid,
        email: newUser.email.toLowerCase().trim(),
        status: 'ATIVO',
        perfilCompleto: false,
        dataCriacao: new Date()
      });

      alert("Sucesso! Usuário criado com e-mail e senha definidos.");
      setShowModal(false);
      setNewUser({ email: '', senha: '' });
      fetchUsers();
    } catch (error) {
      if (error.code === 'auth/email-already-in-use') {
        alert("Atenção: Este e-mail já está cadastrado no sistema. Não é possível criar duas contas com o mesmo e-mail.");
      } else {
        alert("Erro ao criar: " + error.message);
      }
    } finally {
      if (secondaryApp) await deleteApp(secondaryApp);
      setActionLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    if (e) e.preventDefault();
    if (!selectedUser) return alert("Nenhum usuário selecionado.");

    const email = selectedUser.email;
    if (!email) return alert("Erro: E-mail do usuário não identificado.");

    if (!window.confirm(`Enviar um e-mail de redefinição de senha para ${email}?`)) return;

    setActionLoading(true);
    try {
      const auth = getAuth();
      await sendPasswordResetEmail(auth, email);

      alert(`Sucesso! Um e-mail com o link para redefinir a senha foi enviado para ${email}.`);
      setShowPassModal(false);
    } catch (error) {
      console.error("Erro ao enviar e-mail de redefinição:", error);
      alert("Erro ao enviar e-mail: " + (error?.message || "Erro desconhecido"));
    } finally {
      setActionLoading(false);
    }
  };

  const toggleStatus = async (user) => {
    if (!user) return;
    const userId = user.uid || user.id;
    if (!userId) return alert("Erro: ID do usuário não identificado.");

    const isBlocking = user.status !== 'INATIVO';
    const novoStatus = isBlocking ? 'INATIVO' : 'ATIVO';

    if (!window.confirm(`Tem certeza que deseja ${isBlocking ? 'BLOQUEAR' : 'ATIVAR'} o acesso de ${user.email}?`)) return;

    setActionLoading(true);
    try {
      // Usando o formato de string única para o caminho
      const userRef = doc(db, "avicultores/" + String(userId).trim());

      await updateDoc(userRef, {
        status: novoStatus,
        bloqueado: isBlocking,
        ultimaAlteracaoStatus: serverTimestamp()
      });
      fetchUsers();
    } catch (error) {
      console.error("Erro ao atualizar status:", error);
      alert("Erro de Status: " + (error?.message || "Erro de permissão ou conexão"));
    } finally {
      setActionLoading(false);
    }
  };

  if (authLoading) return <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}><RefreshCw className="animate-spin" size={48} color="#008858" /></div>;
  if (!isAdmin) return <Navigate to="/dashboard" />;

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content" style={{ background: '#F4F7F6' }}>

        <div className="top-bar-scientific" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px', margin: 0, paddingBottom: '20px' }}>
          <div>
            <h1 style={{ color: '#0B3B75', fontSize: '26px', fontWeight: '900', margin: 0 }}>Gestão de Acessos App</h1>
            <p style={{ color: '#718096', fontWeight: '600' }} className="hidden-mobile">Controle total de usuários e senhas</p>
          </div>
          <button onClick={() => setShowModal(true)} className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 24px', borderRadius: '8px' }}>
            <UserPlus size={20} /> <span className="hidden-mobile">Novo Produtor</span>
          </button>
        </div>

        <div className="card-scientific" style={{ padding: 0, overflow: 'hidden' }}>
          <div className="table-responsive">
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#F8FAFB', textAlign: 'left', borderBottom: '2px solid #E2E8F0' }}>
                <th style={thStyle}>Produtor / Propriedade</th>
                <th style={thStyle}>E-mail de Login</th>
                <th style={thStyle}>Status</th>
                <th style={{ ...thStyle, textAlign: 'center' }}>Ações</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id} style={{ borderBottom: '1px solid #EDF2F7' }}>
                  <td style={tdStyle}>
                    <div style={{ fontWeight: 'bold', color: '#0B3B75' }}>{u.nome || 'Novo Produtor'}</div>
                    <div style={{ fontSize: '12px', color: '#718096' }}>{u.nomePropriedade || 'Pendente no App'}</div>
                  </td>
                  <td style={tdStyle}>{u.email}</td>
                  <td style={tdStyle}>
                    {u.status === 'INATIVO' ? (
                      <span style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#E53E3E', background: '#FFF5F5', padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', width: 'fit-content', border: '1px solid #FED7D7' }}>
                        <Power size={14} /> BLOQUEADO
                      </span>
                    ) : (
                      <span style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#008858', background: '#E6F4EF', padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '800', width: 'fit-content', border: '1px solid #C6F6D5' }}>
                        <ShieldCheck size={14} /> ATIVO
                      </span>
                    )}
                  </td>
                  <td style={tdStyle}>
                    <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
                      <button
                        onClick={() => { setSelectedUser(u); setShowPassModal(true); }}
                        className="act-btn blue"
                        title="Alterar Senha"
                        style={{ boxShadow: '0 2px 4px rgba(11, 59, 117, 0.1)' }}
                      >
                        <Key size={18} />
                      </button>
                      <button
                        onClick={() => toggleStatus(u)}
                        className={`act-btn ${u.status === 'INATIVO' ? 'green' : 'red'}`}
                        title={u.status === 'INATIVO' ? "Ativar Acesso" : "Bloquear Acesso"}
                        style={{ boxShadow: u.status === 'INATIVO' ? '0 2px 4px rgba(0, 136, 88, 0.1)' : '0 2px 4px rgba(229, 62, 62, 0.1)' }}
                      >
                        <Power size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

        {/* Modal Novo Usuário */}
        {showModal && (
          <div className="adm-modal-overlay">
            <div className="adm-modal-content" style={{ padding: 0, overflow: 'hidden', maxWidth: '500px' }}>
              <div style={{ background: '#0B3B75', padding: '30px', color: 'white', position: 'relative' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                  <div style={{ background: 'rgba(255,255,255,0.2)', padding: '10px', borderRadius: '12px' }}>
                    <UserPlus size={24} color="white" />
                  </div>
                  <div>
                    <h3 style={{ margin: 0, fontSize: '20px', fontWeight: '900' }}>Novo Produtor</h3>
                    <p style={{ margin: 0, opacity: 0.8, fontSize: '13px' }}>Acesso direto ao App AviFácil</p>
                  </div>
                </div>
                <X
                  onClick={() => setShowModal(false)}
                  style={{ position: 'absolute', top: '25px', right: '25px', cursor: 'pointer', opacity: 0.6 }}
                />
              </div>

              <form onSubmit={handleCreateUser} style={{ padding: '30px' }}>
                <div className="adm-field">
                  <label><Mail size={14} style={{ marginRight: '6px' }} /> E-mail (Login de acesso)</label>
                  <input type="email" required value={newUser.email} onChange={e => setNewUser({...newUser, email: e.target.value})} placeholder="exemplo@email.com" />
                </div>

                <div className="adm-field">
                  <label><Key size={14} style={{ marginRight: '6px' }} /> Definir Senha</label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type={showPassword ? "text" : "password"}
                      required
                      value={newUser.senha}
                      onChange={e => setNewUser({...newUser, senha: e.target.value})}
                      placeholder="Mínimo 6 dígitos"
                      style={{ paddingRight: '45px' }}
                    />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} style={eyeBtnStyle}>
                      {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                    </button>
                  </div>
                </div>

                <div style={{ background: '#F0F9FF', border: '1px solid #BAE6FD', padding: '15px', borderRadius: '12px', marginBottom: '25px', display: 'flex', gap: '12px' }}>
                  <ShieldCheck size={20} color="#0284C7" style={{ flexShrink: 0 }} />
                  <p style={{ margin: 0, fontSize: '12px', color: '#0369A1', lineHeight: '1.5' }}>
                    <strong>Nota de Segurança:</strong> O produtor usará este e-mail e senha para logar no celular. Não é necessário confirmação por e-mail.
                  </p>
                </div>

                <button type="submit" disabled={actionLoading} className="btn-primary" style={{ width: '100%', padding: '16px', borderRadius: '12px', fontWeight: '800', fontSize: '15px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px' }}>
                  {actionLoading ? <RefreshCw className="animate-spin" size={20} /> : <><ShieldCheck size={18} /> CRIAR CONTA AGORA</>}
                </button>
              </form>
            </div>
          </div>
        )}

        {/* Modal Alterar Senha */}
        {showPassModal && (
          <div className="adm-modal-overlay">
            <div className="adm-modal-content" style={{ padding: 0, overflow: 'hidden', maxWidth: '420px' }}>
              <div style={{ background: '#0B3B75', padding: '25px', color: 'white', position: 'relative' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div style={{ background: 'rgba(255,255,255,0.2)', padding: '8px', borderRadius: '10px' }}>
                    <Key size={20} color="white" />
                  </div>
                  <div>
                    <h3 style={{ margin: 0, fontSize: '18px', fontWeight: '900' }}>Alterar Senha</h3>
                    <p style={{ margin: 0, opacity: 0.8, fontSize: '12px' }}>{selectedUser?.email}</p>
                  </div>
                </div>
                <X
                  onClick={() => { setShowPassModal(false); setNewPass(''); }}
                  style={{ position: 'absolute', top: '22px', right: '22px', cursor: 'pointer', opacity: 0.6 }}
                />
              </div>

              <div style={{ padding: '25px' }}>
                <div style={{ background: '#F0F9FF', border: '1px solid #BAE6FD', padding: '15px', borderRadius: '12px', marginBottom: '25px', display: 'flex', gap: '12px' }}>
                  <Mail size={20} color="#0284C7" style={{ flexShrink: 0 }} />
                  <p style={{ margin: 0, fontSize: '13px', color: '#0369A1', lineHeight: '1.5' }}>
                    Um link de redefinição será enviado para o e-mail: <strong>{selectedUser?.email}</strong>. O produtor poderá escolher sua própria senha.
                  </p>
                </div>

                <button
                  onClick={handleChangePassword}
                  disabled={actionLoading}
                  className="btn-primary"
                  style={{ width: '100%', padding: '14px', borderRadius: '10px', fontWeight: '800', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px' }}
                >
                  {actionLoading ? <RefreshCw className="animate-spin" size={20} /> : <><Mail size={18} /> ENVIAR E-MAIL DE RECUPERAÇÃO</>}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>

      <style>{`
        .adm-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(11, 59, 117, 0.5); backdrop-filter: blur(5px); display: flex; align-items: center; justify-content: center; z-index: 1000; }
        .adm-modal-content { background: white; border-radius: 20px; width: 95%; maxWidth: 480px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.3); animation: modalIn 0.3s ease-out; }
        @keyframes modalIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
        .adm-field { marginBottom: 18px; }
        .adm-field label { display: flex; align-items: center; fontSize: 12px; fontWeight: '800'; color: #4A5568; marginBottom: 8px; text-transform: uppercase; letter-spacing: 0.5px; }
        .adm-field input { width: 100%; padding: 12px 15px; borderRadius: 10px; border: 1.5px solid #E2E8F0; fontSize: 14px; outline: none; transition: all 0.2s; background: #F8FAFB; }
        .adm-field input:focus { border-color: #0B3B75; background: white; box-shadow: 0 0 0 4px rgba(11, 59, 117, 0.1); }
        .act-btn { border: none; width: 38px; height: 38px; borderRadius: 10px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
        .act-btn.blue { background: #EBF4FF; color: #0B3B75; }
        .act-btn.red { background: #FFF5F5; color: #E53E3E; }
        .act-btn.green { background: #E6F4EF; color: #008858; }
      `}</style>
    </div>
  );
};

const thStyle = { padding: '18px 20px', fontSize: '11px', color: '#718096', textTransform: 'uppercase', fontWeight: '800' };
const tdStyle = { padding: '18px 20px', fontSize: '14px', color: '#2D3748' };
const eyeBtnStyle = { position: 'absolute', right: '12px', top: '12px', border: 'none', background: 'none', cursor: 'pointer', color: '#A0AEC0' };

export default AcessosPage;
