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
        if (diffInMs < 0) return 1; // Caso a data seja anterior ao início, assume dia 1
        return (int) TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS) + 1;
    }

    public static double calcularPesoMedioAtual(List<RegistroEntity> registros) {
        if (registros == null || registros.isEmpty()) return 0;
        // Pega o último registro adicionado (assumindo que estão ordenados ou é o mais recente)
        return registros.get(registros.size() - 1).getPesoAtualMedio();
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
        
        double consumoTotal = calcularTotalConsumoRacao(registros);
        int vivas = calcularAvesVivas(lote, registros);
        double pesoAtual = calcularPesoMedioAtual(registros);

        if (consumoTotal <= 0 || vivas <= 0) return 0;

        // Conforme fórmula: CA = consumoTotal / ((pesoAtualMedio - lote.getPesoInicial()) * avesVivas / 1000)
        double ganhoPesoMedioKg = (pesoAtual - lote.getPesoInicial()) / 1000.0;
        double ganhoPesoTotalLoteKg = ganhoPesoMedioKg * vivas;

        if (ganhoPesoTotalLoteKg <= 0) return 0;

        return consumoTotal / ganhoPesoTotalLoteKg;
    }

    public static double calcularGanhoMedioPeso(LoteEntity lote, List<RegistroEntity> registros, Date dataReferencia) {
        if (lote == null || registros == null || registros.isEmpty()) return 0;

        double pesoAtual = calcularPesoMedioAtual(registros);
        double pesoInicial = lote.getPesoInicial();
        int idadeDias = calcularIdadeDias(lote, dataReferencia);

        if (idadeDias <= 0) return 0;

        // GANHO MÉDIO DE PESO = PESO ATUAL (g) – PESO INICIAL / IDADES DAS AVES (dia)
        return (pesoAtual - pesoInicial) / idadeDias;
    }
}
