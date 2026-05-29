export const mockStats = [
  { label: 'Sobrevivência', value: '97.2%', sub: '+0.5% vs semana anterior', color: 'var(--accent-green)' },
  { label: 'Conversão Alimentar', value: '1.42', sub: 'Meta: 1.45', color: 'var(--accent-blue)' },
  { label: 'Ração Consumida', value: '4.250 kg', sub: 'Estoque: 1.200 kg', color: 'var(--text-secondary)' },
];

export const mockChartData = [
  { dia: 'Dia 1', peso: 0.045, meta: 0.045 },
  { dia: 'Dia 7', peso: 0.180, meta: 0.190 },
  { dia: 'Dia 14', peso: 0.450, meta: 0.480 },
  { dia: 'Dia 21', peso: 0.920, meta: 0.950 },
  { dia: 'Dia 28', peso: 1.550, meta: 1.580 },
  { dia: 'Dia 35', peso: 2.100, meta: 2.150 },
  { dia: 'Dia 42', peso: 2.750, meta: 2.800 },
];

export const currentLote = {
  id: "LOTE-2024-001",
  galpao: "Galpão 04",
  linhagem: "Cobb 500",
  dataInicio: "12/05/2024",
  status: "Em andamento",
  alerta: "Desempenho dentro da meta esperada para a idade."
};
