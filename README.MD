# 🐥 AviFácil - Gestão Avícola Inteligente

O **AviFácil** é uma plataforma completa para o acompanhamento técnico de lotes de frangos de corte. O sistema foi projetado para funcionar em ambientes com conectividade limitada, garantindo a integridade dos dados e auxiliando o produtor na tomada de decisão.

## 🏗️ Estrutura do Projeto

O projeto é dividido em três componentes principais:

1.  **📱 Android App (`/app`)**: Aplicativo nativo em Java/Kotlin focado na coleta de dados em campo, com suporte a modo offline e sincronização via WorkManager.
2.  **⚙️ Cloud Functions (`/functions`)**: Back-end em Node.js rodando em Firebase Functions. Centraliza toda a **lógica de negócio** e cálculos complexos para garantir segurança e consistência.
3.  **💻 Web Dashboard (`/web-site`)**: Painel administrativo em React para análise de dados e visualização de indicadores de desempenho (BI).

## 🛠️ Tecnologias Utilizadas

*   **Android**: Java, Room Database, ViewModel, WorkManager.
*   **Back-end**: Node.js, Firebase Cloud Functions, Firebase Admin SDK.
*   **Front-end**: React + Vite, Firebase Auth.
*   **Banco de Dados**: Cloud Firestore (NoSQL).

## 🚀 Como Iniciar

### Pré-requisitos
*   Node.js (versão 20 ou superior)
*   Firebase CLI (`npm install -g firebase-tools`)
*   Android Studio

### Desenvolvimento Local (Emuladores)
Para testar o ecossistema completo localmente:
1.  Na raiz do projeto, inicie os emuladores:
    ```bash
    firebase emulators:start
    ```
2.  Abra o painel do emulador em `localhost:4000`.

### Configuração do Web Dashboard
1.  Entre na pasta: `cd web-site`
2.  Crie um arquivo `.env` baseado nas suas credenciais do Firebase.
3.  Instale dependências e rode:
    ```bash
    npm install
    npm run dev
    ```

## 🔒 Segurança
Este repositório segue boas práticas de segurança:
*   Arquivos `google-services.json` e chaves de API estão no `.gitignore`.
*   Toda a regra de negócio está protegida no lado do servidor (Functions).

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
