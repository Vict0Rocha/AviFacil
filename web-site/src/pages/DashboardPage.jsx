import { useState, useEffect, useCallback, useMemo } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import RecommendationPanel from '../components/RecommendationPanel';
import { RefreshCw, AlertCircle, TrendingUp, BarChart3 } from 'lucide-react';
import * as Zootecnia from '../utils/zootecnia';
import { useAuth } from '../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const DashboardPage = () => {
  const { user, userData } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [insight, setInsight] = useState("Analisando performance dos lotes...");

  const fetchDashboardData = useCallback(async (uid) => {
    setLoading(true);
    try {
      const lotesRef = collection(db, `avicultores/${uid}/lotes`);
      const lotesSnapshot = await getDocs(lotesRef);

      let geralAlojadas = 0;
      let geralMortas = 0;
      let geralVivas = 0;
      let somaCA = 0;
      let lotesAtivosCount = 0;

      const lotesResultPromises = lotesSnapshot.docs.map(async (loteDoc) => {
        try {
          const loteId = loteDoc.id;
          const loteData = loteDoc.data();
          const lote = { id: loteId, ...loteData };

          const registrosRef = collection(db, `avicultores/${uid}/lotes/${loteId}/registros`);
          const registrosSnapshot = await getDocs(registrosRef);
          const registros = registrosSnapshot.docs.map(rDoc => ({ id: rDoc.id, ...rDoc.data() }));

          const vivas = Zootecnia.calcularAvesVivas(lote, registros);
          const mortas = Zootecnia.calcularTotalMortas(registros);
          const mortalidade = Zootecnia.calcularMortalidade(lote, registros);
          const viabilidade = Zootecnia.calcularViabilidade(lote, registros);
          const ca = Zootecnia.calcularConversaoAlimentar(lote, registros);
          const peso = Zootecnia.calcularPesoMedioAtual(registros);

          geralAlojadas += (Number(lote.quantidadeAvesInicial) || 0);
          geralMortas += mortas;
          geralVivas += vivas;

          if (lote.status === "ATIVO") {
            somaCA += ca;
            lotesAtivosCount++;
          }

          return {
            id: lote.id,
            identificacao: lote.numeroLote || "Lote",
            galpao: lote.galpao || "---",
            avesAtuais: vivas,
            mortalidadeVal: mortalidade,
            mortalidade: mortalidade.toFixed(2) + "%",
            viabilidade: viabilidade.toFixed(2) + "%",
            caVal: ca,
            ca: ca > 0 ? ca.toFixed(2) : "---",
            pesoAtual: peso > 0 ? (peso / 1000).toFixed(3) : "---",
            status: lote.status || "ATIVO"
          };
        } catch (e) {
          return null;
        }
      });

      const lotesResultRaw = await Promise.all(lotesResultPromises);
      const lotesResult = lotesResultRaw.filter(l => l !== null);

      const mortalidadeGeral = geralAlojadas > 0 ? (geralMortas / geralAlojadas) * 100 : 0;
      const viabilidadeGeral = 100 - mortalidadeGeral;
      const caMediaGeral = lotesAtivosCount > 0 ? (somaCA / lotesAtivosCount) : 0;

      setData({
        estatisticasGerais: {
          totalLotes: lotesResult.length,
          avesVivas: geralVivas,
          mortalidadeMedia: mortalidadeGeral.toFixed(2) + "%",
          viabilidadeMedia: viabilidadeGeral.toFixed(2) + "%",
          caMedia: caMediaGeral.toFixed(2)
        },
        lotes: lotesResult
      });

      if (mortalidadeGeral > 5) setInsight("Alerta crítico: A mortalidade geral está acima de 5%. Recomenda-se auditoria sanitária imediata.");
      else if (caMediaGeral > 1.75) setInsight("Atenção: A Conversão Alimentar média pode ser otimizada. Verifique o manejo de cortinas e temperatura.");
      else setInsight("Excelente: Todos os indicadores de produtividade estão dentro das metas de excelência.");

      setError(null);
    } catch (err) {
      setError("Falha na sincronização com a nuvem.");
    } finally {
      setLoading(false);
    }
  }, [userData]);

  useEffect(() => {
    if (user) fetchDashboardData(user.uid);
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
        <div className="top-bar-scientific">
          <div>
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>Dashboard Decisório</h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Visão analítica de performance zootécnica</p>
          </div>
          <div style={{ background: 'white', padding: '8px 16px', borderRadius: '10px', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#48BB78' }}></div>
            <span style={{ fontSize: '13px', fontWeight: '700', color: 'var(--primary-navy)' }}>SISTEMA ONLINE</span>
          </div>
        </div>

        {error && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '16px', background: '#FED7D7', color: '#C53030', borderRadius: '12px', marginBottom: '24px' }}>
            <AlertCircle size={20} /> {error}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
          <IndicatorCard title="Mortalidade Média" value={data?.estatisticasGerais?.mortalidadeMedia} valueColor="#D64545" />
          <IndicatorCard title="Viabilidade" value={data?.estatisticasGerais?.viabilidadeMedia} valueColor="#2D8A4E" />
          <IndicatorCard title="Aves Totais" value={data?.estatisticasGerais?.avesVivas} valueColor="var(--primary-navy)" />
          <IndicatorCard title="C.A. Média" value={data?.estatisticasGerais?.caMedia} valueColor="var(--primary-blue)" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px', marginBottom: '32px' }}>
          <div className="card-scientific">
            <h4 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <BarChart3 size={18} /> Comparativo de Mortalidade por Lote (%)
            </h4>
            <div style={{ width: '100%', height: '250px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={data?.lotes}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                  <XAxis dataKey="identificacao" axisLine={false} tickLine={false} tick={{fontSize: 12}} />
                  <YAxis axisLine={false} tickLine={false} tick={{fontSize: 12}} />
                  <Tooltip cursor={{fill: '#F7FAFC'}} contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 10px rgba(0,0,0,0.1)'}} />
                  <Bar dataKey="mortalidadeVal" name="Mortalidade">
                    {data?.lotes.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.mortalidadeVal > 5 ? '#E53E3E' : '#3182CE'} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
          <RecommendationPanel text={insight} />
        </div>

        <div className="card-scientific">
          <h3 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800' }}>Monitoramento de Lotes Ativos</h3>
          <div className="table-responsive">
            <table className="info-table">
              <thead>
                <tr>
                  <th>Identificação</th>
                  <th>Galpão</th>
                  <th>Vivas</th>
                  <th>Mort. (%)</th>
                  <th>C.A.</th>
                  <th>Peso (kg)</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {data?.lotes?.map((lote) => (
                  <tr key={lote.id}>
                    <td><strong>{lote.identificacao}</strong></td>
                    <td>{lote.galpao}</td>
                    <td>{lote.avesAtuais}</td>
                    <td style={{ color: lote.mortalidadeVal > 5 ? '#E53E3E' : 'inherit', fontWeight: '700' }}>{lote.mortalidade}</td>
                    <td>{lote.ca}</td>
                    <td>{lote.pesoAtual}</td>
                    <td>
                      <span style={{
                        padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: '800',
                        background: lote.status === 'ATIVO' ? '#C6F6D5' : '#EDF2F7',
                        color: lote.status === 'ATIVO' ? '#22543D' : '#4A5568'
                      }}>{lote.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
