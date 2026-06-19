import { initializeApp, getApps, getApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// Acesso seguro ao ambiente Vite
const env = import.meta.env || {};

export const firebaseConfig = {
  apiKey: env.VITE_FIREBASE_API_KEY || "AIzaSyDalpnzo3k3lLjCwkTU_hupwkdfOay9vyE",
  authDomain: env.VITE_FIREBASE_AUTH_DOMAIN || "avifacil.firebaseapp.com",
  projectId: env.VITE_FIREBASE_PROJECT_ID || "avifacil",
  storageBucket: env.VITE_FIREBASE_STORAGE_BUCKET || "avifacil.firebasestorage.app",
  messagingSenderId: env.VITE_FIREBASE_MESSAGING_SENDER_ID || "890428953647",
  appId: env.VITE_FIREBASE_APP_ID || "1:890428953647:web:411728ecc990f86aa2a2da"
};

let app;
try {
  app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();
} catch (error) {
  console.error("Erro ao inicializar Firebase:", error);
  app = getApps()[0]; // Tenta recuperar app já inicializado se houver erro
}

export const auth = getAuth(app);
export const db = getFirestore(app);
