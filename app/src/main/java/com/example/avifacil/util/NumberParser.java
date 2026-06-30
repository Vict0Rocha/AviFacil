package com.example.avifacil.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Utilitário para converter strings formatadas no padrão brasileiro (vírgula)
 * para valores numéricos (double) processáveis pelo sistema.
 */
public class NumberParser {

    private static final Locale PT_BR = new Locale("pt", "BR");

    public static double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        
        try {
            // Tenta converter usando o padrão brasileiro (vírgula como separador decimal)
            NumberFormat format = NumberFormat.getInstance(PT_BR);
            Number number = format.parse(value.replace(".", "")); // Remove pontos de milhar se existirem
            return number.doubleValue();
        } catch (ParseException e) {
            try {
                // Fallback para o padrão americano (caso o usuário ainda digite ponto)
                return Double.parseDouble(value.replace(",", "."));
            } catch (NumberFormatException e2) {
                return 0.0;
            }
        }
    }
    
    public static String formatDouble(double value) {
        NumberFormat format = NumberFormat.getInstance(PT_BR);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(3);
        return format.format(value);
    }
}
