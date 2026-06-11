# 🐥 AviFácil - Gestão Avícola Inteligente

O **AviFácil** é uma plataforma completa para o acompanhamento técnico de lotes de frangos de corte. O sistema foi projetado para funcionar em ambientes com conectividade limitada, garantindo a integridade dos dados e auxiliando o produtor na tomada de decisão.

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

### Desenvolvimento Local (Emuladores)
Para testar o ecossistema completo localmente:
1.  Na raiz do projeto, inicie os emuladores:
    ```bash
    firebase emulators:start
    ```
2.  Abra o painel do emulador em `localhost:4000`.

### Configuração do Web Dashboard
1.  Entre na pasta: `cd web-site`
2.  Instale dependências e rode:
    ```bash
    npm install
    npm run dev
    ```

## 🔒 Segurança
Este repositório segue boas práticas de segurança:
*   Arquivos `google-services.json` e chaves de API estão no `.gitignore`.
*   Regras de segurança do Firestore garantem que produtores acessem apenas seus próprios dados.

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
