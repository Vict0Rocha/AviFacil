package com.example.avifacil.util;

import static org.junit.Assert.assertEquals;

import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ZootecniaCalculatorTest {

    private LoteEntity lote;
    private List<RegistroEntity> registros;
    private Date dataInicio;

    @Before
    public void setUp() {
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.MAY, 1);
        dataInicio = cal.getTime();

        lote = new LoteEntity();
        lote.setQuantidadeAvesInicial(1000);
        lote.setPesoInicial(0.045); // 45g em kg
        lote.setDataInicio(dataInicio);

        registros = new ArrayList<>();
    }

    @Test
    public void testCalcularConversaoAlimentar() {
        // Simular 10 aves mortas
        registros.add(new RegistroEntity(1, "uuid", dataInicio, 10, 50.0, 0.5, 2.5, "milho"));
        // Mais 10 aves mortas, peso médio final 2.0kg, consumo total 3000kg
        // No cenário real, teríamos múltiplos registros. Aqui vamos simplificar para testar a lógica.
        
        // Vamos limpar e fazer um cenário controlado
        registros.clear();
        // Dia 1: 1000 aves, consome 50kg, 10 morrem.
        registros.add(new RegistroEntity(1, "u1", dataInicio, 10, 50.0, 0.1, 2.0, "milho"));
        
        // Dia 40: Peso médio 2.0kg, Consumo total acumulado de 3500kg, Total mortas 50.
        // Como o calculator soma os consumos da lista:
        registros.clear();
        registros.add(new RegistroEntity(1, "u1", dataInicio, 50, 3500.0, 2.0, 2.0, "milho"));

        // Vivas = 1000 - 50 = 950
        // Peso Final Total = 950 * 2.0 = 1900 kg
        // Peso Inicial Total = 1000 * 0.045 = 45 kg
        // Ganho Peso Total = 1900 - 45 = 1855 kg
        // CA = 3500 / 1855 = 1.88679...
        
        double ca = ZootecniaCalculator.calcularConversaoAlimentar(lote, registros);
        assertEquals(1.88679, ca, 0.0001);
    }

    @Test
    public void testCalcularGPD() {
        registros.add(new RegistroEntity(1, "u1", dataInicio, 0, 0, 2.045, 0, ""));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataInicio);
        cal.add(Calendar.DAY_OF_YEAR, 39); // Dia 40 (diff 39 + 1)
        Date dataFim = cal.getTime();

        // GPD = (2.045 - 0.045) / 40 = 2.0 / 40 = 0.05 kg/dia
        double gpd = ZootecniaCalculator.calcularGPD(lote, registros, dataFim);
        assertEquals(0.05, gpd, 0.0001);
    }

    @Test
    public void testCalcularFatorProducao() {
        // Viabilidade = 95%
        // GPD = 0.05 kg/dia
        // CA = 1.88679
        // FP = (95 * 0.05) / (1.88679 * 10) = 4.75 / 18.8679 = 0.2517...
        
        registros.add(new RegistroEntity(1, "u1", dataInicio, 50, 3500.0, 2.0, 2.0, "milho"));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataInicio);
        cal.add(Calendar.DAY_OF_YEAR, 39);
        Date dataFim = cal.getTime();

        // Recalculando GPD para este cenário:
        // Peso médio no registro é 2.0. Peso inicial 0.045. Idade 40.
        // GPD = (2.0 - 0.045) / 40 = 1.955 / 40 = 0.048875
        // Viabilidade = (950 / 1000) * 100 = 95%
        // CA = 3500 / (950 * 2.0 - 45) = 3500 / 1855 = 1.886792
        // FP = (95 * 0.048875) / (18.86792) = 4.643125 / 18.86792 = 0.24608
        
        double fp = ZootecniaCalculator.calcularFatorProducao(lote, registros, dataFim);
        assertEquals(0.24608, fp, 0.0001);
    }
}
