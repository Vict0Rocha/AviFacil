import { useState, useEffect } from 'react';
import { db, auth } from '../firebase';
import { collection, getDocs, doc, getDoc } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import { RefreshCw, AlertCircle } from 'lucide-react';
import * as Zootecnia from '../utils/zootecnia';

const DashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
      if (user) {
        fetchDashboardData(user.uid);
      } else {
        setLoading(false);
        setError("Usuário não autenticado. Redirecionando...");
      }
    });
    return () => unsubscribe();
  }, []);

  const fetchDashboardData = async (uid) => {
    // Garantia absoluta de que o UID é uma string
    const safeUid = uid ? String(uid) : null;

    if (!safeUid || safeUid === "undefined" || safeUid === "null") {
      console.error("[Firebase] UID inválido detectado:", uid);
      setError("Erro: Usuário não identificado corretamente.");
      setLoading(false);
      return;
    }

    setLoading(true);
    console.log("[Firebase] Iniciando busca para UID:", safeUid);

    try {
      // 1. Buscar dados do avicultor
      console.log("[Firebase] Buscando avicultor no Firestore...");
      const avicultorRef = doc(db, "avicultores", safeUid);
      const avicultorDoc = await getDoc(avicultorRef);
      const avicultorData = avicultorDoc.exists() ? avicultorDoc.data() : null;

      // 2. Buscar lotes - Usando template string para garantir o caminho
      console.log("[Firebase] Buscando lotes...");
      const lotesRef = collection(db, `avicultores/${safeUid}/lotes`);
      const lotesSnapshot = await getDocs(lotesRef);
      console.log(`[Firebase] Lotes encontrados: ${lotesSnapshot.size}`);

      let geralAlojadas = 0;
      let geralMortas = 0;
      let geralVivas = 0;
      let somaCA = 0;
      let lotesAtivosCount = 0;

      const lotesResultPromises = lotesSnapshot.docs.map(async (loteDoc) => {
        try {
          // Garante que o ID do lote é string
          const loteId = String(loteDoc.id);
          const loteData = loteDoc.data();
          const lote = { id: loteId, ...loteData };

          // 3. Buscar registros - Usando template string para o caminho completo
          const registrosPath = `avicultores/${safeUid}/lotes/${loteId}/registros`;
          const registrosRef = collection(db, registrosPath);
          const registrosSnapshot = await getDocs(registrosRef);
          const registros = registrosSnapshot.docs.map(rDoc => ({
            id: String(rDoc.id),
            ...rDoc.data()
          }));

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
            mortalidade: mortalidade.toFixed(2) + "%",
            viabilidade: viabilidade.toFixed(2) + "%",
            ca: ca > 0 ? ca.toFixed(2) : "---",
            pesoAtual: peso > 0 ? (peso / 1000).toFixed(3) : "---",
            status: lote.status || "ATIVO"
          };
        } catch (e) {
          console.error(`[Firebase] Erro no lote ${loteDoc.id}:`, e);
          return null;
        }
      });

      const lotesResultRaw = await Promise.all(lotesResultPromises);
      const lotesResult = lotesResultRaw.filter(l => l !== null);

      const mortalidadeGeral = geralAlojadas > 0 ? (geralMortas / geralAlojadas) * 100 : 0;
      const viabilidadeGeral = 100 - mortalidadeGeral;
      const caMediaGeral = lotesAtivosCount > 0 ? (somaCA / lotesAtivosCount) : 0;

      setData({
        nomeProdutor: avicultorData ? avicultorData.nome : "Produtor",
        propriedade: avicultorData ? avicultorData.nomePropriedade : "Fazenda",
        estatisticasGerais: {
          totalLotes: lotesResult.length,
          avesVivas: geralVivas,
          mortalidadeMedia: mortalidadeGeral.toFixed(2) + "%",
          viabilidadeMedia: viabilidadeGeral.toFixed(2) + "%",
          caMedia: caMediaGeral.toFixed(2)
        },
        lotes: lotesResult
      });

      setError(null);
    } catch (err) {
      console.error("Erro ao buscar dados:", err);
      // Exibe a mensagem técnica do erro para facilitar o diagnóstico
      setError(`Erro ao carregar dados: ${err.message || "Erro desconhecido"}`);
    } finally {
      setLoading(false);
    }
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
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>
              {data?.propriedade || "Propriedade em Análise"}
            </h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Produtor: {data?.nomeProdutor}</p>
          </div>
        </div>

        {error && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '16px', background: '#FED7D7', color: '#C53030', borderRadius: '12px', marginBottom: '24px', fontWeight: '600' }}>
            <AlertCircle size={20} />
            {error}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px', marginBottom: '32px' }}>
          <IndicatorCard
            title="Mortalidade Acumulada"
            value={data?.estatisticasGerais?.mortalidadeMedia || "0%"}
            valueColor="#D64545"
          />
          <IndicatorCard
            title="Viabilidade"
            value={data?.estatisticasGerais?.viabilidadeMedia || "0%"}
            valueColor="#2D8A4E"
          />
          <IndicatorCard
            title="Total de aves vivas"
            value={data?.estatisticasGerais?.avesVivas || 0}
            valueColor="#2D8A4E"
          />
          <IndicatorCard
            title="C.A. Média"
            value={data?.estatisticasGerais?.caMedia || 0}
          />
          <IndicatorCard
            title="Lotes Ativos"
            value={data?.estatisticasGerais?.totalLotes || 0}
            valueColor="var(--primary-navy)"
          />
        </div>

        <div className="card-scientific">
          <h3 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800' }}>Análise Técnica Detalhada por Lote</h3>
          <div className="table-responsive">
            <table className="info-table" style={{ marginTop: 0 }}>
              <thead>
                <tr>
                  <th>Lote</th>
                  <th>Galpão</th>
                  <th>Aves Vivas</th>
                  <th>Mort. (%)</th>
                  <th>Viab. (%)</th>
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
                    <td style={{ color: '#D64545', fontWeight: '700' }}>{lote.mortalidade}</td>
                    <td style={{ color: '#2D8A4E', fontWeight: '700' }}>{lote.viabilidade}</td>
                    <td>{lote.ca}</td>
                    <td>{lote.pesoAtual}</td>
                    <td>
                      <span style={{
                        padding: '4px 12px',
                        borderRadius: '20px',
                        fontSize: '11px',
                        fontWeight: '800',
                        background: lote.status === 'ATIVO' ? '#C6F6D5' : '#EDF2F7',
                        color: lote.status === 'ATIVO' ? '#22543D' : '#4A5568',
                        textTransform: 'uppercase'
                      }}>
                        {lote.status}
                      </span>
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
