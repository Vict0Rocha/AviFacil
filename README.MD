# 🐥 AviFácil - Gestão Avícola Inteligente

O **AviFácil** é uma plataforma completa para o acompanhamento técnico de lotes de frangos de corte. O sistema foi projetado para funcionar em ambientes com conectividade limitada, garantindo a integridade dos dados e auxiliando o produtor na tomada de decisão.

## 📊 Documentação de Cálculos Zootécnicos

O motor de cálculo do AviFácil (`ZootecniaCalculator.java`) segue os padrões internacionais de avicultura de corte (Linhagens Cobb/Ross). Abaixo estão detalhadas as fórmulas utilizadas, suas unidades e precisão.

### 1. Indicadores de Crescimento e Sobrevivência

*   **Idade do Lote**: Calculada como a diferença em dias entre a data do registro e a data de alojamento.
    *   **Fórmula**: `Data do Registro - Data de Alojamento`
    *   **Unidade**: Dias (`int`).
*   **Mortalidade Acumulada (%)**: Percentual total de aves perdidas em relação ao alojamento inicial.
    *   **Fórmula**: `(Total de Aves Mortas / Quantidade Inicial) * 100`
    *   **Unidade**: Percentual (`double`), precisão de 2 casas.
*   **Viabilidade (%)**: Percentual de aves vivas em relação ao alojamento inicial.
    *   **Fórmula**: `(Aves Vivas Atuais / Quantidade Inicial) * 100`
    *   **Unidade**: Percentual (`double`), precisão de 2 casas.
*   **Peso Médio Atual (kg)**: Conversão do peso médio informado (em gramas) para quilogramas.
    *   **Fórmula**: `Peso Médio (g) / 1000`
    *   **Unidade**: Quilogramas (`kg`), precisão de 3 casas.

### 2. Eficiência Alimentar e Desempenho

*   **Consumo Total de Ração (kg)**: Soma acumulada de toda a ração fornecida ao lote desde o primeiro dia.
    *   **Fórmula**: `Σ(Consumo de Ração do Período)`
    *   **Unidade**: Quilogramas (`kg`).
*   **Conversão Alimentar (C.A.)**: Indica quantos quilos de ração são necessários para produzir 1kg de peso vivo. É o principal índice de eficiência econômica.
    *   **Fórmula**: `Consumo Total (kg) / (Peso Médio Atual (kg) * Aves Vivas)`
    *   **Unidade**: Proporção (`double`), **Padrão de Mercado: 3 casas decimais**.
*   **Ganho de Peso Diário (GPD)**: Média de peso que cada ave ganhou por dia de vida.
    *   **Fórmula**: `(Peso Médio Atual (g) - Peso Inicial (g)) / Idade (Dias)`
    *   **Unidade**: Gramas/dia (`g/dia`), precisão de 2 casas.
*   **Índice de Eficiência Produtiva (IEP / Fator de Produção)**: Nota global do lote que pondera peso, viabilidade, idade e conversão.
    *   **Fórmula**: `((Peso Médio (kg) * Viabilidade (%)) / (Idade * C.A.)) * 100`
    *   **Unidade**: Índice (`double`), precisão de 2 casas.

### 3. Análise Financeira

*   **Custo Total de Ração**: Valor total investido em alimentação até o momento.
    *   **Fórmula**: `Σ(Consumo do Período * Preço da Ração no Período)`
    *   **Unidade**: Moeda (`R$`), precisão de 2 casas.
*   **Custo de Ração por Ave**: Quanto foi gasto em ração para cada ave que permanece viva no galpão.
    *   **Fórmula**: `Custo Total de Ração / Aves Vivas Atuais`
    *   **Unidade**: Moeda (`R$`), precisão de 2 casas.
*   **Custo de Ração por kg de Frango**: Custo alimentar para produzir cada quilo de carne viva.
    *   **Fórmula**: `C.A. * Preço Médio da Ração (kg)`
    *   **Unidade**: Moeda (`R$`), precisão de 2 casas.

---

## 🏗️ Estrutura do Projeto

O projeto é dividido em três componentes principais:

1.  **📱 Android App (`/app`)**: Aplicativo nativo em Java focado na coleta de dados em campo, com suporte a modo offline e sincronização via WorkManager. Realiza cálculos zootécnicos localmente.
2.  **💻 Web Dashboard (`/web-site`)**: Painel administrativo em React para análise de dados e visualização de indicadores de desempenho (BI). Consome dados diretamente do Firestore e processa métricas no lado do cliente.

## 🛠️ Tecnologias Utilizadas

*   **Android**: Java, Room Database, ViewModel, WorkManager.
*   **Front-end**: React + Vite, Firebase Auth, Firestore SDK.
*   **Banco de Dados**: Cloud Firestore (NoSQL).
*   **Plano**: Firebase Spark (100% gratuito).

## 🚀 Como Iniciar

### Pré-requisitos
*   Node.js (versão 20 ou superior)
*   Firebase CLI (`npm install -g firebase-tools`)
*   Android Studio

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
