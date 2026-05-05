package com.example.avifacil.util;

import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.util.List;

public class ZootecniaCalculator {

    public static int calcularAvesVivas(LoteEntity lote, List<RegistroEntity> registros) {
        int totalMortas = 0;
        for (RegistroEntity r : registros) {
            totalMortas += r.getAvesMortasPeriodo();
        }
        return lote.getQuantidadeAvesInicial() - totalMortas;
    }

    public static double calcularMortalidade(LoteEntity lote, List<RegistroEntity> registros) {
        if (lote.getQuantidadeAvesInicial() == 0) return 0;
        int totalMortas = 0;
        for (RegistroEntity r : registros) {
            totalMortas += r.getAvesMortasPeriodo();
        }
        return (totalMortas * 100.0) / lote.getQuantidadeAvesInicial();
    }

    public static double calcularTotalConsumoRacao(List<RegistroEntity> registros) {
        double total = 0;
        for (RegistroEntity r : registros) {
            total += r.getConsumoRacaoPeriodo();
        }
        return total;
    }

    public static double calcularViabilidade(LoteEntity lote, List<RegistroEntity> registros) {
        return 100.0 - calcularMortalidade(lote, registros);
    }
    
    public static double calcularPesoMedioAtual(List<RegistroEntity> registros) {
        if (registros.isEmpty()) return 0;
        // Pega o peso do registro mais recente (assumindo que a lista está ordenada por data ASC)
        return registros.get(registros.size() - 1).getPesoAtualMedio();
    }

    public static double calcularConversaoAlimentar(LoteEntity lote, List<RegistroEntity> registros) {
        int avesVivas = calcularAvesVivas(lote, registros);
        double pesoMedio = calcularPesoMedioAtual(registros);
        double consumoTotal = calcularTotalConsumoRacao(registros);

        if (avesVivas == 0 || pesoMedio == 0) return 0;

        // CA = Consumo Total (kg) / Peso Total Vivo (kg)
        double pesoTotalVivoKg = (avesVivas * pesoMedio) / 1000.0;
        return consumoTotal / pesoTotalVivoKg;
    }
}
