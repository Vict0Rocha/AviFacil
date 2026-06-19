// Padrões de Desempenho (Referência Cobb 500 - Aproximado)
export const COBB_500_WEIGHT = [
  { idade: 0, peso: 42 },
  { idade: 7, peso: 190 },
  { idade: 14, peso: 480 },
  { idade: 21, peso: 950 },
  { idade: 28, peso: 1580 },
  { idade: 35, peso: 2250 },
  { idade: 42, peso: 2950 },
  { idade: 49, peso: 3550 }
];

export const COBB_500_CA = [
  { idade: 7, ca: 1.05 },
  { idade: 14, ca: 1.22 },
  { idade: 21, ca: 1.38 },
  { idade: 28, ca: 1.52 },
  { idade: 35, ca: 1.65 },
  { idade: 42, ca: 1.78 }
];

export const getStandardWeight = (idade) => {
  if (idade <= 0) return 42;
  const match = COBB_500_WEIGHT.find(p => p.idade === idade);
  if (match) return match.peso;

  // Interpolação simples se não achar o dia exato
  const low = [...COBB_500_WEIGHT].reverse().find(p => p.idade <= idade);
  const high = COBB_500_WEIGHT.find(p => p.idade > idade);

  if (!high) return low.peso;

  const ratio = (idade - low.idade) / (high.idade - low.idade);
  return low.peso + (high.peso - low.peso) * ratio;
};
