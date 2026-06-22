import { createContext, useContext, useState, useEffect } from 'react';
import { auth, db } from '../firebase';
import { onAuthStateChanged, signOut } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [userData, setUserData] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  // Monitor de Inatividade Global (20 minutos)
  useEffect(() => {
    const INACTIVITY_LIMIT = 20 * 60 * 1000; // 20 min
    const CHECK_INTERVAL = 30000; // Verifica a cada 30 segundos

    const resetTimer = () => {
      if (user) {
        localStorage.setItem('lastActivity', Date.now().toString());
      }
    };

    const checkInactivity = () => {
      if (user) {
        const lastActivity = parseInt(localStorage.getItem('lastActivity') || '0');
        const now = Date.now();

        if (now - lastActivity > INACTIVITY_LIMIT) {
          console.warn("Sessão expirada por inatividade.");
          signOut(auth);
          localStorage.removeItem('lastActivity');
          alert("Sua sessão expirou por inatividade para sua segurança.");
        }
      }
    };

    if (user) {
      // Registrar atividade inicial
      resetTimer();

      // Eventos de interação do usuário
      const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
      events.forEach(event => window.addEventListener(event, resetTimer));

      // Intervalo de verificação
      const interval = setInterval(checkInactivity, CHECK_INTERVAL);

      return () => {
        events.forEach(event => window.removeEventListener(event, resetTimer));
        clearInterval(interval);
      };
    }
  }, [user]);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      try {
        setUser(currentUser);
        if (currentUser) {
          // Lista de e-mails administrativos (sempre em minúsculas para comparação)
          const adminsPermitidos = [
            (import.meta.env?.VITE_ADMIN_EMAIL || "").toLowerCase().trim(),
            "cristianoadm@gmail.com",
            "cristiano@gmail.com"
          ];

          const emailLogado = (currentUser.email || "").toLowerCase().trim();
          const ehAdmin = adminsPermitidos.includes(emailLogado);

          setIsAdmin(!!ehAdmin);
          console.log("LOGIN DETECTADO:", emailLogado, "| É ADMIN:", !!ehAdmin);

          const docRef = doc(db, "avicultores", currentUser.uid);
          const docSnap = await getDoc(docRef);
          if (docSnap.exists()) {
            setUserData(docSnap.data());
          }
        } else {
          setUserData(null);
          setIsAdmin(false);
          localStorage.removeItem('lastActivity');
        }
      } catch (error) {
        console.error("Erro na autenticação:", error);
      } finally {
        setLoading(false);
      }
    });
    return () => unsubscribe();
  }, []);

  return (
    <AuthContext.Provider value={{ user, userData, isAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
