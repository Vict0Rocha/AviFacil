package com.example.avifacil.util;

import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZootecniaCalculator {

    private static double round(double value, int places) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0;
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int calcularIdadeDias(LoteEntity lote, Date dataReferencia) {
        if (lote == null || lote.getDataInicio() == null || dataReferencia == null) return 0;
        long diffInMs = dataReferencia.getTime() - lote.getDataInicio().getTime();
        if (diffInMs < 0) return 0;
        return (int) TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
    }

    public static int calcularAvesPerdidasAcumuladas(List<RegistroEntity> registros) {
        if (registros == null) return 0;
        int total = 0;
        for (RegistroEntity r : registros) {
            total += r.getAvesMortasPeriodo();
        }
        return total;
    }

    public static int calcularAvesVivasAtual(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null) return 0;
        int perdas = calcularAvesPerdidasAcumuladas(registros);
        return Math.max(0, lote.getQuantidadeAvesInicial() - perdas);
    }

    public static double calcularConsumoTotalRacaoKg(List<RegistroEntity> registros) {
        if (registros == null) return 0;
        double total = 0;
        for (RegistroEntity r : registros) {
            total += Math.max(0, r.getConsumoRacaoPeriodo());
        }
        return total;
    }

    public static double calcularMortalidadeAcumuladaPercentual(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null || lote.getQuantidadeAvesInicial() <= 0) return 0;
        int perdas = calcularAvesPerdidasAcumuladas(registros);
        return round(((double) perdas / lote.getQuantidadeAvesInicial()) * 100.0, 2);
    }

    public static double calcularViabilidadePercentual(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null || lote.getQuantidadeAvesInicial() <= 0) return 100.0;
        int vivas = calcularAvesVivasAtual(lote, registros);
        return round(((double) vivas / lote.getQuantidadeAvesInicial()) * 100.0, 2);
    }

    public static double calcularPesoMedioAtualKg(List<RegistroEntity> registros) {
        if (registros == null || registros.isEmpty()) return 0;
        double pesoG = registros.get(registros.size() - 1).getPesoAtualMedio();
        return round(pesoG / 1000.0, 3);
    }

    public static double calcularConversaoAlimentar(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;
        
        double consumoTotalKg = calcularConsumoTotalRacaoKg(registros);
        int vivas = calcularAvesVivasAtual(lote, registros);
        
        // Peso médio do último registro em gramas
        double pesoMedioG = registros.get(registros.size() - 1).getPesoAtualMedio();
        double pesoMedioKg = pesoMedioG / 1000.0;

        if (consumoTotalKg <= 0 || vivas <= 0 || pesoMedioKg <= 0) return 0;

        // Biomassa total (peso vivo total no galpão)
        double pesoTotalLoteKg = pesoMedioKg * vivas;
        
        // Padrão de mercado: CA com 2 casas decimais
        return round(consumoTotalKg / pesoTotalLoteKg, 2);
    }

    public static double calcularGPD(LoteEntity lote, List<RegistroEntity> registros, Date dataReferencia) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;

        // Para o GPD ser preciso, devemos usar a idade na data da última pesagem
        RegistroEntity ultimo = registros.get(registros.size() - 1);
        double pesoMedioG = ultimo.getPesoAtualMedio();
        double pesoInicialG = lote.getPesoInicial();
        int idadeDias = calcularIdadeDias(lote, ultimo.getDataRegistro());

        if (idadeDias <= 0 || pesoMedioG <= 0) return 0;

        return round((pesoMedioG - pesoInicialG) / idadeDias, 2);
    }

    public static double calcularFatorProducao(LoteEntity lote, List<RegistroEntity> registros, Date dataReferencia) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;

        // Pegamos o último registro para garantir consistência entre peso e idade
        RegistroEntity ultimo = registros.get(registros.size() - 1);
        double pesoMedioG = ultimo.getPesoAtualMedio();
        double pesoInicialG = lote.getPesoInicial();
        int idadeDias = calcularIdadeDias(lote, ultimo.getDataRegistro());

        if (idadeDias <= 0 || pesoMedioG <= 0) return 0;

        // GPD em kg (Sem arredondamento intermediário para precisão máxima do FP)
        double gpdKg = ((pesoMedioG - pesoInicialG) / idadeDias) / 1000.0;
        
        // Viabilidade em %
        int vivas = calcularAvesVivasAtual(lote, registros);
        double viabilidade = ((double) vivas / lote.getQuantidadeAvesInicial()) * 100.0;
        
        // C.A. (Consumo Total / Biomassa Total)
        double consumoTotalKg = calcularConsumoTotalRacaoKg(registros);
        double pesoTotalLoteKg = (pesoMedioG / 1000.0) * vivas;
        
        if (pesoTotalLoteKg <= 0) return 0;
        double ca = consumoTotalKg / pesoTotalLoteKg;

        if (ca <= 0) return 0;

        // Fórmula solicitada: Fator de produção = ((GPD_kg * viabilidade_%) / C.A.) * 100
        // Arredondado para 0 casas decimais conforme padrão do setor
        return round(((gpdKg * viabilidade) / ca) * 100.0, 0);
    }

    public static double calcularCustoTotalRacao(List<RegistroEntity> registros) {
        if (registros == null) return 0;
        double custoTotal = 0;
        for (RegistroEntity r : registros) {
            custoTotal += r.getConsumoRacaoPeriodo() * r.getPrecoKgInsumo();
        }
        return round(custoTotal, 2);
    }

    public static double calcularCustoRacaoPorAve(LoteEntity lote, List<RegistroEntity> registros) {
        int vivas = calcularAvesVivasAtual(lote, registros);
        if (vivas <= 0) return 0;
        return round(calcularCustoTotalRacao(registros) / vivas, 2);
    }

    public static double calcularCustoRacaoPorKgFrango(LoteEntity lote, List<RegistroEntity> registros) {
        double ca = calcularConversaoAlimentar(lote, registros);
        double consumoTotal = calcularConsumoTotalRacaoKg(registros);
        if (consumoTotal <= 0 || ca <= 0) return 0;
        
        double precoMedioKg = calcularCustoTotalRacao(registros) / consumoTotal;
        return round(ca * precoMedioKg, 2);
    }
}
