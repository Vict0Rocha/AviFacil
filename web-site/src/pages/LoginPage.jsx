import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';
import { signInWithEmailAndPassword, sendPasswordResetEmail, setPersistence, browserSessionPersistence } from 'firebase/auth';
import { Eye, EyeOff, Lock, User, RefreshCw } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loadingAction, setLoadingAction] = useState(false);
  const navigate = useNavigate();
  const { user, loading } = useAuth();

  useEffect(() => {
    if (!loading && user) {
      navigate('/dashboard');
    }
  }, [user, loading, navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoadingAction(true);
    try {
      // Define a persistência para SESSION (limpa ao fechar a aba/navegador)
      await setPersistence(auth, browserSessionPersistence);
      await signInWithEmailAndPassword(auth, email, senha);
    } catch (err) {
      console.error(err);
      setError('E-mail ou senha incorretos.');
      setLoadingAction(false);
    }
  };

  const handleForgotPassword = async () => {
    if (!email) {
      setError('Por favor, insira seu e-mail para recuperar a senha.');
      return;
    }
    setError('');
    setMessage('');
    setLoadingAction(true);
    try {
      await sendPasswordResetEmail(auth, email);
      setMessage('E-mail de recuperação enviado! Verifique sua caixa de entrada.');
    } catch (err) {
      setError('Erro ao enviar e-mail de recuperação. Verifique o endereço digitado.');
    } finally {
      setLoadingAction(false);
    }
  };

  return (
    <div className="login-page-wrapper">
      <div className="login-container-box">
        <h1 className="logo-title-login">Portal AviFácil</h1>

        <form onSubmit={handleLogin}>
          <div className="input-group-login">
            <User size={18} />
            <input
              type="email"
              className="input-field-login"
              placeholder="E-mail"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="input-group-login">
            <Lock size={18} />
            <input
              type={showPassword ? "text" : "password"}
              className="input-field-login"
              placeholder="Senha"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
              style={{ paddingRight: '45px' }}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                position: 'absolute',
                right: '15px',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#0B3B75',
                display: 'flex',
                alignItems: 'center',
                padding: 0
              }}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          <div style={{ textAlign: 'right', marginBottom: '20px' }}>
            <button
              type="button"
              onClick={handleForgotPassword}
              style={{
                background: 'none',
                border: 'none',
                color: '#0B3B75',
                fontSize: '13px',
                fontWeight: '600',
                cursor: 'pointer',
                textDecoration: 'underline'
              }}
            >
              Esqueci minha senha
            </button>
          </div>

          {error && <p style={{ color: '#E53E3E', fontSize: '13px', marginBottom: '15px', fontWeight: 'bold' }}>{error}</p>}
          {message && <p style={{ color: '#008858', fontSize: '13px', marginBottom: '15px', fontWeight: 'bold' }}>{message}</p>}

          <button type="submit" className="btn-login-action" disabled={loadingAction}>
            {loadingAction ? <RefreshCw className="animate-spin" size={20} style={{ margin: '0 auto' }} /> : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
