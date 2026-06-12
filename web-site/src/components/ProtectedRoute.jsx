import { Navigate } from 'react-router-dom';
import { auth } from '../firebase';
import { useState, useEffect } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { RefreshCw } from 'lucide-react';

const ProtectedRoute = ({ children }) => {
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: '#F8FAFB' }}>
        <RefreshCw className="animate-spin" size={48} color="#008858" />
      </div>
    );
  }

  if (!user) {
    // Redireciona para o login se não houver usuário autenticado
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;
