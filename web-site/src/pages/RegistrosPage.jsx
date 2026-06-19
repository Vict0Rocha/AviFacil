import { useState, useEffect, useCallback, useMemo } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import * as Zootecnia from '../utils/zootecnia';
import {
  RefreshCw, AlertTriangle, User, Database, Activity, TrendingUp, Calendar, Hash,
  ArrowUpRight, Users, HeartPulse, Scale, DollarSign
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const RegistrosPage = () => {
  const { user, isAdmin } = useAuth();
  const [avicultores, setAvicultores] = useState([]);
  const [selectedUid, setSelectedUid] = useState('');
  const [processedData, setProcessedData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadProducers = useCallback(async () => {
    if (!user) return;
    try {
      const snapshot = await getDocs(collection(db, 'avicultores'));
      const list = snapshot.docs.map(doc => ({
        ...doc.data(),
        uid_oficial: String(doc.id),
        nome_exibicao: doc.data().nome || doc.data().email || `ID: ${doc.id}`
      }));
      setAvicultores(list);
      if (list.length > 0) {
        const realUID = list.find(a => a.uid_oficial.length > 15);
        setSelectedUid(realUID ? realUID.uid_oficial : list[0].uid_oficial);
      }
    } catch (err) {
      setError("Erro ao carregar lista de produtores.");
    }
  }, [user]);

  useEffect(() => {
    if (isAdmin) loadProducers();
    else if (user) setSelectedUid(String(user.uid));
  }, [isAdmin, user, loadProducers]);

  const fetchAnalytics = useCallback(async (uid) => {
    if (!uid) return;
    setLoading(true);
    setProcessedData([]);
    try {
      const path = `avicultores/${uid}/lotes`;
      const lotesSnap = await getDocs(collection(db, path));
      if (lotesSnap.empty) {
        setLoading(false);
        return;
      }

      const results = await Promise.all(lotesSnap.docs.map(async (loteDoc) => {
        try {
          const loteData = { ...loteDoc.data(), id: loteDoc.id };
          const regsSnap = await getDocs(collection(db, `${path}/${loteDoc.id}/registros`));
          const registros = regsSnap.docs.map(d => d.data());

          // Executa cálculos usando a biblioteca sincronizada com o Mobile
          return {
            ...loteData,
            vivas: Zootecnia.calcularAvesVivas(loteData, registros),
            mortas: Zootecnia.calcularTotalMortas(registros),
            viabilidade: Zootecnia.calcularViabilidade(loteData, registros),
            ca: Zootecnia.calcularConversaoAlimentar(loteData, registros),
            gpd: Zootecnia.calcularGanhoMedioPeso(loteData, registros),
            mortalidade: Zootecnia.calcularMortalidade(loteData, registros),
            pesoAtual: Zootecnia.calcularPesoMedioAtual(registros),
            fatorProducao: Zootecnia.calcularFatorProducao(loteData, registros),
            idade: Zootecnia.calcularIdadeDias(loteData),
            consumoTotal: Zootecnia.calcularTotalConsumoRacao(registros),
            custoTotal: Zootecnia.calcularCustoTotalRacao(registros)
          };
        } catch (e) { return null; }
      }));

      setProcessedData(results.filter(r => r !== null).sort((a, b) => a.status === 'ATIVO' ? -1 : 1));
    } catch (err) {
      setError("Falha ao processar dados de performance.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (selectedUid) fetchAnalytics(selectedUid);
  }, [selectedUid, fetchAnalytics]);

  const stats = useMemo(() => {
    if (processedData.length === 0) return null;
    const ativos = processedData.filter(l => l.status === 'ATIVO');
    return {
      totalVivas: ativos.reduce((acc, curr) => acc + curr.vivas, 0),
      mortalidadeMedia: processedData.reduce((acc, curr) => acc + curr.mortalidade, 0) / processedData.length,
      fatorProducaoMedio: processedData.reduce((acc, curr) => acc + curr.fatorProducao, 0) / processedData.length,
      pesoMedio: ativos.length > 0 ? ativos.reduce((acc, curr) => acc + curr.pesoAtual, 0) / ativos.length : 0
    };
  }, [processedData]);

  return (
    <div style={{ display: 'flex', height: '100vh', width: '100vw', overflow: 'hidden', background: '#F0F2F5' }}>
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>

        <header style={{
          padding: '12px 32px', background: '#fff', borderBottom: '1px solid #E2E8F0',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 10px rgba(0,0,0,0.03)', zIndex: 10
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={20} color="#008858" />
              <h1 style={{ color: '#0B3B75', fontSize: '20px', fontWeight: '800', margin: 0 }}>BI Administrativo</h1>
            </div>
            <p style={{ color: '#718096', fontSize: '11px', margin: 0, fontWeight: '600' }}>Sincronizado com App Mobile v1.0</p>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
            {isAdmin && (
              <div style={{ position: 'relative' }}>
                <select
                  value={selectedUid}
                  onChange={(e) => setSelectedUid(e.target.value)}
                  style={{
                    width: '350px', height: '42px', padding: '0 12px 0 40px', borderRadius: '8px',
                    border: '1.5px solid #E2E8F0', fontSize: '13px', fontWeight: '700', color: '#2D3748', background: '#F8FAFB'
                  }}
                >
                  {avicultores.map(avi => (
                    <option key={avi.uid_oficial} value={avi.uid_oficial}>{avi.nome_exibicao}</option>
                  ))}
                </select>
                <User size={16} style={{ position: 'absolute', left: '14px', top: '13px', color: '#0B3B75' }} />
              </div>
            )}
            <button onClick={() => fetchAnalytics(selectedUid)} className="btn-primary" disabled={loading} style={{ height: '42px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
              <span>Sincronizar</span>
            </button>
          </div>
        </header>

        <main style={{ flex: 1, overflowY: 'auto', padding: '24px 32px' }}>
          {stats && !loading && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '24px' }}>
              <SummaryCard icon={<Users color="#0B3B75"/>} bg="#EBF4FF" label="Aves no Campo" value={stats.totalVivas.toLocaleString()} border="#0B3B75" />
              <SummaryCard icon={<HeartPulse color="#E53E3E"/>} bg="#FFF5F5" label="Mortalidade Média" value={`${stats.mortalidadeMedia.toFixed(2)}%`} border="#E53E3E" />
              <SummaryCard icon={<TrendingUp color="#008858"/>} bg="#E6F4EF" label="Fator de Prod. Médio" value={stats.fatorProducaoMedio.toFixed(2)} border="#008858" />
              <SummaryCard icon={<Scale color="#3182CE"/>} bg="#EBF8FF" label="Peso Médio Ativos" value={`${Math.round(stats.pesoMedio)}g`} border="#3182CE" />
            </div>
          )}

          <div className="card-scientific" style={{ padding: 0, overflow: 'hidden', border: '1px solid #E2E8F0' }}>
            <div style={{ padding: '16px 24px', borderBottom: '1px solid #EDF2F7', display: 'flex', justifyContent: 'space-between', background: '#fff' }}>
              <h2 style={{ fontSize: '15px', color: '#0B3B75', margin: 0, fontWeight: '800' }}>Rastreabilidade de Lotes</h2>
            </div>

            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: '#F8FAFB' }}>
                    <th style={thStyle}>Lote / Linhagem</th>
                    <th style={thCenter}>Idade</th>
                    <th style={thCenter}>Inicial</th>
                    <th style={thCenter}>Vivas</th>
                    <th style={thCenter}>Mortas</th>
                    <th style={thCenter}>Mortal. %</th>
                    <th style={thCenter}>Viab. %</th>
                    <th style={thCenter}>Peso</th>
                    <th style={thCenter}>G.P.D.</th>
                    <th style={thCenter}>Consumo (kg)</th>
                    <th style={thCenter}>C.A.</th>
                    <th style={thCenter}>Custo Ração</th>
                    <th style={{ ...thCenter, background: '#E6F4EF', color: '#008858' }}>Fator Prod.</th>
                    <th style={thCenter}>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {processedData.map((lote) => (
                    <tr key={lote.id} style={{ borderBottom: '1px solid #EDF2F7', background: '#fff' }} className="table-row-hover">
                      <td style={tdStyle}>
                        <div style={{ fontWeight: '800', color: '#0B3B75' }}>Lote {lote.numeroLote}</div>
                        <div style={{ fontSize: '11px', color: '#718096' }}>{lote.linhagem || 'Cobb-500'}</div>
                      </td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.idade}d</td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.quantidadeAvesInicial}</td>
                      <td style={{ ...tdStyle, textAlign: 'center', color: '#0B3B75', fontWeight: '800' }}>{lote.vivas}</td>
                      <td style={{ ...tdStyle, textAlign: 'center', color: '#E53E3E' }}>{lote.mortas}</td>
                      <td style={{ ...tdStyle, textAlign: 'center', fontWeight: '700', color: lote.mortalidade > 5 ? '#E53E3E' : '#2D3748' }}>
                        {lote.mortalidade.toFixed(2)}%
                      </td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.viabilidade.toFixed(2)}%</td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.pesoAtual}g</td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.gpd.toFixed(2)}</td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>{lote.consumoTotal.toLocaleString()} kg</td>
                      <td style={{ ...tdStyle, textAlign: 'center', color: '#3182CE', fontWeight: '800' }}>{lote.ca.toFixed(3)}</td>
                      <td style={{ ...tdStyle, textAlign: 'center', color: '#008858', fontWeight: '700' }}>
                        R$ {lote.custoTotal.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                      </td>
                      <td style={{ ...tdStyle, textAlign: 'center', background: '#F0FFF4', fontWeight: '900', color: '#008858' }}>
                        {lote.fatorProducao.toFixed(2)}
                      </td>
                      <td style={{ ...tdStyle, textAlign: 'center' }}>
                        <span className={`badge ${lote.status}`}>{lote.status}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

const SummaryCard = ({ icon, bg, label, value, border }) => (
  <div className="card-scientific" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px', borderLeft: `4px solid ${border}` }}>
    <div style={{ background: bg, padding: '12px', borderRadius: '12px' }}>{icon}</div>
    <div>
      <p style={{ margin: 0, fontSize: '11px', fontWeight: '700', color: '#718096', textTransform: 'uppercase' }}>{label}</p>
      <h3 style={{ margin: 0, color: border, fontSize: '22px' }}>{value}</h3>
    </div>
  </div>
);

const thStyle = { padding: '16px 15px', fontSize: '11px', fontWeight: '800', color: '#4A5568', textTransform: 'uppercase', borderBottom: '2px solid #E2E8F0' };
const thCenter = { ...thStyle, textAlign: 'center' };
const tdStyle = { padding: '14px 15px', fontSize: '13px', color: '#2D3748' };

export default RegistrosPage;
