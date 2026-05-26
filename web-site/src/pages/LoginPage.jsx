import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';
import { signInWithEmailAndPassword } from 'firebase/auth';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      await signInWithEmailAndPassword(auth, email, senha);
      navigate('/dashboard');
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
            <i className="fa-regular fa-user"></i>
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
            <i className="fa-solid fa-lock"></i>
            <input
              type="password"
              className="input-field-login"
              placeholder="Senha"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
            />
          </div>

          {error && <p style={{ color: '#ff8080', fontSize: '13px', marginBottom: '15px', fontWeight: 'bold' }}>{error}</p>}

          <button type="submit" className="btn-login-action">Entrar</button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
