export const safeDate = (dateVal) => {
    if (!dateVal) return new Date();
    if (dateVal.toDate) return dateVal.toDate();
    const d = new Date(dateVal);
    return isNaN(d.getTime()) ? new Date() : d;
};

// Funções de Arredondamento para bater com o Mobile (RoundingMode.HALF_UP)
const round = (value, places) => {
    if (!value || isNaN(value)) return 0;
    return Number(Math.round(value + "e" + places) + "e-" + places);
};

export const calcularTotalMortas = (registros) => {
    if (!registros || !Array.isArray(registros)) return 0;
    return registros.reduce((total, r) => total + (Number(r.avesMortasPeriodo) || 0), 0);
};

export const calcularAvesVivas = (lote, registros) => {
    if (!lote) return 0;
    const inicial = Number(lote.quantidadeAvesInicial) || 0;
    const perdas = calcularTotalMortas(registros);
    return Math.max(0, inicial - perdas);
};

export const calcularMortalidade = (lote, registros) => {
    if (!lote || !lote.quantidadeAvesInicial || lote.quantidadeAvesInicial === 0) return 0;
    const perdas = calcularTotalMortas(registros);
    return round((perdas / lote.quantidadeAvesInicial) * 100.0, 2);
};

export const calcularViabilidade = (lote, registros) => {
    if (!lote || !lote.quantidadeAvesInicial || lote.quantidadeAvesInicial === 0) return 100.0;
    const vivas = calcularAvesVivas(lote, registros);
    return round((vivas / lote.quantidadeAvesInicial) * 100.0, 2);
};

export const calcularIdadeDias = (lote, dataReferencia = null) => {
    if (!lote || !lote.dataInicio) return 0;
    const dataInicio = safeDate(lote.dataInicio);

    let ref = dataReferencia;
    if (!ref) {
        if (lote.status === 'FINALIZADO' && lote.dataFim) {
            ref = safeDate(lote.dataFim);
        } else {
            ref = new Date();
        }
    } else {
        ref = safeDate(ref);
    }

    const diffInMs = ref.getTime() - dataInicio.getTime();
    if (diffInMs < 0) return 0;
    return Math.floor(diffInMs / (1000 * 60 * 60 * 24));
};

export const calcularPesoMedioAtual = (registros) => {
    if (!registros || registros.length === 0) return 0;
    // Ordena para garantir que pegamos o registro mais recente (maior data)
    const sorted = [...registros].sort((a, b) => {
        const da = safeDate(a.dataRegistro);
        const db = safeDate(b.dataRegistro);
        return db - da;
    });
    return Number(sorted[0].pesoAtualMedio) || 0;
};

export const calcularTotalConsumoRacao = (registros) => {
    if (!registros) return 0;
    return registros.reduce((total, r) => total + Math.max(0, Number(r.consumoRacaoPeriodo) || 0), 0);
};

export const calcularCustoTotalRacao = (registros) => {
    if (!registros) return 0;
    const custoTotal = registros.reduce((total, r) => {
        const consumo = Math.max(0, Number(r.consumoRacaoPeriodo) || 0);
        const preco = Number(r.precoKgInsumo) || 0;
        return total + (consumo * preco);
    }, 0);
    return round(custoTotal, 2);
};

export const calcularGanhoMedioPeso = (lote, registros) => {
    if (!lote || !registros || registros.length === 0) return 0;
    const sorted = [...registros].sort((a, b) => safeDate(b.dataRegistro) - safeDate(a.dataRegistro));
    const pesoMedioG = Number(sorted[0].pesoAtualMedio) || 0;
    const pesoInicialG = Number(lote.pesoInicial) || 0;
    const idadeDias = calcularIdadeDias(lote, sorted[0].dataRegistro);
    if (idadeDias <= 0 || pesoMedioG <= 0) return 0;
    return round((pesoMedioG - pesoInicialG) / idadeDias, 2);
};

export const calcularConversaoAlimentar = (lote, registros) => {
    if (!lote || !registros || registros.length === 0) return 0;
    const consumoTotalKg = calcularTotalConsumoRacao(registros);
    const vivas = calcularAvesVivas(lote, registros);
    const pesoMedioG = calcularPesoMedioAtual(registros);
    const pesoMedioKg = pesoMedioG / 1000.0;

    if (consumoTotalKg <= 0 || vivas <= 0 || pesoMedioKg <= 0) return 0;

    // Biomassa total (igual ao mobile)
    const pesoTotalLoteKg = pesoMedioKg * vivas;
    return round(consumoTotalKg / pesoTotalLoteKg, 2);
};

export const calcularFatorProducao = (lote, registros) => {
    if (!lote || !registros || registros.length === 0) return 0;

    const sorted = [...registros].sort((a, b) => safeDate(b.dataRegistro) - safeDate(a.dataRegistro));
    const gpdGramas = calcularGanhoMedioPeso(lote, registros);
    const gpdKg = gpdGramas / 1000.0;
    const viabilidade = calcularViabilidade(lote, registros);
    const ca = calcularConversaoAlimentar(lote, registros);

    if (ca <= 0) return 0;

    // Nova Fórmula: ((GPD(kg) * Viabilidade %) / CA) * 100
    // Arredondado para 0 casas decimais
    return round(((gpdKg * viabilidade) / ca) * 100.0, 0);
};

export const calcularCustoRacaoPorAve = (lote, registros) => {
    const vivas = calcularAvesVivas(lote, registros);
    if (vivas <= 0) return 0;
    return round(calcularCustoTotalRacao(registros) / vivas, 2);
};

export const calcularCustoRacaoPorKgFrango = (lote, registros) => {
    const ca = calcularConversaoAlimentar(lote, registros);
    const consumoTotal = calcularTotalConsumoRacao(registros);
    if (consumoTotal <= 0 || ca <= 0) return 0;

    const precoMedioKg = calcularCustoTotalRacao(registros) / consumoTotal;
    return round(ca * precoMedioKg, 2);
};
