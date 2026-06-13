import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { Eye, EyeOff, Lock, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
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
    try {
      await signInWithEmailAndPassword(auth, email, senha);
    } catch (err) {
      setError('E-mail ou senha incorretos.');
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

          {error && <p style={{ color: '#ff8080', fontSize: '13px', marginBottom: '15px', fontWeight: 'bold' }}>{error}</p>}

          <button type="submit" className="btn-login-action">Entrar</button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
