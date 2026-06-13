import { useState, useEffect, useCallback } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import RecommendationPanel from '../components/RecommendationPanel';
import { RefreshCw, AlertCircle, BarChart3 } from 'lucide-react';
import * as Zootecnia from '../utils/zootecnia';
import { useAuth } from '../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const DashboardPage = () => {
  const { user, isAdmin } = useAuth();
  const [data, setData] = useState({ estatisticas: {}, lotes: [], produtores: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [insight, setInsight] = useState("Iniciando análise técnica...");

  const fetchDashboardData = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      let geralAlojadas = 0;
      let geralMortas = 0;
      let geralVivas = 0;
      let somaCA = 0;
      let lotesAtivosCount = 0;
      let todosLotes = [];
      let resumoProdutores = [];

      if (isAdmin) {
        const avicultoresSnapshot = await getDocs(collection(db, 'avicultores'));

        for (const aviDoc of avicultoresSnapshot.docs) {
          const uid = aviDoc.id;
          const aviData = aviDoc.data();
          const lotesRef = collection(db, `avicultores/${uid}/lotes`);
          const lotesSnap = await getDocs(lotesRef);

          let pAlojadas = 0;
          let pMortas = 0;
          let pSomaCA = 0;
          let pLotesAtivos = 0;

          for (const loteDoc of lotesSnap.docs) {
            const loteData = loteDoc.data();
            const regsRef = collection(db, `avicultores/${uid}/lotes/${loteDoc.id}/registros`);
            const regsSnap = await getDocs(regsRef);
            const registros = regsSnap.docs.map(d => d.data());

            const vivas = Zootecnia.calcularAvesVivas(loteData, registros);
            const mortas = Zootecnia.calcularTotalMortas(registros);
            const ca = Zootecnia.calcularConversaoAlimentar(loteData, registros);

            geralAlojadas += (Number(loteData.quantidadeAvesInicial) || 0);
            geralMortas += mortas;
            geralVivas += vivas;

            pAlojadas += (Number(loteData.quantidadeAvesInicial) || 0);
            pMortas += mortas;

            if (loteData.status === "ATIVO") {
              somaCA += ca;
              lotesAtivosCount++;
              pSomaCA += ca;
              pLotesAtivos++;
            }

            todosLotes.push({
              id: loteDoc.id,
              produtor: aviData.nome || "Produtor",
              identificacao: loteData.numeroLote || "Lote",
              avesAtuais: vivas,
              mortalidadeVal: Zootecnia.calcularMortalidade(loteData, registros),
              caVal: ca,
              status: loteData.status
            });
          }

          resumoProdutores.push({
            id: uid,
            nome: aviData.nome || "---",
            propriedade: aviData.nomePropriedade || "---",
            totalAlojadas: pAlojadas,
            avesVivas: pAlojadas - pMortas,
            mortalidade: pAlojadas > 0 ? ((pMortas / pAlojadas) * 100).toFixed(2) : "0.00",
            mortalidadeNum: pAlojadas > 0 ? (pMortas / pAlojadas) * 100 : 0,
            caMedia: pLotesAtivos > 0 ? (pSomaCA / pLotesAtivos).toFixed(3) : "---",
            viabilidade: pAlojadas > 0 ? (100 - (pMortas / pAlojadas) * 100).toFixed(2) + "%" : "100%"
          });
        }
      } else {
        // MODO PRODUTOR
        const lotesRef = collection(db, `avicultores/${user.uid}/lotes`);
        const snapshot = await getDocs(lotesRef);
        // ... (lógica simplificada para produtor único se necessário, mas o foco aqui é o Admin)
      }

      setData({
        estatisticas: {
          mortalidade: (geralAlojadas > 0 ? (geralMortas / geralAlojadas) * 100 : 0).toFixed(2) + "%",
          viabilidade: (geralAlojadas > 0 ? (100 - (geralMortas / geralAlojadas) * 100) : 100).toFixed(2) + "%",
          avesVivas: geralVivas,
          caMedia: (lotesAtivosCount > 0 ? somaCA / lotesAtivosCount : 0).toFixed(3)
        },
        lotes: todosLotes,
        produtores: resumoProdutores
      });
      setInsight(isAdmin ? "Monitoramento Administrativo Ativado. Analisando todos os produtores." : "Performance técnica atualizada.");
      setError(null);
    } catch (err) {
      console.error("Erro no Dashboard:", err);
      setError("Não foi possível carregar os dados técnicos.");
    } finally {
      setLoading(false);
    }
  }, [user, isAdmin]);

  useEffect(() => {
    if (user) fetchDashboardData();
  }, [user, fetchDashboardData]);

  if (loading) return (
    <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', background: '#F8FAFB' }}>
      <RefreshCw className="animate-spin" size={48} color="#008858" />
    </div>
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <h1 className="page-title">{isAdmin ? "Painel de Controle Bi & Analytics" : "Meu Dashboard"}</h1>

        {error && (
          <div style={{ padding: '16px', background: '#FED7D7', color: '#C53030', borderRadius: '12px', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <AlertCircle size={20} /> {error}
          </div>
        )}

        <div className="indicators-grid">
          <IndicatorCard title="Mortalidade Global" value={data?.estatisticas?.mortalidade || "---"} valueColor="#D64545" />
          <IndicatorCard title="Viabilidade Média" value={data?.estatisticas?.viabilidade || "---"} valueColor="#2D8A4E" />
          <IndicatorCard title="Total Aves Vivas" value={data?.estatisticas?.avesVivas || "0"} valueColor="#0B3B75" />
          <IndicatorCard title="C.A. Média Global" value={data?.estatisticas?.caMedia || "---"} valueColor="#3182CE" />
        </div>

        <div className="dashboard-charts">
          <div className="card-scientific">
            <h4 className="card-header"><BarChart3 size={18} /> Ranking de Mortalidade por Produtor (%)</h4>
            <div style={{ height: 300, minWidth: '100%' }}>
              {data.produtores.length > 0 ? (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={data.produtores}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                    <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{fontSize: 12}} />
                    <YAxis axisLine={false} tickLine={false} />
                    <Tooltip />
                    <Bar dataKey="mortalidadeNum" name="Mortalidade %">
                      {data.produtores.map((e, i) => (
                        <Cell key={i} fill={Number(e.mortalidadeNum) > 5 ? '#E53E3E' : '#008858'} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                  Aguardando dados analíticos...
                </div>
              )}
            </div>
          </div>
          <RecommendationPanel text={insight} />
        </div>


        {isAdmin && (
          <div className="card-scientific" style={{ marginTop: '24px' }}>
            <h3 className="card-header">Performance Geral por Produtor</h3>
            <div className="table-responsive">
              <table className="info-table">
                <thead>
                  <tr>
                    <th>Produtor</th>
                    <th>Propriedade</th>
                    <th>Alojadas</th>
                    <th>Vivas</th>
                    <th>Mort. %</th>
                    <th>Viab.</th>
                    <th>C.A. Média</th>
                  </tr>
                </thead>
                <tbody>
                  {data.produtores.length > 0 ? (
                    data.produtores.map(p => (
                      <tr key={p.id}>
                        <td><strong>{p.nome}</strong></td>
                        <td>{p.propriedade}</td>
                        <td>{p.totalAlojadas}</td>
                        <td>{p.avesVivas}</td>
                        <td style={{ color: p.mortalidadeNum > 5 ? '#E53E3E' : '#2D8A4E', fontWeight: '800' }}>
                          {p.mortalidade}%
                        </td>
                        <td>{p.viabilidade}</td>
                        <td style={{ fontWeight: '700', color: 'var(--primary-blue)' }}>{p.caMedia}</td>
                      </tr>
                    ))
                  ) : (
                    <tr><td colSpan="7" style={{ textAlign: 'center', padding: '20px' }}>Nenhum dado de produtor encontrado.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <div className="card-scientific" style={{ marginTop: '24px' }}>
          <h3 className="card-header">Monitoramento Individual de Lotes</h3>
          <div className="table-responsive">
            <table className="info-table">
              <thead>
                <tr>
                  {isAdmin && <th>Produtor</th>}
                  <th>Lote</th>
                  <th>Aves Atuais</th>
                  <th>Mortalidade</th>
                  <th>C.A.</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {data?.lotes?.length > 0 ? (
                  data.lotes.map(l => (
                    <tr key={l.id}>
                      {isAdmin && <td>{l.produtor}</td>}
                      <td><strong>{l.identificacao}</strong></td>
                      <td>{l.avesAtuais}</td>
                      <td style={{ color: l.mortalidadeVal > 5 ? '#E53E3E' : 'inherit' }}>{l.mortalidadeVal.toFixed(2)}%</td>
                      <td>{typeof l.caVal === 'number' ? l.caVal.toFixed(3) : "---"}</td>
                      <td><span className={`badge ${l.status}`}>{l.status}</span></td>
                    </tr>
                  ))
                ) : (
                  <tr><td colSpan={isAdmin ? 6 : 5} style={{ textAlign: 'center', padding: '20px' }}>Nenhum lote ativo encontrado.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};


export default DashboardPage;
