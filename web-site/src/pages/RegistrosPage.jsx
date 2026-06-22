import { useState, useEffect, useCallback, useMemo } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import * as Zootecnia from '../utils/zootecnia';
import {
  RefreshCw, AlertTriangle, User, Database, Activity, TrendingUp, Calendar, Hash,
  ArrowUpRight, Users, HeartPulse, Scale, DollarSign, Eye, X, Download, FileText, Table
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

const RegistrosPage = () => {
  const { user, isAdmin } = useAuth();
  const [avicultores, setAvicultores] = useState([]);
  const [selectedUid, setSelectedUid] = useState('');
  const [processedData, setProcessedData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedLote, setSelectedLote] = useState(null);
  const [showExportMenu, setShowExportMenu] = useState(false);

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

  const exportToPDF = () => {
    try {
      const doc = new jsPDF('l', 'mm', 'a4');
      const producer = avicultores.find(a => a.uid_oficial === selectedUid);
      const date = new Date().toLocaleDateString('pt-BR');

      doc.setFontSize(18);
      doc.setTextColor(11, 59, 117);
      doc.text(`AviFácil - Relatório de Rastreabilidade`, 14, 15);

      doc.setFontSize(12);
      doc.setTextColor(45, 55, 72);
      doc.text(`Produtor: ${producer?.nome_exibicao || '---'}`, 14, 22);
      doc.text(`Data: ${date}`, 14, 28);

      const tableData = (processedData || []).map(l => [
        `Lote ${l.numeroLote || '---'}`,
        l.idade || 0,
        l.quantidadeAvesInicial || 0,
        l.vivas || 0,
        l.mortas || 0,
        (l.mortalidade || 0).toFixed(2) + '%',
        (l.viabilidade || 0).toFixed(2) + '%',
        (l.pesoAtual || 0) + 'g',
        (l.gpd || 0).toFixed(2),
        (l.ca || 0).toFixed(3),
        (l.fatorProducao || 0).toFixed(2),
        l.status || '---'
      ]);

      autoTable(doc, {
        startY: 35,
        head: [['Lote', 'Idade', 'Inicial', 'Vivas', 'Mortas', 'Mort.%', 'Viab.%', 'Peso', 'GPD', 'C.A.', 'F.P.', 'Status']],
        body: tableData,
        theme: 'striped',
        headStyles: { fillColor: [11, 59, 117], fontSize: 9 },
        styles: { fontSize: 8 },
        columnStyles: {
          10: { fontStyle: 'bold', fillColor: [230, 244, 239] }
        }
      });

      const fileName = `Rastreabilidade_${(producer?.nome_exibicao || 'Export').replace(/\s/g, '_')}_${date.replace(/\//g, '-')}.pdf`;
      doc.save(fileName);
    } catch (err) {
      console.error("Erro ao exportar PDF:", err);
      alert("Erro ao gerar o PDF. Verifique se os dados estão carregados corretamente.");
    }
    setShowExportMenu(false);
  };

  const exportToExcel = () => {
    const producer = avicultores.find(a => a.uid_oficial === selectedUid);
    const worksheet = XLSX.utils.json_to_sheet(processedData.map(l => ({
      'Lote': l.numeroLote,
      'Linhagem': l.linhagem || 'Cobb-500',
      'Idade (dias)': l.idade,
      'Aves Iniciais': l.quantidadeAvesInicial,
      'Aves Vivas': l.vivas,
      'Aves Mortas': l.mortas,
      'Mortalidade %': l.mortalidade.toFixed(2),
      'Viabilidade %': l.viabilidade.toFixed(2),
      'Peso Médio (g)': l.pesoAtual,
      'GPD': l.gpd.toFixed(2),
      'Consumo (kg)': l.consumoTotal,
      'Conversão Alimentar': l.ca.toFixed(3),
      'Custo Ração (R$)': l.custoTotal,
      'Fator Produção': l.fatorProducao.toFixed(2),
      'Status': l.status
    })));

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Rastreabilidade");
    XLSX.writeFile(workbook, `Rastreabilidade_${producer?.nome_exibicao.replace(/\s/g, '_')}_${new Date().toISOString().split('T')[0]}.xlsx`);
    setShowExportMenu(false);
  };

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
            custoTotal: Zootecnia.calcularCustoTotalRacao(registros),
            registros: registros.sort((a, b) => Zootecnia.safeDate(b.dataRegistro) - Zootecnia.safeDate(a.dataRegistro))
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
    <div className="app-layout">
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, width: '100%' }}>

        <header className="top-bar-scientific" style={{
          padding: '12px 32px', background: '#fff', borderBottom: '1px solid #E2E8F0',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 10px rgba(0,0,0,0.03)', zIndex: 10, margin: 0
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={20} color="#008858" />
              <h1 style={{ color: '#0B3B75', fontSize: '20px', fontWeight: '800', margin: 0 }}>Consulta por produtor</h1>
            </div>
            <p className="hidden-mobile" style={{ color: '#718096', fontSize: '11px', margin: 0, fontWeight: '600' }}>Sincronizado com App Mobile v1.0</p>
          </div>

          <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap' }}>
            {isAdmin && (
              <div style={{ position: 'relative', width: '250px' }}>
                <select
                  value={selectedUid}
                  onChange={(e) => setSelectedUid(e.target.value)}
                  style={{
                    width: '100%', height: '42px', padding: '0 12px 0 40px', borderRadius: '8px',
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

            <div style={{ position: 'relative' }}>
              <button
                onClick={() => setShowExportMenu(!showExportMenu)}
                className="btn-secondary"
                style={{ height: '42px', display: 'flex', alignItems: 'center', gap: '8px', background: '#fff', border: '1px solid #E2E8F0', color: '#4A5568', padding: '0 15px', borderRadius: '8px', cursor: 'pointer' }}
              >
                <Download size={16} /> <span className="hidden-mobile">Exportar</span>
              </button>

              {showExportMenu && (
                <div style={{ position: 'absolute', top: '100%', right: 0, marginTop: '8px', background: '#fff', borderRadius: '8px', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', border: '1px solid #E2E8F0', zIndex: 100, width: '180px', overflow: 'hidden' }}>
                  <button onClick={exportToPDF} style={{ width: '100%', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: '10px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', fontSize: '13px', color: '#4A5568', transition: 'background 0.2s' }} onMouseEnter={e => e.target.style.background = '#F7FAFC'} onMouseLeave={e => e.target.style.background = 'none'}>
                    <FileText size={14} color="#E53E3E" /> PDF
                  </button>
                  <button onClick={exportToExcel} style={{ width: '100%', padding: '12px 16px', display: 'flex', alignItems: 'center', gap: '10px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', fontSize: '13px', color: '#4A5568', borderTop: '1px solid #F1F5F9', transition: 'background 0.2s' }} onMouseEnter={e => e.target.style.background = '#F7FAFC'} onMouseLeave={e => e.target.style.background = 'none'}>
                    <Table size={14} color="#2D8A4E" /> Excel (Tabela)
                  </button>
                </div>
              )}
            </div>

            <button onClick={() => fetchAnalytics(selectedUid)} className="btn-primary" disabled={loading} style={{ height: '42px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
              <span className="hidden-mobile">Sincronizar</span>
            </button>
          </div>
        </header>

        <main className="main-content" style={{ flex: 1, padding: '24px 32px' }}>
          {stats && !loading && (
            <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '24px' }}>
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

            <div className="table-responsive">
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
                    <th style={thCenter}>Ações</th>
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
                      <td style={{ ...tdStyle, textAlign: 'center' }}>
                        <button
                          onClick={() => setSelectedLote(lote)}
                          style={{
                            background: '#F1F5F9', border: 'none', borderRadius: '6px', padding: '6px',
                            cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
                            color: '#64748B', transition: 'all 0.2s'
                          }}
                          onMouseEnter={e => e.currentTarget.style.background = '#E2E8F0'}
                          onMouseLeave={e => e.currentTarget.style.background = '#F1F5F9'}
                          title="Ver Registros"
                        >
                          <Eye size={16} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </main>

        {/* Modal de Registros Detalhados */}
        {selectedLote && (
          <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 1000, padding: '20px'
          }}>
            <div style={{
              background: '#fff', borderRadius: '16px', width: '100%', maxWidth: '900px',
              maxHeight: '85vh', display: 'flex', flexDirection: 'column', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.25)'
            }}>
              <div style={{ padding: '20px 24px', borderBottom: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ margin: 0, color: '#0B3B75', fontSize: '18px', fontWeight: '800' }}>
                    Registros Diários - Lote {selectedLote.numeroLote}
                  </h3>
                  <p style={{ margin: 0, fontSize: '12px', color: '#718096' }}>{selectedLote.linhagem || 'Cobb-500'} • {selectedLote.quantidadeAvesInicial} aves iniciais</p>
                </div>
                <button onClick={() => setSelectedLote(null)} style={{ background: '#F7FAFC', border: 'none', borderRadius: '50%', padding: '8px', cursor: 'pointer' }}>
                  <X size={20} color="#4A5568" />
                </button>
              </div>

              <div style={{ flex: 1, overflowY: 'auto', padding: '0' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead style={{ position: 'sticky', top: 0, background: '#F8FAFB', zIndex: 1 }}>
                    <tr>
                      <th style={modalTh}>Data</th>
                      <th style={modalTh}>Idade</th>
                      <th style={modalTh}>Peso Médio</th>
                      <th style={modalTh}>Mortalidade</th>
                      <th style={modalTh}>Consumo Ração</th>
                      <th style={modalTh}>Preço Kg Insumo</th>
                      <th style={modalTh}>Tipo Insumo</th>
                      <th style={modalTh}>Observações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedLote.registros.length > 0 ? (
                      selectedLote.registros.map((reg, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid #EDF2F7' }}>
                          <td style={modalTd}>{Zootecnia.safeDate(reg.dataRegistro).toLocaleDateString('pt-BR')}</td>
                          <td style={modalTd}>{Zootecnia.calcularIdadeDias(selectedLote, reg.dataRegistro)} dias</td>
                          <td style={{ ...modalTd, fontWeight: '700', color: '#3182CE' }}>{reg.pesoAtualMedio}g</td>
                          <td style={{ ...modalTd, color: '#E53E3E', fontWeight: '700' }}>{reg.avesMortasPeriodo} aves</td>
                          <td style={{ ...modalTd, fontWeight: '600' }}>{reg.consumoRacaoPeriodo} kg</td>
                          <td style={modalTd}>R$ {Number(reg.precoKgInsumo || 0).toFixed(2)}</td>
                          <td style={modalTd}><span style={{ textTransform: 'capitalize' }}>{reg.tipoInsumo || '---'}</span></td>
                          <td style={{ ...modalTd, fontSize: '11px', color: '#718096', maxWidth: '200px', whiteSpace: 'normal' }}>
                            {reg.observacoes || '---'}
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr><td colSpan="8" style={{ padding: '40px', textAlign: 'center', color: '#718096' }}>Nenhum registro encontrado para este lote.</td></tr>
                    )}
                  </tbody>
                </table>
              </div>

              <div style={{ padding: '16px 24px', borderTop: '1px solid #E2E8F0', background: '#F8FAFB', borderRadius: '0 0 16px 16px', textAlign: 'right' }}>
                <button onClick={() => setSelectedLote(null)} className="btn-secondary" style={{ padding: '8px 20px' }}>Fechar Janela</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const modalTh = { padding: '14px 20px', fontSize: '11px', fontWeight: '800', color: '#718096', textTransform: 'uppercase', textAlign: 'left', borderBottom: '1px solid #E2E8F0' };
const modalTd = { padding: '12px 20px', fontSize: '13px', color: '#2D3748' };

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
