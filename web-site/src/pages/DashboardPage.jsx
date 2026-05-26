import { useState, useEffect } from 'react';
import { functions } from '../firebase';
import { httpsCallable } from 'firebase/functions';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import { RefreshCw } from 'lucide-react';

const DashboardPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const getDashboard = httpsCallable(functions, 'getProdutorDashboard');
      const result = await getDashboard();
      setData(result.data);
      setError(null);
    } catch (err) {
      console.error("Erro ao buscar dados:", err);
      setError("Não foi possível carregar os dados do banco de dados.");
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
              {data?.propriedade || "Carregando Propriedade..."}
            </h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Produtor: {data?.nomeProdutor}</p>
          </div>

        </div>

        {error && (
          <div style={{ padding: '16px', background: '#FED7D7', color: '#C53030', borderRadius: '12px', marginBottom: '24px', fontWeight: '600' }}>
            {error}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '24px', marginBottom: '32px' }}>
          <IndicatorCard
            title="Mortalidade Média"
            value={data?.estatisticasGerais?.mortalidadeMedia || "0%"}
            valueColor="#D64545"
          />
          <IndicatorCard
            title="Total de aves vivas"
            value={data?.estatisticasGerais?.avesEmCampo || 0}
            valueColor="#2D8A4E"
          />
          <IndicatorCard
            title="Lotes Ativos"
            value={data?.estatisticasGerais?.totalLotes || 0}
          />
        </div>

        <div className="card-scientific">
          <h3 style={{ marginBottom: '20px', color: 'var(--primary-navy)', fontWeight: '800' }}>Desempenho por Lote</h3>
          <div className="table-responsive">
            <table className="info-table" style={{ marginTop: 0 }}>
              <thead>
                <tr>
                  <th>Lote</th>
                  <th>Galpão</th>
                  <th>Aves Atuais</th>
                  <th>Mortalidade</th>
                  <th>Peso Médio</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {data?.lotes?.map((lote) => (
                  <tr key={lote.id}>
                    <td><strong>{lote.identificacao}</strong></td>
                    <td>{lote.galpao}</td>
                    <td>{lote.avesAtuais}</td>
                    <td style={{ color: lote.alerta === 'Alerta' ? '#E53E3E' : 'inherit', fontWeight: '600' }}>
                      {lote.mortalidadePercentual}
                    </td>
                    <td>{lote.pesoAtual} kg</td>
                    <td>
                      <span style={{
                        padding: '4px 12px',
                        borderRadius: '20px',
                        fontSize: '12px',
                        fontWeight: '700',
                        background: lote.status === 'ATIVO' ? '#C6F6D5' : '#EDF2F7',
                        color: lote.status === 'ATIVO' ? '#22543D' : '#4A5568'
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
