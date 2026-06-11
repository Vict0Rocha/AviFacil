const safeDate = (dateVal) => {
    if (!dateVal) return new Date();
    if (dateVal.toDate) return dateVal.toDate();
    const d = new Date(dateVal);
    return isNaN(d.getTime()) ? new Date() : d;
};

export const calcularTotalMortas = (registros) => {
    if (!registros || !Array.isArray(registros)) return 0;
    return registros.reduce((total, r) => total + (Number(r.avesMortasPeriodo) || 0), 0);
};

export const calcularAvesVivas = (lote, registros) => {
    if (!lote) return 0;
    const inicial = Number(lote.quantidadeAvesInicial) || 0;
    return Math.max(0, inicial - calcularTotalMortas(registros));
};

export const calcularMortalidade = (lote, registros) => {
    if (!lote || !lote.quantidadeAvesInicial || lote.quantidadeAvesInicial === 0) return 0;
    return (calcularTotalMortas(registros) * 100.0) / lote.quantidadeAvesInicial;
};

export const calcularViabilidade = (lote, registros) => {
    return 100.0 - calcularMortalidade(lote, registros);
};

export const calcularIdadeDias = (lote, dataReferencia = new Date()) => {
    if (!lote || !lote.dataInicio) return 1;
    const dataInicio = safeDate(lote.dataInicio);
    const diffInMs = dataReferencia.getTime() - dataInicio.getTime();
    if (diffInMs < 0) return 1;
    return Math.floor(diffInMs / (1000 * 60 * 60 * 24)) + 1;
};

export const calcularPesoMedioAtual = (registros) => {
    if (!registros || registros.length === 0) return 0;
    const sorted = [...registros].sort((a, b) => {
        const da = safeDate(a.dataRegistro);
        const db = safeDate(b.dataRegistro);
        return db - da;
    });
    return Number(sorted[0].pesoAtualMedio) || 0;
};

export const calcularTotalConsumoRacao = (registros) => {
    if (!registros) return 0;
    return registros.reduce((total, r) => total + (Number(r.consumoRacaoPeriodo) || 0), 0);
};

export const calcularGanhoMedioPeso = (lote, registros) => {
    if (!lote || !registros || registros.length === 0) return 0;
    const pesoAtual = calcularPesoMedioAtual(registros);
    const pesoInicial = Number(lote.pesoInicial) || 0;
    const idadeDias = calcularIdadeDias(lote);
    if (idadeDias <= 0) return 0;
    return (pesoAtual - pesoInicial) / idadeDias;
};

export const calcularConversaoAlimentar = (lote, registros) => {
    if (!lote || !registros || registros.length === 0) return 0;
    const consumoTotal = calcularTotalConsumoRacao(registros);
    const vivas = calcularAvesVivas(lote, registros);
    const pesoAtual = calcularPesoMedioAtual(registros);
    const pesoInicial = Number(lote.pesoInicial) || 0;

    if (consumoTotal <= 0 || vivas <= 0) return 0;
    const ganhoPesoMedioKg = (pesoAtual - pesoInicial) / 1000.0;
    const ganhoPesoTotalLoteKg = ganhoPesoMedioKg * vivas;
    if (ganhoPesoTotalLoteKg <= 0) return 0;
    return consumoTotal / ganhoPesoTotalLoteKg;
};
