import React from 'react';
import Sidebar from '../components/Sidebar';
import { BookOpen, AlertTriangle, CheckCircle, TrendingUp, Thermometer, Droplets } from 'lucide-react';

const GuiaCard = ({ title, icon: Icon, color, content, recommendations }) => (
  <div className="card-scientific" style={{ marginBottom: '24px' }}>
    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '20px' }}>
      <div style={{ background: `${color}15`, color: color, padding: '10px', borderRadius: '12px' }}>
        <Icon size={24} />
      </div>
      <h3 style={{ color: 'var(--primary-navy)', fontWeight: '800', fontSize: '18px' }}>{title}</h3>
    </div>
    <p style={{ color: 'var(--text-main)', fontSize: '14px', lineHeight: '1.6', marginBottom: '20px' }}>{content}</p>
    <div style={{ background: 'var(--bg-scientific)', padding: '16px', borderRadius: '12px', borderLeft: `4px solid ${color}` }}>
      <h4 style={{ fontSize: '12px', textTransform: 'uppercase', color: 'var(--text-muted)', fontWeight: '800', marginBottom: '10px', letterSpacing: '0.5px' }}>Checklist de Decisão</h4>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {recommendations.map((item, index) => (
          <li key={index} style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', fontSize: '13px', marginBottom: '8px', color: 'var(--text-main)', fontWeight: '600' }}>
            <CheckCircle size={16} color={color} style={{ marginTop: '2px', flexShrink: 0 }} />
            {item}
          </li>
        ))}
      </ul>
    </div>
  </div>
);

const GuiasPage = () => {
  const guias = [
    {
      title: "Manejo de Ambiência e Temperatura",
      icon: Thermometer,
      color: "#E53E3E",
      content: "O controle térmico é o fator que mais impacta a Conversão Alimentar (CA). Aves em estresse térmico gastam energia para se resfriar em vez de converter ração em peso.",
      recommendations: [
        "Verifique a vedação das cortinas para evitar correntes de ar diretas.",
        "Monitore a temperatura interna: ideal entre 28°C e 32°C na primeira semana.",
        "Acione o sistema de nebulização se a umidade relativa estiver abaixo de 50%.",
        "Observe o comportamento: aves amontoadas indicam frio; aves arquejando indicam calor."
      ]
    },
    {
      title: "Otimização de Conversão Alimentar (CA)",
      icon: TrendingUp,
      color: "#3182CE",
      content: "Uma CA elevada indica desperdício. Pequenos ajustes no manejo diário podem representar economias significativas no custo final do lote.",
      recommendations: [
        "Ajuste a altura dos comedouros diariamente: o prato deve estar na altura do peito das aves.",
        "Elimine vazamentos nos bebedouros: cama úmida aumenta a fermentação e o estresse.",
        "Verifique a qualidade da ração (presença de finos ou mofo).",
        "Mantenha o programa de luz rigorosamente conforme a linhagem."
      ]
    },
    {
      title: "Monitoramento de Sanidade e Mortalidade",
      icon: AlertTriangle,
      color: "#D69E2E",
      content: "Picos de mortalidade após a primeira semana devem ser investigados imediatamente. A detecção precoce de patologias evita perdas em massa.",
      recommendations: [
        "Realize a necropsia de aves mortas para identificar sinais de colibacilose ou coccidiose.",
        "Mantenha o vazio sanitário rigoroso entre os lotes (mínimo 15 dias).",
        "Controle rigorosamente o acesso de pessoas e veículos à granja.",
        "Troque o desinfetante dos pedilúvios diariamente."
      ]
    }
  ];

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <div className="top-bar-scientific">
          <div>
            <h1 style={{ color: 'var(--primary-navy)', fontSize: '24px', fontWeight: '800' }}>Guias de Decisão Técnica</h1>
            <p style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Protocolos baseados em indicadores zootécnicos</p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--primary-blue)', fontWeight: '700', fontSize: '14px' }}>
            <BookOpen size={20} /> Manual de Boas Práticas
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
          {guias.map((guia, index) => (
            <GuiaCard key={index} {...guia} />
          ))}
        </div>
      </main>
    </div>
  );
};

export default GuiasPage;
