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
        
        String cleanValue = value.trim();
        
        try {
            // Lógica robusta para tratar diferentes formatos de entrada:
            
            // 1. Se contém vírgula e ponto: Assumimos padrão BR (ex: 1.250,50)
            if (cleanValue.contains(",") && cleanValue.contains(".")) {
                cleanValue = cleanValue.replace(".", "").replace(",", ".");
            } 
            // 2. Se contém apenas vírgula: É o decimal brasileiro (ex: 1,50)
            else if (cleanValue.contains(",")) {
                cleanValue = cleanValue.replace(",", ".");
            }
            // 3. Se contém apenas ponto: No Android, é comum o teclado numérico 
            // fornecer apenas o ponto, mesmo em PT-BR. Tratamos como decimal.
            
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            // Fallback final usando NumberFormat
            try {
                NumberFormat format = NumberFormat.getInstance(PT_BR);
                return format.parse(value).doubleValue();
            } catch (Exception e2) {
                return 0.0;
            }
        }
    }
    
    public static String formatDouble(double value) {
        NumberFormat format = NumberFormat.getInstance(PT_BR);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(3);
        format.setGroupingUsed(false); // Desabilitado para facilitar a edição no campo
        return format.format(value);
    }
}
