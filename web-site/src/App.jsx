import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import RegistrosPage from './pages/RegistrosPage';
import AcessosPage from './pages/AcessosPage';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/registros"
          element={
            <ProtectedRoute>
              <RegistrosPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/acessos"
          element={
            <ProtectedRoute>
              <AcessosPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
