# 🐥 AviFácil - Gestão Avícola

O **AviFácil** é uma solução inteligente para o monitoramento e gestão de lotes de frangos de corte. Desenvolvido para atender tanto o produtor no campo quanto o gestor técnico, o sistema combina a agilidade de um aplicativo Android offline-first com o poder analítico de um painel web em tempo real.

---

## 📊 Cálculos Zootécnicos

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

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
