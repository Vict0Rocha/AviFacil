import { useState, useEffect, useMemo } from 'react';
import { db } from '../firebase';
import { collection, getDocs, query, orderBy } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import {
  RefreshCw, Search, Calendar, Weight, Wheat, Skull, Info, TrendingUp, Download
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, AreaChart, Area
} from 'recharts';

const RegistrosPage = () => {
  const { user } = useAuth();
  const [lotes, setLotes] = useState([]);
  const [selectedLoteId, setSelectedLoteId] = useState('');
  const [registros, setRegistros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingRegs, setLoadingRegs] = useState(false);
  const [error, setError] = useState(null);

  const selectedLoteData = useMemo(() =>
    lotes.find(l => l.id === selectedLoteId),
  [lotes, selectedLoteId]);

  // Prepara os dados para o gráfico (invertendo a ordem para cronológica)
  const chartData = useMemo(() => {
    return [...registros].reverse().map(reg => ({
      dia: `Dia ${reg.idadeLote}`,
      peso: reg.pesoAtualMedio || 0,
      consumo: reg.consumoRacaoPeriodo || 0,
    }));
  }, [registros]);

  useEffect(() => {
    if (user) fetchLotes(user.uid);
  }, [user]);

  const fetchLotes = async (uid) => {
    try {
      const lotesRef = collection(db, `avicultores/${uid}/lotes`);
      const snapshot = await getDocs(lotesRef);
      const lotesList = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setLotes(lotesList);
      if (lotesList.length > 0) {
        setSelectedLoteId(lotesList[0].id);
        fetchRegistros(uid, lotesList[0].id);
      }
    } catch (err) {
      setError("Erro ao carregar lotes.");
    } finally {
      setLoading(false);
    }
  };

  const fetchRegistros = async (uid, loteId) => {
    setLoadingRegs(true);
    try {
      const regsRef = collection(db, `avicultores/${uid}/lotes/${loteId}/registros`);
      const q = query(regsRef, orderBy("dataRegistro", "desc"));
      const snapshot = await getDocs(q);
      setRegistros(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    } catch (err) {
      setError("Erro ao carregar registros.");
    } finally {
      setLoadingRegs(false);
    }
  };

  const formatDate = (dateVal) => {
    if (!dateVal) return "---";
    const date = dateVal.toDate ? dateVal.toDate() : new Date(dateVal);
    return date.toLocaleDateString('pt-BR');
  };

  if (loading) return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: '#F8FAFB' }}>
      <RefreshCw className="animate-spin" size={48} color="#008858" />
    </div>
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <div className="top-bar-scientific">
          <div>
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>Análise de Registros</h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Rastreabilidade e indicadores por lote</p>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            <div style={{ position: 'relative' }}>
              <select
                value={selectedLoteId}
                onChange={(e) => {
                  setSelectedLoteId(e.target.value);
                  fetchRegistros(user.uid, e.target.value);
                }}
                className="input-field-login"
                style={{ width: '220px', padding: '8px 8px 8px 35px', height: '40px', fontSize: '13px', margin: 0 }}
              >
                {lotes.map(lote => <option key={lote.id} value={lote.id}>Lote {lote.numeroLote || lote.id}</option>)}
              </select>
              <Search size={16} style={{ position: 'absolute', left: '10px', top: '12px', color: 'var(--primary-blue)' }} />
            </div>
            <button className="btn-login-action" style={{ width: 'auto', padding: '0 16px', height: '40px', margin: 0, fontSize: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Download size={16} /> Exportar
            </button>
          </div>
        </div>

        {selectedLoteData && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px', marginBottom: '32px' }}>
            {/* Seção de Gráfico */}
            <div className="card-scientific" style={{ gridColumn: 'span 2', minHeight: '350px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                <h4 style={{ color: 'var(--primary-navy)', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <TrendingUp size={18} color="var(--primary-green)"/> Evolução do Peso (g)
                </h4>
              </div>
              <div style={{ width: '100%', height: '280px' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData}>
                    <defs>
                      <linearGradient id="colorPeso" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#008858" stopOpacity={0.1}/>
                        <stop offset="95%" stopColor="#008858" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                    <XAxis dataKey="dia" axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#718096'}} />
                    <YAxis axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#718096'}} />
                    <Tooltip
                      contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                    />
                    <Area type="monotone" dataKey="peso" stroke="#008858" strokeWidth={3} fillOpacity={1} fill="url(#colorPeso)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Sidebar de Info do Lote */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="card-scientific" style={{ padding: '20px' }}>
                <h4 style={{ fontSize: '14px', marginBottom: '15px', color: 'var(--primary-navy)', fontWeight: '800' }}>Detalhes do Alojamento</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Data Início</span>
                    <span style={{ fontWeight: '700' }}>{formatDate(selectedLoteData.dataInicio)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Qtd. Inicial</span>
                    <span style={{ fontWeight: '700' }}>{selectedLoteData.quantidadeAvesInicial}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Galpão</span>
                    <span style={{ fontWeight: '700', color: 'var(--primary-green)' }}>{selectedLoteData.galpao}</span>
                  </div>
                </div>
              </div>
              <div className="card-scientific" style={{ padding: '20px', background: 'var(--primary-navy)', color: 'white' }}>
                <p style={{ fontSize: '11px', opacity: 0.8, textTransform: 'uppercase', fontWeight: '800' }}>Status Operacional</p>
                <p style={{ fontSize: '18px', fontWeight: '800', marginTop: '4px' }}>Lote em Conformidade</p>
                <div style={{ marginTop: '15px', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px' }}>
                  <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#48BB78' }}></div>
                  Sincronizado com App Mobile
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="card-scientific">
          <h3 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800' }}>Linha do Tempo de Campo</h3>
          <div className="table-responsive">
            <table className="info-table">
              <thead>
                <tr>
                  <th>Data</th>
                  <th>Idade</th>
                  <th>Mortes</th>
                  <th>Ração (kg)</th>
                  <th>Peso Médio (g)</th>
                  <th>Ambiência</th>
                </tr>
              </thead>
              <tbody>
                {registros.length === 0 ? (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>Nenhum dado registrado.</td></tr>
                ) : (
                  registros.map((reg) => (
                    <tr key={reg.id}>
                      <td><strong>{formatDate(reg.dataRegistro)}</strong></td>
                      <td><span style={{ background: '#EBF4FF', color: '#3182CE', padding: '4px 10px', borderRadius: '6px', fontWeight: '800', fontSize: '12px' }}>{reg.idadeLote} dias</span></td>
                      <td style={{ color: '#E53E3E', fontWeight: '800' }}>{reg.avesMortasPeriodo || 0}</td>
                      <td>{reg.consumoRacaoPeriodo || 0} kg</td>
                      <td><strong>{reg.pesoAtualMedio || 0} g</strong></td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#2D8A4E', fontWeight: '700', fontSize: '12px' }}>
                          <Info size={14}/> Estável
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};

export default RegistrosPage;
