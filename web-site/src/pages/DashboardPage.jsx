import { useState, useEffect, useCallback } from 'react';
import { db } from '../firebase';
import { collection, getDocs } from 'firebase/firestore';
import Sidebar from '../components/Sidebar';
import IndicatorCard from '../components/IndicatorCard';
import { RefreshCw, AlertCircle, BarChart3, TrendingUp, ChevronRight, Download, FileText, Table } from 'lucide-react';
import * as Zootecnia from '../utils/zootecnia';
import { useAuth } from '../context/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

const DashboardPage = () => {
  const { user, isAdmin } = useAuth();
  const [data, setData] = useState({ estatisticas: {}, produtores: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showExportMenu, setShowExportMenu] = useState(false);

  const exportToPDF = () => {
    try {
      const doc = new jsPDF();
      const date = new Date().toLocaleDateString('pt-BR');

      doc.setFontSize(18);
      doc.setTextColor(11, 59, 117);
      doc.text('AviFácil - Relatório de Desempenho Global', 14, 20);

      doc.setFontSize(10);
      doc.setTextColor(100);
      doc.text(`Gerado em: ${date}`, 14, 28);

      const stats = data.estatisticas || {};

      autoTable(doc, {
        startY: 35,
        head: [['Mortalidade Global', 'Viabilidade', 'Aves no Campo', 'Lotes (Ativ/Enc)']],
        body: [[
          stats.mortalidade || '0%',
          stats.viabilidade || '100%',
          stats.avesVivas || '0',
          stats.totalLotes || '0 / 0'
        ]],
        theme: 'grid',
        headStyles: { fillColor: [11, 59, 117] }
      });

      const tableData = (data.produtores || []).map(p => [
        p.nome || '---',
        p.email || '---',
        (p.avesVivas || 0).toLocaleString('pt-BR'),
        (p.mortalidade || '0.00') + '%',
        p.viabilidade || '0%',
        p.lotesAtivos || 0
      ]);

      const finalY = doc.lastAutoTable ? doc.lastAutoTable.finalY : 70;

      autoTable(doc, {
        startY: finalY + 15,
        head: [['Produtor', 'Email', 'Aves Vivas', 'Mortalidade', 'Viabilidade', 'Lotes Ativos']],
        body: tableData,
        theme: 'striped',
        headStyles: { fillColor: [0, 136, 88] }
      });

      doc.save(`Relatorio_AviFacil_${date.replace(/\//g, '-')}.pdf`);
    } catch (err) {
      console.error("Erro detalhado PDF:", err);
      alert("Erro ao gerar PDF. Verifique o console para mais detalhes.");
    }
    setShowExportMenu(false);
  };

  const exportToExcel = () => {
    const excelData = [
      {
        'Produtor': 'RESUMO GLOBAL',
        'Email': '---',
        'Aves no Campo': data.estatisticas.avesVivas,
        'Mortalidade %': data.estatisticas.mortalidade,
        'Viabilidade %': data.estatisticas.viabilidade,
        'Lotes Ativos': data.estatisticas.totalLotes
      },
      ...data.produtores.map(p => ({
        'Produtor': p.nome,
        'Email': p.email,
        'Aves no Campo': p.avesVivas,
        'Mortalidade %': p.mortalidade,
        'Viabilidade %': p.viabilidade,
        'Lotes Ativos': p.lotesAtivos
      }))
    ];

    const worksheet = XLSX.utils.json_to_sheet(excelData);

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Performance");

    XLSX.writeFile(workbook, `Dados_AviFacil_${new Date().toISOString().split('T')[0]}.xlsx`);
    setShowExportMenu(false);
  };

  const fetchDashboardData = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      let geralAlojadas = 0;
      let geralMortas = 0;
      let geralVivas = 0;
      let totalLotesAtivos = 0;
      let totalLotesEncerrados = 0;
      let resumoProdutores = [];
      let totalSomaFP = 0;
      let totalCountFP = 0;

      if (isAdmin) {
        const avicultoresSnapshot = await getDocs(collection(db, 'avicultores'));

        for (const aviDoc of avicultoresSnapshot.docs) {
          const uid = String(aviDoc.id);
          if (uid.length < 5 && avicultoresSnapshot.size > 5) continue;

          const aviData = aviDoc.data();
          const lotesRef = collection(db, `avicultores/${uid}/lotes`);
          const lotesSnap = await getDocs(lotesRef);

          let pAlojadas = 0;
          let pMortas = 0;
          let pLotesAtivos = 0;
          let pLotesEncerrados = 0;
          let pSomaFPAtivo = 0;
          let pSomaFPEncerrado = 0;
          let pCountFPAtivo = 0;
          let pCountFPEncerrado = 0;

          for (const loteDoc of lotesSnap.docs) {
            const loteData = loteDoc.data();
            const regsRef = collection(db, `avicultores/${uid}/lotes/${loteDoc.id}/registros`);
            const regsSnap = await getDocs(regsRef);
            const registros = regsSnap.docs.map(d => d.data());

            const vivas = Zootecnia.calcularAvesVivas(loteData, registros);
            const mortas = Zootecnia.calcularTotalMortas(registros);
            const fp = Zootecnia.calcularFatorProducao(loteData, registros);

            geralAlojadas += (Number(loteData.quantidadeAvesInicial) || 0);
            geralMortas += mortas;

            if (fp > 0) {
              totalSomaFP += fp;
              totalCountFP++;
            }

            pAlojadas += (Number(loteData.quantidadeAvesInicial) || 0);
            pMortas += mortas;

            if (loteData.status === "ATIVO") {
              geralVivas += vivas;
              pLotesAtivos++;
              totalLotesAtivos++;
              if (fp > 0) {
                pSomaFPAtivo += fp;
                pCountFPAtivo++;
              }
            } else {
              pLotesEncerrados++;
              totalLotesEncerrados++;
              if (fp > 0) {
                pSomaFPEncerrado += fp;
                pCountFPEncerrado++;
              }
            }
          }

          if (pAlojadas > 0 || pLotesAtivos > 0 || pLotesEncerrados > 0) {
            resumoProdutores.push({
              id: uid,
              nome: aviData.nome || "Produtor Desconhecido",
              email: aviData.email || "---",
              mortalidadeNum: pAlojadas > 0 ? (pMortas / pAlojadas) * 100 : 0,
              mortalidade: pAlojadas > 0 ? ((pMortas / pAlojadas) * 100).toFixed(2) : "0.00",
              avesVivas: pAlojadas - pMortas,
              viabilidade: pAlojadas > 0 ? (100 - (pMortas / pAlojadas) * 100).toFixed(2) + "%" : "100%",
              lotesAtivos: pLotesAtivos,
              lotesEncerrados: pLotesEncerrados,
              fpMedioAtivo: pCountFPAtivo > 0 ? (pSomaFPAtivo / pCountFPAtivo) : 0,
              fpMedioEncerrado: pCountFPEncerrado > 0 ? (pSomaFPEncerrado / pCountFPEncerrado) : 0
            });
          }
        }
      }

      setData({
        estatisticas: {
          mortalidade: (geralAlojadas > 0 ? (geralMortas / geralAlojadas) * 100 : 0).toFixed(2) + "%",
          viabilidade: (geralAlojadas > 0 ? (100 - (geralMortas / geralAlojadas) * 100) : 100).toFixed(2) + "%",
          avesVivas: geralVivas.toLocaleString('pt-BR'),
          totalLotes: `${totalLotesAtivos} / ${totalLotesEncerrados}`,
          iepMedio: totalCountFP > 0 ? (totalSomaFP / totalCountFP).toFixed(2) : "0.00"
        },
        produtores: resumoProdutores.sort((a, b) => b.mortalidadeNum - a.mortalidadeNum)
      });

      setError(null);
    } catch (err) {
      console.error("Erro no Dashboard BI:", err);
      setError("Falha na consolidação dos indicadores globais.");
    } finally {
      setLoading(false);
    }
  }, [user, isAdmin]);

  useEffect(() => {
    if (user) fetchDashboardData();
  }, [user, fetchDashboardData]);

  return (
    <div className="app-layout">
      <Sidebar />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, width: '100%' }}>

        {/* Top Bar Fixa */}
        <header className="top-bar-scientific" style={{ padding: '20px 32px', background: '#fff', borderBottom: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', zIndex: 10, margin: 0 }}>
          <div>
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '22px', fontWeight: '800', margin: 0 }}>
              {isAdmin ? "Dashboard Geral" : "Meu Dashboard"}
            </h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)', fontSize: '12px' }}>
              <ChevronRight size={14} /> <span>Indicadores de Desempenho Global</span>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
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
                    <Table size={14} color="#2D8A4E" /> Excel (XLSX)
                  </button>
                </div>
              )}
            </div>

            <button onClick={fetchDashboardData} className="btn-primary" style={{ height: '42px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <RefreshCw size={16} className={loading ? "animate-spin" : ""} /> <span className="hidden-mobile">Sincronizar Dados</span>
            </button>
          </div>
        </header>

        {/* Conteúdo com Scroll */}
        <main className="main-content" style={{ flex: 1, padding: '32px' }}>

          {loading ? (
            <div style={{ display: 'flex', height: '60vh', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: '20px' }}>
              <RefreshCw className="animate-spin" size={48} color="var(--primary-green)" />
              <p style={{ fontWeight: '700', color: 'var(--primary-navy)' }}>Consolidando dados do Firestore...</p>
            </div>
          ) : (
            <>
              {error && (
                <div style={{ padding: '16px', background: '#FED7D7', color: '#C53030', borderRadius: '12px', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '10px', borderLeft: '4px solid #E53E3E' }}>
                  <AlertCircle size={20} /> {error}
                </div>
              )}

              {/* Grid de Indicadores */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                <IndicatorCard title="Mortalidade Global" value={data.estatisticas.mortalidade} valueColor="#D64545" small />
                <IndicatorCard title="Viabilidade" value={data.estatisticas.viabilidade} valueColor="#2D8A4E" small />
                <IndicatorCard title="Aves no Campo" value={data.estatisticas.avesVivas} valueColor="#0B3B75" small />
                <IndicatorCard title="Lotes (Ativos/Enc)" value={data.estatisticas.totalLotes} valueColor="#3182CE" small />
              </div>

              {/* Ranking Mortalidade (Sempre no topo, largura total) */}
              <div style={{ marginBottom: '24px' }}>
                <div className="card-scientific">
                  <h4 className="card-header" style={{ fontSize: '15px', border: 'none', marginBottom: '20px', fontWeight: '800' }}>
                    <BarChart3 size={18} color="var(--primary-green)" style={{ marginRight: '8px' }} />
                    Mortalidade por Produtor (%)
                  </h4>
                  <div style={{ height: 260, width: '100%' }}>
                    {data.produtores.length > 0 ? (
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data.produtores} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                          <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: '700', fill: '#4A5568'}} />
                          <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10}} />
                          <Tooltip
                            cursor={{fill: '#F7FAFC'}}
                            contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', padding: '12px' }}
                          />
                          <Bar dataKey="mortalidadeNum" name="Mortalidade %" radius={[4, 4, 0, 0]} barSize={35}>
                            {data.produtores.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={entry.mortalidadeNum > 5 ? '#E53E3E' : '#008858'} />
                            ))}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    ) : (
                      <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '14px', border: '2px dashed #E2E8F0', borderRadius: '12px' }}>
                        Sem dados para o ranking.
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Grid de Fator de Produção (Abaixo da mortalidade) */}
              <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', marginBottom: '24px' }}>
                {/* Gráfico Fator Produção Ativos */}
                <div className="card-scientific">
                  <h4 className="card-header" style={{ fontSize: '15px', border: 'none', marginBottom: '20px', fontWeight: '800' }}>
                    <TrendingUp size={18} color="#008858" style={{ marginRight: '8px' }} />
                    Méd. Fator Produção (Lotes Ativos)
                  </h4>
                  <div style={{ height: 260, width: '100%' }}>
                    {data.produtores.length > 0 ? (
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data.produtores} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                          <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: '700', fill: '#4A5568'}} />
                          <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10}} />
                          <Tooltip
                            cursor={{fill: '#F7FAFC'}}
                            contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', padding: '12px' }}
                          />
                          <Bar dataKey="fpMedioAtivo" name="F.P. Médio (Ativos)" fill="#008858" radius={[4, 4, 0, 0]} barSize={35} />
                        </BarChart>
                      </ResponsiveContainer>
                    ) : (
                      <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '14px', border: '2px dashed #E2E8F0', borderRadius: '12px' }}>
                        Sem dados para lotes ativos.
                      </div>
                    )}
                  </div>
                </div>

                {/* Gráfico Fator Produção Encerrados */}
                <div className="card-scientific">
                  <h4 className="card-header" style={{ fontSize: '15px', border: 'none', marginBottom: '20px', fontWeight: '800' }}>
                    <BarChart3 size={18} color="#0B3B75" style={{ marginRight: '8px' }} />
                    Méd. Fator Produção (Lotes Encerrados)
                  </h4>
                  <div style={{ height: 260, width: '100%' }}>
                    {data.produtores.length > 0 ? (
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data.produtores} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                          <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: '700', fill: '#4A5568'}} />
                          <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10}} />
                          <Tooltip
                            cursor={{fill: '#F7FAFC'}}
                            contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', padding: '12px' }}
                          />
                          <Bar dataKey="fpMedioEncerrado" name="F.P. Médio (Encerrados)" fill="#0B3B75" radius={[4, 4, 0, 0]} barSize={35} />
                        </BarChart>
                      </ResponsiveContainer>
                    ) : (
                      <div style={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '14px', border: '2px dashed #E2E8F0', borderRadius: '12px' }}>
                        Sem dados para lotes encerrados.
                      </div>
                    )}
                  </div>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '24px' }}>

                {/* Tabela de Performance */}
                {isAdmin && (
                  <div className="card-scientific">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', paddingBottom: '12px', borderBottom: '1px solid #edf2f7' }}>
                      <h3 style={{ fontSize: '16px', fontWeight: '800', color: 'var(--primary-navy)', display: 'flex', alignItems: 'center', gap: '8px', margin: 0 }}>
                        <TrendingUp size={20} color="var(--primary-blue)" /> Performance Consolidada por Produtor
                      </h3>
                    </div>
                    <div className="table-responsive">
                      <table className="info-table" style={{ fontSize: '13px' }}>
                        <thead>
                          <tr style={{ background: '#F8FAFB' }}>
                            <th>Produtor</th>
                            <th style={{ textAlign: 'center' }}>Aves Vivas</th>
                            <th style={{ textAlign: 'center' }}>Mort. %</th>
                            <th style={{ textAlign: 'center' }}>Viabilidade</th>
                            <th style={{ textAlign: 'center' }}>Lotes Ativos</th>
                          </tr>
                        </thead>
                        <tbody>
                          {data.produtores.length > 0 ? (
                            data.produtores.map(p => (
                              <tr key={p.id}>
                                <td>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                    <div style={{ width: '32px', height: '32px', background: '#E6F4EF', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#008858', fontWeight: 'bold', fontSize: '11px' }}>
                                      {p.nome.charAt(0)}
                                    </div>
                                    <div>
                                      <div style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>{p.nome}</div>
                                      <div style={{ fontSize: '10px', color: 'var(--text-muted)' }}>{p.email}</div>
                                    </div>
                                  </div>
                                </td>
                                <td style={{ textAlign: 'center', fontWeight: '600' }}>{p.avesVivas.toLocaleString('pt-BR')}</td>
                                <td style={{ textAlign: 'center', color: p.mortalidadeNum > 5 ? '#E53E3E' : '#2D8A4E', fontWeight: '800' }}>
                                  {p.mortalidade}%
                                </td>
                                <td style={{ textAlign: 'center' }}>{p.viabilidade}</td>
                                <td style={{ textAlign: 'center', fontWeight: '700', color: 'var(--primary-blue)' }}>{p.lotesAtivos}</td>
                              </tr>
                            ))
                          ) : (
                            <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>Aguardando sincronização de dados...</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>

              <div style={{ height: '40px' }}></div>
            </>
          )}
        </main>
      </div>
    </div>
  );
};

export default DashboardPage;
