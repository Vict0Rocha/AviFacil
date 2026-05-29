package com.example.avifacil.domain.calculation;

public class ZootecniaCalculator {

    public static int calcularAvesVivas(int inicial, int mortasAcumuladas) {
        return Math.max(0, inicial - mortasAcumuladas);
    }

    public static double calcularPercentualMortalidade(int inicial, int mortasAcumuladas) {
        if (inicial <= 0) return 0;
        return (mortasAcumuladas / (double) inicial) * 100;
    }

    public static double calcularViabilidade(int inicial, int mortasAcumuladas) {
        return 100 - calcularPercentualMortalidade(inicial, mortasAcumuladas);
    }

    public static double calcularConversaoAlimentar(double racaoTotalKg, int inicial, int mortasAcumuladas, double pesoMedioAtualKg) {
        int vivas = calcularAvesVivas(inicial, mortasAcumuladas);
        if (vivas <= 0 || pesoMedioAtualKg <= 0) return 0;
        double massaTotal = vivas * pesoMedioAtualKg;
        return racaoTotalKg / massaTotal;
    }

    public static double calcularPesoTotalEstimado(int inicial, int mortasAcumuladas, double pesoMedioKg) {
        return calcularAvesVivas(inicial, mortasAcumuladas) * pesoMedioKg;
    }
}
