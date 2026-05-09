# Documento de Requisitos

**Documento Elaborado por:** 

---

## DOCUMENTO DE REQUISITOS: Documento de Requisitos

### INTRODUÇÃO

#### Propósito
O propósito deste documento é formalizar os requisitos funcionais e não-funcionais do aplicativo **AviFácil**, um sistema de gestão avícola desenvolvido para dispositivos Android. O objetivo principal é fornecer ao produtor rural uma ferramenta robusta para o acompanhamento técnico de lotes de frangos de corte, garantindo a integridade dos dados mesmo em ambientes com conectividade limitada.

#### Escopo
O sistema abrange a autenticação de usuários, o gerenciamento de perfis de avicultores e suas propriedades, a administração de lotes (abertura, acompanhamento e encerramento) e o registro diário de indicadores de desempenho (mortalidade, consumo de ração e peso). Inclui também uma arquitetura de sincronização automática com a nuvem (Firebase).

#### Definições, Acrônimos e Abreviações
*   **Room:** Biblioteca de persistência de dados local para Android.
*   **Firestore:** Banco de dados NoSQL na nuvem do Firebase.
*   **UUID:** *Universally Unique Identifier* (Identificador Único Universal), usado para chaves primárias globais.
*   **Offline-first:** Estratégia de desenvolvimento onde a aplicação é totalmente funcional sem internet, sincronizando os dados posteriormente.
*   **WorkManager:** API Android para execução de tarefas em segundo plano garantidas.
*   **Soft Delete:** Técnica de exclusão lógica onde os dados não são removidos fisicamente de imediato para permitir sincronização de deleção.

### Organização da Especificação de Requisitos de Software
Este documento está organizado em seções que descrevem as funções do produto, as características dos usuários, os requisitos específicos de dados e os atributos de qualidade como desempenho, confiabilidade e usabilidade.

### Descrição Geral do Aplicativo
O AviFácil funciona como um assistente digital de campo. Ele permite que o avicultor registre informações críticas diretamente no galpão, visualize indicadores de desempenho em tempo real através de um dashboard e mantenha um backup seguro de todas as informações na nuvem.

### Funções do Produto
1.  **Gestão de Identidade:** Cadastro e login de usuários via Firebase Auth.
2.  **Gestão de Avicultores:** Registro de nome e nome da propriedade rural.
3.  **Controle de Lotes:** Criação de lotes com dados de linhagem, quantidade inicial e peso de alojamento.
4.  **Lançamentos Diários:** Registro de mortalidade, consumo de ração e pesagens amostrais.
5.  **Monitoramento:** Dashboard com cálculos automáticos de mortalidade acumulada e aves vivas.
6.  **Sincronização de Dados:** Upload automático de dados locais para o Firebase Firestore.

### Características do Usuário
O sistema é destinado a produtores de aves de corte (avicultores) e técnicos extensionistas que necessitam de mobilidade e rapidez na coleta de dados, muitas vezes em locais com baixa cobertura de rede móvel.

### Suposições e Dependências
*   O dispositivo deve possuir sistema operacional Android (SDK 24 ou superior).
*   Dependência do Google Play Services para o funcionamento do Firebase.
*   Necessidade de conexão ocasional à internet para sincronização e backup.

---

### REQUISITOS ESPECÍFICOS

#### Requisitos Funcionais

##### Requisitos para Inclusão, Exclusão, Modificação e Manutenção dos Dados
*   **RF001 - Autenticação:** O sistema deve permitir a criação de conta e login utilizando e-mail e senha.
*   **RF002 - Perfil do Produtor:** O sistema deve permitir incluir e editar os dados do avicultor e da propriedade.
*   **RF003 - Cadastro de Lote:** O sistema deve permitir a inclusão de novos lotes, impedindo números de lotes duplicados para o mesmo produtor.
*   **RF004 - Registro de Dados Diários:** O sistema deve permitir a inclusão de registros vinculados a um lote, contendo data, aves mortas, consumo de ração e peso médio.
*   **RF005 - Edição e Exclusão:** O sistema deve permitir a alteração e exclusão lógica (soft delete) de registros diários para manter a integridade entre local e nuvem.
*   **RF006 - Encerramento de Lote:** O sistema deve permitir marcar um lote como "Encerrado", impedindo novos lançamentos diários após esta ação.

#### Requisitos de Patrimônio
*   **Integridade de Dados:** O sistema deve garantir que nenhum dado seja perdido durante a sincronização, utilizando transações locais (Room) e confirmações de escrita no Firestore.
*   **Estabilidade de IDs:** O uso de UUIDs garante que os registros criados offline não sofram colisões ao serem integrados à base de dados global na nuvem.

#### Requisitos para Uso da Pesquisa por Itens de Informação
*   **Busca de Lotes:** O sistema deve permitir a visualização de lotes filtrados por status (Ativos ou Encerrados).
*   **Histórico de Registros:** O usuário deve ser capaz de visualizar todos os registros diários em ordem cronológica dentro de cada lote.

#### Requisitos para Consultas Gerais e Emissão de Relatórios
*   **Dashboard de Indicadores:** O sistema deve apresentar na tela principal:
    *   Total de aves alojadas no período.
    *   Percentual de mortalidade geral acumulada.
    *   Contagem de lotes ativos vs. encerrados.
    *   Lista rápida dos últimos 5 lotes movimentados.
*   **Cálculo de Desempenho:** O sistema deve calcular automaticamente a viabilidade do lote e a Conversão Alimentar (C.A.) baseada nos registros inseridos.

---

### REQUISITOS DE DESEMPENHO
*   **Latência Local:** Operações de escrita e leitura no banco de dados local (Room) devem ser concluídas em menos de 100ms para garantir fluidez.
*   **Sincronização em Background:** O processo de sincronização não deve interferir no uso da interface do usuário (UI), sendo executado em threads separadas.

### Atributos do Sistema de Software

#### Confiabilidade
*   O aplicativo deve garantir a persistência dos dados mesmo em caso de fechamento inesperado do sistema.
*   Mecanismo de tentativa (*retry*) em caso de falha na sincronização por falta de internet.

#### Usabilidade
*   Interface otimizada para uso com uma mão, considerando o ambiente de campo.
*   Feedback visual claro sobre o estado da sincronização (Sincronizando/Sincronizado).
*   Mensagens de erro amigáveis para orientar o usuário em campos obrigatórios não preenchidos.
