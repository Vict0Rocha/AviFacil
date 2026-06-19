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

  // Monitor de Inatividade (20 minutos)
  useEffect(() => {
    let inactivityTimer;
    const INACTIVITY_LIMIT = 20 * 60 * 1000; // 20 min em milissegundos

    const resetTimer = () => {
      if (inactivityTimer) clearTimeout(inactivityTimer);

      if (user) {
        inactivityTimer = setTimeout(() => {
          console.warn("Sessão expirada por inatividade (20 min).");
          signOut(auth);
          alert("Sua sessão expirou por inatividade. Por favor, faça login novamente.");
        }, INACTIVITY_LIMIT);
      }
    };

    if (user) {
      // Eventos que reiniciam o timer de inatividade
      const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];

      events.forEach(event => window.addEventListener(event, resetTimer));
      resetTimer(); // Inicia o timer pela primeira vez ao logar

      return () => {
        if (inactivityTimer) clearTimeout(inactivityTimer);
        events.forEach(event => window.removeEventListener(event, resetTimer));
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
