import { useState, useEffect, useMemo } from 'react';
import { db } from '../firebase';
import { collection, getDocs, query, orderBy } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import * as Zootecnia from '../utils/zootecnia';
import {
  RefreshCw, Search, Info, TrendingUp, Calendar, Hash, Activity, Download
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area
} from 'recharts';

const RegistrosPage = () => {
  const { user, isAdmin } = useAuth();
  const [avicultores, setAvicultores] = useState([]);
  const [selectedAvicultorId, setSelectedAvicultorId] = useState('');
  const [lotes, setLotes] = useState([]);
  const [selectedLoteId, setSelectedLoteId] = useState('');
  const [registros, setRegistros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const selectedLoteData = useMemo(() =>
    lotes.find(l => l.id === selectedLoteId),
  [lotes, selectedLoteId]);

  // Estatísticas calculadas em tempo real para o lote selecionado
  const loteStats = useMemo(() => {
    if (!selectedLoteData || registros.length === 0) return null;
    return {
      mortalidade: Zootecnia.calcularMortalidade(selectedLoteData, registros).toFixed(2) + "%",
      viabilidade: Zootecnia.calcularViabilidade(selectedLoteData, registros).toFixed(2) + "%",
      ca: Zootecnia.calcularConversaoAlimentar(selectedLoteData, registros).toFixed(3),
      gpd: Zootecnia.calcularGanhoMedioPeso(selectedLoteData, registros).toFixed(2) + "g",
      vivas: Zootecnia.calcularAvesVivas(selectedLoteData, registros)
    };
  }, [selectedLoteData, registros]);

  const chartData = useMemo(() => {
    return [...registros].reverse().map(reg => ({
      dia: `Dia ${reg.idadeLote}`,
      peso: reg.pesoAtualMedio || 0,
      consumo: reg.consumoRacaoPeriodo || 0,
    }));
  }, [registros]);

  // Busca lista de avicultores se for ADMIN
  useEffect(() => {
    const fetchAvicultores = async () => {
      if (isAdmin) {
        try {
          const snapshot = await getDocs(collection(db, 'avicultores'));
          const list = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
          setAvicultores(list);
          if (list.length > 0) {
            setSelectedAvicultorId(list[0].id);
          }
        } catch (err) {
          console.error("Erro ao buscar avicultores", err);
        }
      } else if (user) {
        setSelectedAvicultorId(user.uid);
      }
    };
    fetchAvicultores();
  }, [isAdmin, user]);

  // Busca lotes sempre que o avicultor selecionado mudar
  useEffect(() => {
    if (selectedAvicultorId) {
      fetchLotes(selectedAvicultorId);
    }
  }, [selectedAvicultorId]);

  const fetchLotes = async (uid) => {
    setLoading(true);
    try {
      const lotesRef = collection(db, `avicultores/${uid}/lotes`);
      const snapshot = await getDocs(lotesRef);
      const lotesList = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setLotes(lotesList);
      if (lotesList.length > 0) {
        setSelectedLoteId(lotesList[0].id);
        fetchRegistros(uid, lotesList[0].id);
      } else {
        setSelectedLoteId('');
        setRegistros([]);
      }
    } catch (err) {
      setError("Erro ao carregar lotes.");
    } finally {
      setLoading(false);
    }
  };

  const fetchRegistros = async (uid, loteId) => {
    try {
      const regsRef = collection(db, `avicultores/${uid}/lotes/${loteId}/registros`);
      const q = query(regsRef, orderBy("dataRegistro", "desc"));
      const snapshot = await getDocs(q);
      setRegistros(snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })));
    } catch (err) {
      setError("Erro ao carregar registros.");
    }
  };

  const formatDate = (dateVal) => {
    if (!dateVal) return "---";
    const date = dateVal.toDate ? dateVal.toDate() : new Date(dateVal);
    return date.toLocaleDateString('pt-BR');
  };

  if (loading && !selectedAvicultorId) return (
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
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>Rastreabilidade Analítica</h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Análise profunda por produtor e lote individual</p>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            {isAdmin && (
              <div style={{ position: 'relative' }}>
                <span style={{ fontSize: '10px', fontWeight: '800', position: 'absolute', top: '-18px', left: '0', color: 'var(--primary-green)' }}>SELECIONAR PRODUTOR</span>
                <select
                  value={selectedAvicultorId}
                  onChange={(e) => setSelectedAvicultorId(e.target.value)}
                  className="input-field-login"
                  style={{ width: '220px', padding: '8px 8px 8px 35px', height: '40px', fontSize: '13px', margin: 0 }}
                >
                  {avicultores.map(avi => (
                    <option key={avi.id} value={avi.id}>{avi.nome || "Produtor"}</option>
                  ))}
                </select>
                <Search size={16} style={{ position: 'absolute', left: '10px', top: '12px', color: 'var(--primary-navy)' }} />
              </div>
            )}

            <div style={{ position: 'relative' }}>
              <span style={{ fontSize: '10px', fontWeight: '800', position: 'absolute', top: '-18px', left: '0', color: 'var(--primary-blue)' }}>SELECIONAR LOTE</span>
              <select
                value={selectedLoteId}
                onChange={(e) => {
                  setSelectedLoteId(e.target.value);
                  fetchRegistros(selectedAvicultorId, e.target.value);
                }}
                className="input-field-login"
                style={{ width: '220px', padding: '8px 8px 8px 35px', height: '40px', fontSize: '13px', margin: 0 }}
              >
                {lotes.length === 0 && <option value="">Nenhum lote encontrado</option>}
                {lotes.map(lote => <option key={lote.id} value={lote.id}>Lote {lote.numeroLote || lote.id}</option>)}
              </select>
              <Search size={16} style={{ position: 'absolute', left: '10px', top: '12px', color: 'var(--primary-blue)' }} />
            </div>
          </div>
        </div>

        {loteStats && (
          <div className="indicators-grid" style={{ marginBottom: '32px' }}>
            <IndicatorCard title="Mortalidade Lote" value={loteStats.mortalidade} valueColor="#D64545" />
            <IndicatorCard title="C.A. Atual" value={loteStats.ca} valueColor="#3182CE" />
            <IndicatorCard title="G.P.D. (Médio)" value={loteStats.gpd} valueColor="#2D8A4E" />
            <IndicatorCard title="Aves Vivas" value={loteStats.vivas} valueColor="#0B3B75" />
          </div>
        )}

        {selectedLoteData && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px', marginBottom: '32px' }}>
            <div className="card-scientific" style={{ gridColumn: 'span 2', minHeight: '380px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                <h4 style={{ color: 'var(--primary-navy)', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <TrendingUp size={18} color="var(--primary-green)"/> Curva de Crescimento (Peso em Gramas)
                </h4>
              </div>
              <div style={{ width: '100%', height: '300px' }}>
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

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="card-scientific" style={{ padding: '24px' }}>
                <h4 style={{ fontSize: '15px', marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800', borderBottom: '1px solid var(--border-color)', paddingBottom: '10px' }}>Ficha Técnica do Lote</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <Calendar size={18} color="var(--text-muted)" />
                    <div>
                      <p style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: '700' }}>DATA DE ALOJAMENTO</p>
                      <p style={{ fontWeight: '700' }}>{formatDate(selectedLoteData.dataInicio)}</p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <Hash size={18} color="var(--text-muted)" />
                    <div>
                      <p style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: '700' }}>QUANTIDADE INICIAL</p>
                      <p style={{ fontWeight: '700' }}>{selectedLoteData.quantidadeAvesInicial} aves</p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <Activity size={18} color="var(--text-muted)" />
                    <div>
                      <p style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: '700' }}>LINHAGEM / GALPÃO</p>
                      <p style={{ fontWeight: '700', color: 'var(--primary-green)' }}>{selectedLoteData.linhagem || "Cobb"} - {selectedLoteData.galpao}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="card-scientific">
          <h3 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800' }}>Histórico Detalhado de Registros</h3>
          <div className="table-responsive">
            <table className="info-table">
              <thead>
                <tr>
                  <th>Data do Registro</th>
                  <th>Idade</th>
                  <th>Mortes</th>
                  <th>Consumo (kg)</th>
                  <th>Peso Médio (g)</th>
                  <th>Status Ambiência</th>
                </tr>
              </thead>
              <tbody>
                {registros.length === 0 ? (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>Nenhum dado registrado para este lote.</td></tr>
                ) : (
                  registros.map((reg) => (
                    <tr key={reg.id}>
                      <td><strong>{formatDate(reg.dataRegistro)}</strong></td>
                      <td><span style={{ background: '#E6F4EF', color: '#008858', padding: '4px 10px', borderRadius: '6px', fontWeight: '800', fontSize: '12px' }}>{reg.idadeLote} dias</span></td>
                      <td style={{ color: (reg.avesMortasPeriodo > 5) ? '#E53E3E' : 'inherit', fontWeight: '800' }}>{reg.avesMortasPeriodo || 0}</td>
                      <td>{reg.consumoRacaoPeriodo || 0} kg</td>
                      <td><strong>{reg.pesoAtualMedio || 0} g</strong></td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#2D8A4E', fontWeight: '700', fontSize: '12px' }}>
                          <div style={{ width: '8px', height: '8px', background: '#2D8A4E', borderRadius: '50%' }}></div>
                          Conforme
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
