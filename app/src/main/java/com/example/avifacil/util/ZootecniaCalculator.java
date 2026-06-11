package com.example.avifacil.util;

import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZootecniaCalculator {

    public static int calcularTotalMortas(List<RegistroEntity> registros) {
        int total = 0;
        if (registros == null) return 0;
        for (RegistroEntity r : registros) {
            total += r.getAvesMortasPeriodo();
        }
        return total;
    }

    public static int calcularAvesVivas(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null) return 0;
        return lote.getQuantidadeAvesInicial() - calcularTotalMortas(registros);
    }

    public static double calcularMortalidade(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null || lote.getQuantidadeAvesInicial() == 0) return 0;
        return (calcularTotalMortas(registros) * 100.0) / lote.getQuantidadeAvesInicial();
    }

    public static double calcularViabilidade(LoteEntity lote, List<RegistroEntity> registros) {
        return 100.0 - calcularMortalidade(lote, registros);
    }

    public static int calcularIdadeDias(LoteEntity lote, Date dataReferencia) {
        if (lote == null || lote.getDataInicio() == null || dataReferencia == null) return 1;
        long diffInMs = dataReferencia.getTime() - lote.getDataInicio().getTime();
        if (diffInMs < 0) return 1;
        return (int) TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS) + 1;
    }

    public static double calcularPesoMedioAtualKg(List<RegistroEntity> registros) {
        if (registros == null || registros.isEmpty()) return 0;
        // O peso no registro agora é em GRAMAS
        return registros.get(registros.size() - 1).getPesoAtualMedio() / 1000.0;
    }

    public static double calcularTotalConsumoRacao(List<RegistroEntity> registros) {
        double total = 0;
        if (registros == null) return 0;
        for (RegistroEntity r : registros) {
            total += r.getConsumoRacaoPeriodo();
        }
        return total;
    }

    public static double calcularConversaoAlimentar(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;
        
        double consumoTotalKg = calcularTotalConsumoRacao(registros);
        int vivas = calcularAvesVivas(lote, registros);
        double pesoMedioG = registros.get(registros.size() - 1).getPesoAtualMedio();

        if (consumoTotalKg <= 0 || vivas <= 0 || pesoMedioG <= 0) return 0;

        // Ganho de Peso Total (kg) = (Peso Final Total(g) - Peso Inicial Total(g)) / 1000
        double pesoFinalTotalKg = (pesoMedioG * vivas) / 1000.0;
        double pesoInicialTotalKg = (lote.getPesoInicial() * lote.getQuantidadeAvesInicial()) / 1000.0;
        
        double ganhoPesoTotalKg = pesoFinalTotalKg - pesoInicialTotalKg;
        
        if (ganhoPesoTotalKg <= 0) return 0;

        // CA = Consumo Total (kg) / Ganho de Peso Total (kg)
        return consumoTotalKg / ganhoPesoTotalKg;
    }

    public static double calcularGPD(LoteEntity lote, List<RegistroEntity> registros, Date dataReferencia) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;

        double pesoMedioG = registros.get(registros.size() - 1).getPesoAtualMedio();
        double pesoInicialPintoG = lote.getPesoInicial();
        int idadeDias = calcularIdadeDias(lote, dataReferencia);

        if (idadeDias <= 0) return 0;

        // GPD (g/dia) = (Peso Médio Atual (g) - Peso Inicial (g)) / Idade (dias)
        return (pesoMedioG - pesoInicialPintoG) / idadeDias;
    }

    public static double calcularFatorProducao(LoteEntity lote, List<RegistroEntity> registros, Date dataReferencia) {
        double gpdG = calcularGPD(lote, registros, dataReferencia);
        double viabilidade = calcularViabilidade(lote, registros);
        double ca = calcularConversaoAlimentar(lote, registros);

        if (ca <= 0) return 0;

        // FP = (Viabilidade(%) * GPD(kg)) / (CA * 10)
        // Convertendo GPD de g para kg: gpdG / 1000.0
        return (viabilidade * (gpdG / 1000.0)) / (ca * 10.0);
    }

    public static double calcularCustoTotalInsumos(List<RegistroEntity> registros) {
        if (registros == null) return 0;
        double custoTotal = 0;
        for (RegistroEntity r : registros) {
            custoTotal += r.getConsumoRacaoPeriodo() * r.getPrecoKgInsumo();
        }
        return custoTotal;
    }

    public static double calcularPrecoMedioInsumo(List<RegistroEntity> registros) {
        double consumoTotal = calcularTotalConsumoRacao(registros);
        if (consumoTotal <= 0) return 0;
        return calcularCustoTotalInsumos(registros) / consumoTotal;
    }
}
