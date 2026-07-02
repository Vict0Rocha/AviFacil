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

## 🔒 Política de Privacidade — AviFácil

**Última atualização: 21 de maio de 2024**

Esta Política de Privacidade descreve como o aplicativo AviFácil trata dados pessoais e informações operacionais dos usuários. O AviFácil é uma solução voltada à gestão avícola, permitindo o cadastro de avicultores, propriedades, lotes de aves, registros produtivos e indicadores zootécnicos.

Ao utilizar o AviFácil, você declara estar ciente das práticas descritas nesta Política.

### 1. Responsável pelo tratamento dos dados
O responsável pelo tratamento dos dados pessoais no AviFácil é:
*   **Responsável/Desenvolvedor:** Cristiano (Gestão AviFácil)
*   **E-mail de contato:** cristianoadm@gmail.com
*   **Aplicativo:** AviFácil
*   **País:** Brasil

Este contato deve ser utilizado para solicitações relacionadas à privacidade, acesso, correção, exclusão ou dúvidas sobre o tratamento de dados.

### 2. Dados que coletamos
O AviFácil pode coletar e tratar os seguintes dados:

**2.1 Dados de conta e autenticação**
Para permitir o acesso ao aplicativo, autenticação e recuperação de senha, podemos tratar:
- nome do usuário;
- endereço de e-mail;
- identificador único da conta (UUID);
- senha, quando utilizada para login, sendo gerenciada pelo serviço de autenticação (Firebase Auth);
- informações de sessão e status de autenticação;
- dados técnicos necessários para segurança do login.

**2.2 Dados do perfil do avicultor**
Para identificação e organização do uso do sistema, podemos coletar:
- nome do avicultor;
- e-mail;
- nome da propriedade;
- status do perfil;
- indicação de perfil completo ou incompleto;
- status de bloqueio ou ativação da conta;
- datas de atualização e sincronização.

**2.3 Dados de lotes avícolas**
Para permitir a gestão da produção, o aplicativo pode armazenar:
- número do lote, linhagem e galpão;
- data de início do lote, quantidade inicial de aves e peso inicial;
- indicadores calculados: peso médio atual, conversão alimentar, mortalidade acumulada, viabilidade, ganho de peso diário e índice de eficiência produtiva;
- observações inseridas pelo usuário e status do lote.

**2.4 Dados de registros produtivos**
Durante o uso do aplicativo, o usuário pode registrar informações como:
- data do registro;
- quantidade de aves mortas no período;
- consumo de ração;
- peso médio atual;
- preço por quilo de insumo e tipo de insumo;
- observações operacionais.

**2.5 Dados técnicos e de segurança**
Podemos tratar dados técnicos necessários ao funcionamento, segurança e sincronização do aplicativo, como:
- identificadores internos da conta;
- informações de autenticação;
- registros de sincronização;
- data e hora de atualização;
- informações técnicas processadas por serviços de terceiros utilizados no app, como Firebase.

### 3. Como usamos os dados
Os dados são utilizados para:
- permitir login e autenticação do usuário;
- criar e manter o perfil do avicultor;
- cadastrar e gerenciar lotes;
- registrar informações diárias de produção;
- calcular indicadores zootécnicos automáticos;
- exibir dados no aplicativo e no painel web;
- sincronizar informações entre o dispositivo e a nuvem;
- permitir uso offline com posterior sincronização;
- proteger contas contra acesso indevido;
- corrigir erros, melhorar estabilidade e manter a segurança.

### 4. Armazenamento local e sincronização em nuvem
O AviFácil possui funcionamento **offline-first**. Isso significa que as informações são salvas primeiro no próprio dispositivo do usuário (Room/SQLite) e depois sincronizadas com a nuvem (**Google Firebase/Firestore**) quando houver conexão disponível.

### 5. Compartilhamento de dados
O AviFácil não vende dados pessoais dos usuários. Os dados podem ser compartilhados apenas com provedores de tecnologia necessários para funcionamento do app (Google Firebase), com administradores autorizados para suporte ou quando exigido por lei.

### 6. Serviços de terceiros
O AviFácil utiliza serviços de terceiros para autenticação, armazenamento e sincronização de dados, incluindo:
- Firebase Authentication;
- Firebase Firestore;
- Serviços Google relacionados à infraestrutura Firebase.

### 7. Retenção dos dados
Os dados serão mantidos pelo tempo necessário para o funcionamento da conta, prestação das funcionalidades do aplicativo e manutenção de histórico produtivo. Quando o usuário solicita a exclusão da conta, o AviFácil removerá os dados aplicáveis, salvo obrigações legais de retenção.

### 8. Segurança dos dados
Adotamos medidas técnicas e organizacionais para proteger os dados, incluindo autenticação de usuários, regras de acesso ao banco de dados em nuvem e separação de dados por usuário.

### 9. Direitos do titular dos dados
Nos termos da **LGPD**, o usuário pode solicitar confirmação de tratamento, acesso, correção ou exclusão de seus dados pessoais através do e-mail: **cristianoadm@gmail.com**.

### 10. Transferência internacional de dados
Como utilizamos a infraestrutura do Firebase/Google, os dados podem ser armazenados ou processados em servidores localizados fora do Brasil, observando as medidas legais de proteção.

---
*AviFácil - Otimizando a produção avícola de ponta a ponta.*
