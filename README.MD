# 🐥 AviFácil - Gestão Avícola

O **AviFácil** é uma solução inteligente para o monitoramento e gestão de lotes de frangos de corte. Desenvolvido para atender tanto o produtor no campo quanto o gestor técnico, o sistema combina a agilidade de um aplicativo Android offline-first com o poder analítico de um painel web em tempo real.

---

## 📱 O Aplicativo (Android)

O app mobile é a ferramenta de campo do produtor. 
- **Modo Offline**: Funciona sem internet. Os dados são salvos localmente em um banco de dados **Room** (v15) e sincronizados automaticamente quando houver conexão.
- **Teclado Otimizado**: Todos os campos numéricos (Peso, Consumo, Preço) abrem automaticamente o teclado numérico.
- **Localização Brasileira**: Suporte completo para entrada de números com **vírgula** (ex: `1,250`), adaptado ao padrão nacional.
- **Cálculos em Tempo Real**: O produtor vê o desempenho do lote (GPD, C.A., IEP) instantaneamente após cada registro diário.

## 💻 O Painel Web (Dashboard)

Uma interface React integrada ao **Firebase** que consolida os dados de todos os avicultores.
- **Visão Geral**: Acompanhamento de múltiplos lotes simultaneamente.
- **Transparência**: Fórmulas idênticas às do aplicativo, garantindo que o técnico e o produtor falem a mesma língua.

---

## 📊 Documentação de Cálculos Zootécnicos

O motor de cálculo segue os padrões da indústria (Cobb/Ross).

### Fórmulas Principais:
*   **GPD (Ganho de Peso Diário)**: `(Peso Atual (g) - Peso Inicial (g)) / Idade` (em gramas).
*   **C.A. (Conversão Alimentar)**: `Consumo Total (kg) / Peso Vivo Total (kg)`. (3 casas decimais).
*   **IEP (Índice de Eficiência Produtiva)**: `((GPD(kg) * Viabilidade(%)) / C.A.) * 100`.
    *   *Nota: O resultado é arredondado para **0 casas decimais**, conforme padrão de mercado.*
*   **Viabilidade**: `(Aves Vivas / Aves Alojadas) * 100`.

---

## 🏗️ Estrutura do Projeto

*   `/app`: Projeto Android Studio (Java).
*   `/web-site`: Projeto React + Vite (Javascript).
*   `firestore.rules`: Regras de segurança para o banco de dados na nuvem.

## 🛠️ Tecnologias
- **Mobile**: Java, Room (SQLite local), WorkManager, Retrofit.
- **Web**: React, Tailwind CSS, Firebase (Auth & Firestore).
- **Backend**: Serverless via Firebase.

---

## 🚀 Como Rodar o Projeto

### 1. Painel Web
1. Entre na pasta: `cd web-site`
2. Instale as dependências: `npm install`
3. Inicie o servidor de desenvolvimento: `npm run dev`
4. Acesse: `http://localhost:5173`

### 2. Aplicativo Android
1. Abra a pasta raiz no **Android Studio**.
2. Certifique-se de que o Google Services (`google-services.json`) está configurado na pasta `/app`.
3. Sincronize o Gradle.
4. Rode no Emulador ou Dispositivo Físico (API 26+).

> **Aviso de Migração**: Este projeto utiliza a versão **15** do banco de dados local (`avifacil_pro_v15_db`). Caso instale sobre uma versão muito antiga e encontre erros, limpe o cache do app ou reinstale-o.

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
