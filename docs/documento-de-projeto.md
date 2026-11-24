# Documento de Projeto de Software — HelpDesk

**Pontificia Universidade Catolica de Campinas — PUC-Campinas**  
**Curso:** Sistemas de Informacao  
**Disciplina:** Projeto Integrador IV (2026-2)  
**Turma:** 0102 | **Grupo:** Grupo 02  

---

### Titulo do Projeto
**HelpDesk — Sistema Corporativo de Gerenciamento de Chamados de TI com Notificacoes em Tempo Real**

### Subtitulo
*Arquitetura Orientada a Eventos em Java 21 Puro, WebSocket RFC 6455, REST API, Neon PostgreSQL e SPA Vanilla*

### Autores / Integrantes da Equipe
- **Caua Vasconcelos Grecco De Faria** — RA: 25006367
- **Rafael Trevisan** — RA: 25002001
- **Vinicius Fortes Heinzl** — RA: 25008058
- **Pedro Henrique Vieira Lima** — RA: 25018202
- **Kaue Rodrigues Seixas** — RA: 23011884
- **Lucas Curti Rodrigues Pinto** — RA: 25001822

**Campinas - SP**  
**Novembro de 2026**

---

## Sumario

- [1. Introducao](#1-introducao)
  - [1.1 Contexto e Justificativa](#11-contexto-e-justificativa)
  - [1.2 Problema](#12-problema)
  - [1.3 Objetivos (Geral e Especificos)](#13-objetivos-geral-e-especificos)
  - [1.4 Publico-Alvo e Personas](#14-publico-alvo-e-personas)
- [2. Descricao da Solucao](#2-descricao-da-solucao)
  - [2.1 Visao Geral do Produto](#21-visao-geral-do-produto)
  - [2.2 Principais Funcionalidades](#22-principais-funcionalidades)
  - [2.3 Diferenciais e Proposta de Valor](#23-diferenciais-e-proposta-de-valor)
- [3. Metodologia e Processo de Desenvolvimento](#3-metodologia-e-processo-de-desenvolvimento)
  - [3.1 Metodologia de Trabalho](#31-metodologia-de-trabalho)
  - [3.2 Papeis e Responsabilidades da Equipe](#32-papeis-e-responsabilidades-da-equipe)
  - [3.3 Ferramentas de Gestao e Comunicacao](#33-ferramentas-de-gestao-e-comunicacao)
- [4. Arquitetura e Tecnologias](#4-arquitetura-e-tecnologias)
  - [4.1 Visao Geral da Arquitetura e Diagrama C4 (Nivel 2)](#41-visao-geral-da-arquitetura-e-diagrama-c4-nivel-2)
  - [4.2 Frontend (SPA Vanilla)](#42-frontend-spa-vanilla)
  - [4.3 Backend API (Java 21 Puro REST)](#43-backend-api-java-21-puro-rest)
  - [4.4 Backend Socket (Java 21 Puro RFC 6455)](#44-backend-socket-java-21-puro-rfc-6455)
  - [4.5 Banco de Dados e Armazenamento (PostgreSQL no Neon)](#45-banco-de-dados-e-armazenamento-postgresql-no-neon)
  - [4.6 Infraestrutura, Conteineres e Deploy](#46-infraestrutura-conteineres-e-deploy)
- [5. Modelagem de Dados e Banco de Dados](#5-modelagem-de-dados-e-banco-de-dados)
  - [5.1 Modelo Conceitual (DER)](#51-modelo-conceitual-der)
  - [5.2 Modelo Logico / Relacional](#52-modelo-logico--relacional)
  - [5.3 Dicionario de Dados Detalhado](#53-dicionario-de-dados-detalhado)
- [6. Historias de Usuario e Criterios de Aceite](#6-historias-de-usuario-e-criterios-de-aceite)
  - [6.1 Historias de Usuario (Matriz MoSCoW)](#61-historias-de-usuario-matriz-moscow)
  - [6.2 Criterios de Aceite (BDD / Gherkin)](#62-criterios-de-aceite-bdd--gherkin)
- [7. Casos de Uso e Diagramas de Atividades](#7-casos-de-uso-e-diagramas-de-atividades)
  - [7.1 Mapeamento dos Casos de Uso Principais (Entregaveis E1 a E9)](#71-mapeamento-dos-casos-de-uso-principais-entregaveis-e1-a-e9)
  - [7.2 Diagramas de Sequencia dos Fluxos Criticos](#72-diagramas-de-sequencia-dos-fluxos-criticos)
- [8. Qualidade e Testes de Software](#8-qualidade-e-testes-de-software)
  - [8.1 Estrategia de Testes](#81-estrategia-de-testes)
  - [8.2 Testes Automatizados com JUnit 5 no Backend Socket](#82-testes-automatizados-com-junit-5-no-backend-socket)
  - [8.3 Analise Estatica de Codigo com SonarCloud](#83-analise-estatica-de-codigo-com-sonarcloud)
  - [8.4 Testes de Integracao, Concorrencia e Testes Manuais](#84-testes-de-integracao-concorrencia-e-testes-manuais)
- [9. Manual de Implantacao e Uso](#9-manual-de-implantacao-e-uso)
- [10. Resultados e Discussao](#10-resultados-e-discussao)
  - [10.1 Funcionalidades Entregues vs. Planejadas](#101-funcionalidades-entregues-vs-planejadas)
  - [10.2 Desafios Tecnicos e Storyline de Superacao](#102-desafios-tecnicos-e-storyline-de-superacao)
  - [10.3 Avaliacao do Processo e Dinamica da Equipe](#103-avaliacao-do-processo-e-dinamica-da-equipe)
- [11. Conclusao e Trabalhos Futuros](#11-conclusao-e-trabalhos-futuros)
  - [11.1 Conclusao](#111-conclusao)
  - [11.2 Limitacoes Conhecidas](#112-limitacoes-conhecidas)
  - [11.3 Trabalhos Futuros](#113-trabalhos-futuros)
- [Apendice A — Registro de Decisoes de Arquitetura (ADRs)](#apendice-a--registro-de-decisoes-de-arquitetura-adrs)
- [Apendice B — Historico Semantico de Versoes (Changelog)](#apendice-b--historico-semantico-de-versoes-changelog)

---

## 1. Introducao

**Responsavel pelo capitulo:** Rafael Trevisan


### 1.1 Contexto e Justificativa
A gestao eficiente de suporte de Tecnologia da Informacao e um fator critico para a continuidade operacional de organizacoes contemporaneas. No cotidiano corporativo e academico, falhas em equipamentos, indisponibilidade de servicos de rede, problemas com credenciais e interrupcoes de software geram quedas severas de produtividade. 

Historicamente, canais informais como e-mails, comunicadores instantaneos e solicitacoes verbais resultam em perda de chamados, ausencia de rastreabilidade, retrabalho e falta de indicadores de desempenho (SLA). O desenvolvimento de uma solucao integrada, acessivel e com notificacao em tempo real justifica-se pela necessidade de centralizar a comunicacao, conferir visibilidade imediata a equipe de suporte e fornecer aos solicitantes um canal confiavel para acompanhamento do ciclo de vida de suas demandas.

### 1.2 Problema
Muitas ferramentas de Help Desk tradicionais no mercado sao excessivamente complexas, onerosas e dependentes de arquiteturas legadas baseadas em polling periodico (onde o navegador requisita o servidor a cada poucos segundos para verificar novidades), sobrecarregando a infraestrutura de rede e bancos de dados. Alem disso, do ponto de vista academico no ambito do Projeto Integrador IV, o desafio consistiu em projetar e implementar um sistema completo e distribuido sem recorrer a abstrações prontas de frameworks web como Spring Boot ou Netty, construindo os protocolos de comunicacao REST e WebSocket diretamente com **Java 21 Puro**.

### 1.3 Objetivos (Geral e Especificos)

#### Objetivo Geral:
Projetar, construir, testar e publicar em ambiente de nuvem um sistema corporativo completo de gerenciamento de chamados de TI, dotado de interface web responsiva, controle de acesso baseado em papeis e departamentos (RBAC), API RESTful robusta e um servico dedicado de mensageria em tempo real baseado na norma RFC 6455 do protocolo WebSocket.

#### Objetivos Especificos:
1. Implementar a camada de apresentacao utilizando HTML5, CSS3 Vanilla e JavaScript ES6+ no modelo Single Page Application (SPA), garantindo autonomia e usabilidade sem bibliotecas de terceiros.
2. Desenvolver a API RESTful em Java 21 Puro utilizando o pacote `com.sun.net.httpserver`, estabelecendo persistencia transacional com banco de dados PostgreSQL 15+ via JDBC nativo.
3. Projetar e implementar um servidor de WebSocket em Java 21 Puro em conformidade rigorosa com a RFC 6455, efetuando manualmente o aperto de mao (handshake HTTP 101), codificacao/decodificacao de frames binarios mascarados e despacho concorrente multithread.
4. Integrar o publicador de eventos assincrono entre a API e o Socket (Contrato CT18) e o broadcast seguro para os clientes (Contrato CT22), suportando o ciclo completo CRUD em tempo real (`CREATED`, `UPDATED`, `DELETED`).
5. Estabelecer esteira de integracao continua (CI/CD) com analise estatica de codigo via SonarCloud e execucao automatizada de testes unitarios com JUnit 5.
6. Publicar a solucao integral em ambiente de nuvem (Vercel, Render e Neon DB) com suporte a resiliencia contra cold start.

### 1.4 Publico-Alvo e Personas

- **Persona 1: Carlos Andrade (Funcionario Solicitante / Cliente)**
  - *Perfil:* Analista Financeiro que depende de computadores, planilhas e impressoras para fechamento mensal.
  - *Necessidade:* Precisa abrir chamados de forma rapida, acompanhar o status sem precisar cobrar o suporte por mensagem e visualizar solicitacoes abertas por seus colegas de departamento.
- **Persona 2: Beatriz Souza (Tecnica de Suporte de TI / Support)**
  - *Perfil:* Tecnica de nivel 1 e 2 responsavel pela triagem, diagnostico e resolucao das ocorrencias de infraestrutura.
  - *Necessidade:* Precisa de um painel unificado (Kanban e Lista), receber alertas visuais e sonoros instantaneos assim que um chamado for aberto e assumir o atendimento de maneira atomica para evitar duplicidade com outros tecnicos.
- **Persona 3: Roberto Mendes (Coordenador de TI / Administrador)**
  - *Perfil:* Gestor responsavel pela governanca de TI, distribuicao de carga de trabalho e seguranca de dados.
  - *Necessidade:* Gerenciar usuarios e permissoes, monitorar metricas consolidadas em dashboard e auditar o historico de atendimento.

---

## 2. Descricao da Solucao

**Responsavel pelo capitulo:** Vinicius Fortes Heinzl


### 2.1 Visao Geral do Produto
O **HelpDesk** e uma plataforma web distribuida composta por tres camadas desacopladas: uma Single Page Application cliente, uma API REST transacional de negocio e um servidor de WebSocket concorrente para atualizacao em tempo real. Os dados sao consolidados em um banco relacional PostgreSQL serverless na nuvem.

### 2.2 Principais Funcionalidades
1. **Autenticacao e Controle de Acesso (RBAC):** Login com emissao de tokens HMAC-SHA256, auto-cadastro de funcionarios e perfis de permissao (`CLIENT`, `SUPPORT`, `ADMIN`).
2. **Politica de Isolamento por Departamento (`DepartmentPolicy`):** Os usuarios visualizam e recebem alertas restritos ao seu departamento corporativo (ex: Financeiro, RH, Operacoes). O departamento de `TI` possui visao administrativa global de todos os chamados da empresa.
3. **Abertura e Triagem de Chamados:** Formulario intuitivo com selecao de categorias tecnicas, prioridade e descricao detalhada.
4. **Ciclo de Vida Transacional:** Transicao controlada entre estados: `NEW` (Novo / Aguardando TI) -> `ASSIGNED` (Em Atendimento) -> `CLOSED` (Resolvido) ou `UNRESOLVED` (Nao Resolvido).
5. **Atribuicao Atomica (`startService`):** Mecanismo baseado em Common Table Expressions (CTE) no PostgreSQL que garante que, se dois tecnicos tentarem assumir o mesmo chamado simultaneamente, apenas um tera sucesso e o outro recebera indicacao de conflito.
6. **Timeline Cronologica Auditavel:** Registro historico de todas as mudancas de estado, mensagens tecnicas e apontamentos.
7. **Notificacoes em Tempo Real (Push Ping):** Alertas imediatos com badge visual no cabeçalho e sinal sonoro sutil quando novos chamados sao criados ou alterados.
8. **Visualizacao Dual:** Alternancia dinamica entre exibicao em Lista Tabular e Quadro Kanban interativo.
9. **Dashboard de Indicadores:** Metricas em tempo real de chamados abertos, chamados resolvidos, distribuicao por prioridade e tempo medio de resolucao.

### 2.3 Diferenciais e Proposta de Valor
- **Ausencia de Dependencias Pesadas:** Execucao enxuta com Java 21 Puro, demonstrando alto desempenho com consumo minimo de memoria em comparacao com frameworks convencionais.
- **Eliminacao de Polling:** Substituicao completa de requisicoes HTTP repetitivas por conexao WebSocket bidirecional permanente, reduzindo a latencia de comunicacao para milissegundos e economizando recursos computacionais no banco de dados.
- **Resiliencia Operacional:** Mecanismo de Heartbeat nativo via Ping/Pong contra encerramentos por inatividade em proxies reversos, alem de reconciliacao automatica de dados no cliente web apos reconexoes.

---

## 3. Metodologia e Processo de Desenvolvimento

**Responsavel pelo capitulo:** Pedro Henrique Vieira Lima


### 3.1 Metodologia de Trabalho
A equipe adotou uma adaptacao agil combinando praticas do Scrum e do Kanban. O ciclo de vida foi estruturado em sprints curtas orientadas aos entregaveis formais do Projeto Integrador IV:
- **Sprint 1:** Concepcao, levantamento de requisitos, definicao de personas e modelagem do banco relacional.
- **Sprint 2:** Prototipacao da interface SPA (HTML/CSS) e arquitetura inicial da API REST em Java Puro.
- **Sprint 3:** Implementacao dos DAOs, regras de negocio, politicas de departamento e integracao full-stack com Neon DB.
- **Sprint 4:** Construcao do servidor WebSocket RFC 6455 em Java Puro e desenvolvimento dos contratos de integracao CT18, CT19 e CT22.
- **Sprint 5:** Integracao dos testes automatizados com JUnit 5 no Backend Socket, pipeline CI/CD no SonarCloud, refinamento de resiliencia em nuvem e documentacao final.

### 3.2 Papeis e Responsabilidades da Equipe
- **Caua Vasconcelos Grecco De Faria:** Desenvolvimento do Backend Socket (Implementacao do protocolo WebSocket RFC 6455 em Java 21 Puro, ServerSocket, manipulacao de frames binarios, estruturas concorrentes thread-safe e integracao CI/CD com SonarCloud).
- **Lucas Curti Rodrigues Pinto:** Testes e Automacao do Backend Socket (Desenvolvimento da suite de testes com JUnit 5 no modulo de socket, construcao dos mocks em memoria ServidorDeTeste, ClienteWs e UserDaoFalso, e automacao de testes).
- **Kaue Rodrigues Seixas:** Desenvolvimento do Backend API e Banco de Dados (Modelagem relacional SQL no Neon DB, desenvolvimento dos DAOs com JDBC Puro, handlers HTTP REST, transacoes atomicas CTE e politicas de departamento).
- **Rafael Trevisan:** Desenvolvimento do Backend API (Regras de negocio da API REST, controle de acesso RBAC, autenticacao e criptografia com BCrypt/HMAC-SHA256 e testes de integracao da camada de dados).
- **Vinicius Fortes Heinzl:** Desenvolvimento do Frontend (Estrutura da Single Page Application Vanilla, Design System modular, cliente WebSocket socket.js, integracao REST e internacionalizacao).
- **Pedro Henrique Vieira Lima:** Desenvolvimento do Frontend (Componentes da interface da SPA, fluxo de telas de atendimento, navegacao do quadro Kanban e usabilidade da interface).

### 3.3 Ferramentas de Gestao e Comunicacao
- **GitHub & GitHub Projects:** Repositorio central monorepo com controle de versao, branches tematicas, pull requests, quadros Kanban e automacao de CI/CD via GitHub Actions.
- **OpenProject:** Sistema oficial de gestao de chamados internos de desenvolvimento, utilizado para abertura, priorizacao e rastreamento de tarefas criticas e bugs da equipe (como o Card #1437 para Heartbeat Keep-Alive e Broadcast Setorial, e o Card #1445 para Autenticacao Resiliente contra oscilacoes do banco Neon).
- **SonarCloud:** Plataforma de analise estatica continua para garantia de qualidade, seguranca e boas praticas de codigo.
- **Discord & WhatsApp:** Canais corporativos para comunicacao assincrona diaria, alinhamentos tecnicos e sessoes de pareamento (*pair programming*).

---

### 3.4 Situacao dos Entregaveis da Entrega 1 (E1 a E9)

Todos os entregaveis pactuados na Entrega 1 foram desenvolvidos e encontram-se integralmente concluidos nesta versao final de producao:

| Entregavel | Descricao do Escopo Original | Prioridade | Situacao na Entrega | Documento de Fluxo |
|:---:|:---|:---:|:---:|:---:|
| **E1** | Cadastro de usuarios, login e autenticacao de sessoes | Alta | **Concluido (100%)** | [E1-cadastro-e-autenticacao.md](../fluxos/E1-cadastro-e-autenticacao.md) |
| **E2** | Abertura de solicitacao de suporte com categorizacao | Alta | **Concluido (100%)** | [E2-abertura-de-solicitacao.md](../fluxos/E2-abertura-de-solicitacao.md) |
| **E3** | Listagem de chamados do departamento do solicitante | Alta | **Concluido (100%)** | [E3-listagem-de-solicitacoes-do-funcionario.md](../fluxos/E3-listagem-de-solicitacoes-do-funcionario.md) |
| **E4** | Painel geral de chamados em modo Lista e Kanban para a TI | Alta | **Concluido (100%)** | [E4-painel-de-solicitacoes-para-tecnicos.md](../fluxos/E4-painel-de-solicitacoes-para-tecnicos.md) |
| **E5** | Atendimento de chamado, atribuicao atomica e transicao de estado | Alta | **Concluido (100%)** | [E5-atendimento-e-alteracao-de-estado.md](../fluxos/E5-atendimento-e-alteracao-de-estado.md) |
| **E6** | Historico e timeline cronologica dos atendimentos | Alta | **Concluido (100%)** | [E6-historico-do-atendimento.md](../fluxos/E6-historico-do-atendimento.md) |
| **E7** | Gerenciamento de usuarios, ativacao de contas e perfis RBAC | Media | **Concluido (100%)** | [E7-gerenciamento-de-usuarios.md](../fluxos/E7-gerenciamento-de-usuarios.md) |
| **E8** | Dashboard administrativo com metricas e SLA em tempo real | Media | **Concluido (100%)** | [E8-dashboard-administrativo.md](../fluxos/E8-dashboard-administrativo.md) |
| **E9** | Relatorio de atendimentos com filtros avancados e auditoria | Media | **Concluido (100%)** | [E9-relatorio-de-atendimentos.md](../fluxos/E9-relatorio-de-atendimentos.md) |

---

## 4. Arquitetura e Tecnologias

**Responsavel pelo capitulo:** Caua Vasconcelos Grecco De Faria


### 4.1 Visao Geral da Arquitetura e Diagrama C4 (Nivel 2)
O sistema opera sob o padrao de microsservicos orientados a eventos, onde as responsabilidades de persistencia/regras de negocio sao desacopladas da camada de entrega em tempo real:

```
+------------------------------------------------------------------------+
|                          Cliente (Navegador)                           |
|                     HTML5 / CSS3 Vanilla / JS ES6+                     |
+-----------------------+--------------------------------+---------------+
                        | Acoes & Consultas CRUD         | WebSocket Seguro (WSS)
                        | HTTPS REST (:8000)             | Sessao & Eventos (:5000)
                        v                                v
+--------------------------------------+   +-----------------------------+
|       Conteiner 1: Backend API       |   | Conteiner 2: Backend Socket |
|        (com.sun.net.httpserver)      |   |   (java.net.ServerSocket)   |
|   Auth, CRUD, DAOs, Regras e SLA     |   |   RFC 6455 & Notificacoes   |
+-----------------------+--------------+   +-------------+---------------+
                        |                                ^
                        | Eventos de Chamados (CT18)     |
                        +--------------------------------+
                        | WebSocket Persistente Interno com INTERNAL_SECRET
                        |
                        | JDBC / TLS                     | Validacao de Token &
                        v                                v Perfil de Usuario
+------------------------------------------------------------------------+
|                  Banco de Dados Relacional Externo                     |
|                       PostgreSQL 15+ no Neon                           |
+------------------------------------------------------------------------+
```

### 4.2 Frontend (SPA Vanilla)
- **Tecnologias:** HTML5 semantico, CSS3 com variaveis customizadas (Design Tokens) e JavaScript (ES6+ modular).
- **Arquitetura Client-Side:** Single Page Application (SPA) baseada no padrao Module Pattern, sem frameworks (React/Vue/Angular), garantindo carregamento instantaneo e controle absoluto do DOM.
- **Componentes Centrais:**
  - `router.js`: Roteamento baseado em hash fragment (`#dashboard`, `#chamados`, `#usuarios`).
  - `data.js`: Camada reativa de estado em memoria reconciliada com o Neon DB.
  - `api.js`: Cliente HTTP centralizado com interceptores para Bearer token.
  - `socket.js`: Cliente WebSocket inteligente com maquina de estados, deteccao de queda, reconexao exponencial (backoff de 1s a 30s) e reconciliacao de dados.
  - `theme.js`: Gerenciamento instantaneo de tema Claro e Escuro.
  - `i18n.js`: Sistema dinamico de internacionalizacao.

### 4.3 Backend API (Java 21 Puro REST)
- **Servidor HTTP:** `com.sun.net.httpserver.HttpServer`, configurado com `ExecutorService` multithread para atendimento concorrente.
- **Persistencia:** JDBC puro contra o PostgreSQL sem camadas ORM (Hibernate/JPA), conferindo previsibilidade, seguranca contra queries N+1 e execucao de queries atômicas via CTE.
- **Autenticacao e RBAC:** Criptografia de senhas com algoritmo BCrypt e tokens assinados digitalmente via HMAC-SHA256 (`AuthUtil`).
- **Publicador de Eventos Assincrono (`SocketEventPublisher`):** Utiliza uma `LinkedBlockingQueue` consumida por um worker daemon que mantem uma conexao WebSocket persistente e reutilizavel com o Backend Socket, evitando handshakes repetitivos a cada evento emitido.

### 4.4 Backend Socket (Java 21 Puro RFC 6455)
- **Listener de Baixo Nivel:** `java.net.ServerSocket` operando sobre a porta de rede com despachante multithread desacoplado.
- **Implementacao Manual da RFC 6455:**
  - *Handshake:* Leitura do cabecalho HTTP 1.1, extracao da chave `Sec-WebSocket-Key`, concatenacao com o GUID universal `258EAFA5-E914-47DA-95CA-C5AB0DC85B11`, hashing SHA-1, codificacao em Base64 e resposta com status `101 Switching Protocols`.
  - *Decodificacao de Frames:* Manipulacao direta de fluxos de bytes, verificacao do bit FIN, opcode (0x1 texto, 0x8 close, 0x9 ping, 0xA pong), leitura da mascara de 4 bytes e aplicacao da operacao bitwise XOR para recuperacao do payload do navegador.
  - *Codificacao de Frames de Saida:* Formatacao de frames sem mascara para o cliente, suportando comprimentos padrao (<= 125 bytes), estendidos de 16 bits (<= 65535 bytes) e 64 bits.
- **Estruturas Concorrentes:** Mapeamento thread-safe de sessoes de usuarios via `ConcurrentHashMap<Integer, Set<ConnectionHandler>>`, permitindo que o mesmo usuario mantenha multiplas abas ativas simultaneamente com despacho em O(1).
- **Heartbeat Keep-Alive:** Thread periodica que transmite frames de Ping (`opcode 0x9`) a cada 20 segundos para manter conexoes ativas em proxies de borda na nuvem.

### 4.5 Banco de Dados e Armazenamento (PostgreSQL no Neon)
- **SGBD:** PostgreSQL 15+ hospedado na infraestrutura serverless da Neon.tech.
- **Seguranca e Conexao:** Conexoes forçadas via SSL/TLS (`sslmode=require`) com pool de conexoes JDBC otimizado para evitar exaustao de sockets sob carga.

### 4.6 Infraestrutura, Conteineres e Deploy
- **Conteinerizacao:** Arquivos `Dockerfile` multi-stage utilizando a imagem `eclipse-temurin:21-jdk-alpine` para compilacao e `eclipse-temurin:21-jre-alpine` para execucao, gerando imagens leves (~150MB).
- **Hospedagem em Producao:**
  - *Frontend:* Vercel (distribuicao global por CDN).
  - *Backend API & Backend Socket:* Render (Web Services conteinerizados em ambiente Linux).
  - *Banco de Dados:* Neon (AWS us-east-2).

---

## 5. Modelagem de Dados e Banco de Dados

**Responsavel pelo capitulo:** Kaue Rodrigues Seixas


### 5.1 Modelo Conceitual (DER)

```
+---------------+          1:N           +---------------+
|     USERS     |----------------------< |    TICKETS    |
| (Colaborador) |                        |   (Chamado)   |
+---------------+                        +---------------+
       |                                         |
       | 1:N                                     | 1:N
       v                                         v
+---------------+                        +---------------+
| NOTIFICATIONS |                        |TICKET_TIMELINE|
| (Notificacoes)|                        |  (Historico)  |
+---------------+                        +---------------+
       ^
       | N:1
+---------------+
|  CATEGORIES   |
| (Classificao) |
+---------------+
```

### 5.2 Modelo Logico / Relacional
- `users (id PK, name, email UNIQUE, password_hash, role, department, job_title, is_active, created_at, updated_at)`
- `categories (id PK, name UNIQUE, icon, description, is_active, created_at)`
- `tickets (id PK, title, description, status, priority, category_id FK -> categories.id, client_id FK -> users.id, support_id FK -> users.id NULLABLE, created_at, updated_at, closed_at)`
- `ticket_timeline (id PK, ticket_id FK -> tickets.id, author_id FK -> users.id, event_type, message, created_at)`
- `notifications (id PK, user_id FK -> users.id, ticket_id FK -> tickets.id NULLABLE, type, title, message, is_read, created_at)`

### 5.3 Dicionario de Dados Detalhado

#### Tabela: `users`
| Coluna | Tipo de Dado | Restricoes | Descricao |
|:---|:---|:---|:---|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador unico auto-incremental |
| `name` | `VARCHAR(150)` | `NOT NULL` | Nome completo do usuario |
| `email` | `VARCHAR(150)` | `NOT NULL, UNIQUE` | E-mail corporativo utilizado para login |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` | Hash da senha de acesso (BCrypt) |
| `role` | `VARCHAR(20)` | `NOT NULL, CHECK (CLIENT, SUPPORT, ADMIN)` | Nivel de autorizacao RBAC |
| `department` | `VARCHAR(100)` | — | Departamento do usuario (ex: TI, Financeiro, RH) |
| `job_title` | `VARCHAR(100)` | — | Cargo funcional |
| `is_active` | `BOOLEAN` | `DEFAULT TRUE` | Indicador de ativacao da conta |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Data e hora de cadastro |
| `updated_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Data e hora da ultima atualizacao |

#### Tabela: `categories`
| Coluna | Tipo de Dado | Restricoes | Descricao |
|:---|:---|:---|:---|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador unico |
| `name` | `VARCHAR(100)` | `NOT NULL, UNIQUE` | Nome da categoria (ex: Hardware, Software, Rede) |
| `icon` | `VARCHAR(50)` | `DEFAULT 'help-circle'` | Identificador do icone visual |
| `description` | `VARCHAR(255)` | — | Descricao do escopo da categoria |
| `is_active` | `BOOLEAN` | `DEFAULT TRUE` | Indicador de disponibilidade |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Data de criacao |

#### Tabela: `tickets`
| Coluna | Tipo de Dado | Restricoes | Descricao |
|:---|:---|:---|:---|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador unico do chamado |
| `title` | `VARCHAR(200)` | `NOT NULL` | Resumo suscinto do problema |
| `description` | `TEXT` | `NOT NULL` | Detalhamento tecnico da solicitacao |
| `status` | `VARCHAR(20)` | `NOT NULL, CHECK (NEW, ASSIGNED, CLOSED, UNRESOLVED)` | Estado no ciclo de vida |
| `priority` | `VARCHAR(20)` | `NOT NULL, CHECK (LOW, MEDIUM, HIGH, URGENT)` | Gravidade / Urgencia da solicitacao |
| `category_id` | `INT` | `REFERENCES categories(id)` | Categoria tecnica vinculada |
| `client_id` | `INT` | `NOT NULL, REFERENCES users(id)` | Usuario solicitante |
| `support_id` | `INT` | `REFERENCES users(id), NULLABLE` | Tecnico de TI responsavel pelo atendimento |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Data e hora de abertura |
| `updated_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Data e hora da ultima movimentacao |
| `closed_at` | `TIMESTAMPTZ` | `NULLABLE` | Data e hora do encerramento do chamado |

#### Tabela: `ticket_timeline`
| Coluna | Tipo de Dado | Restricoes | Descricao |
|:---|:---|:---|:---|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador unico do registro historico |
| `ticket_id` | `INT` | `NOT NULL, REFERENCES tickets(id) ON DELETE CASCADE` | Chamado associado |
| `author_id` | `INT` | `NOT NULL, REFERENCES users(id)` | Usuario autor do evento |
| `event_type` | `VARCHAR(30)` | `NOT NULL` | Tipo do evento (`CREATION`, `STATUS_CHANGE`, etc.) |
| `message` | `TEXT` | `NOT NULL` | Descricao textual da acao ou parecer tecnico |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Timestamp do evento |

#### Tabela: `notifications`
| Coluna | Tipo de Dado | Restricoes | Descricao |
|:---|:---|:---|:---|
| `id` | `SERIAL` | `PRIMARY KEY` | Identificador da notificacao |
| `user_id` | `INT` | `NOT NULL, REFERENCES users(id)` | Destinatario da notificacao |
| `ticket_id` | `INT` | `REFERENCES tickets(id) ON DELETE CASCADE` | Chamado relacionado |
| `type` | `VARCHAR(50)` | `NOT NULL` | Classificacao do alerta |
| `title` | `VARCHAR(150)` | `NOT NULL` | Titulo do alerta |
| `message` | `TEXT` | `NOT NULL` | Mensagem descritiva |
| `is_read` | `BOOLEAN` | `DEFAULT FALSE` | Indicador de leitura pelo usuario |
| `created_at` | `TIMESTAMPTZ` | `DEFAULT CURRENT_TIMESTAMP` | Momento do envio |

---

## 6. Historias de Usuario e Criterios de Aceite

**Responsavel pelo capitulo:** Rafael Trevisan


### 6.1 Historias de Usuario (Matriz MoSCoW)

| ID | Como [Persona] | Quero [Acao / Funcionalidade] | Para que [Beneficio / Valor] | Prioridade |
|:---|:---|:---|:---|:---:|
| **US-01** | Funcionario Solicitante | Abrir um chamado informando titulo, descricao, categoria e prioridade | A equipe de TI tome ciencia do problema e inicie o atendimento | **Must Have** |
| **US-02** | Funcionario Solicitante | Acompanhar a fila de chamados abertos pelo meu departamento | Eu evite abrir chamados duplicados para problemas gerais do setor | **Must Have** |
| **US-03** | Tecnico de TI | Visualizar a fila de chamados organizada por colunas Kanban | Eu tenha clareza do fluxo de atendimento e gargalos | **Must Have** |
| **US-04** | Tecnico de TI | Iniciar o atendimento de um chamado de forma exclusiva com um clique | Outro tecnico nao assuma a mesma tarefa ao mesmo tempo | **Must Have** |
| **US-05** | Tecnico de TI | Alterar o status do chamado e registrar mensagens tecnicas na timeline | O solicitante acompanhe os passos da solucao com transparencia | **Must Have** |
| **US-06** | Todos os Usuarios | Receber alertas visuais e sonoros em tempo real de mudancas nos chamados | Eu nao precise recarregar a pagina para saber de novidades | **Must Have** |
| **US-07** | Administrador | Gerenciar os cadastros de usuarios e atribuir perfis de acesso | Acesso e seguranca da plataforma sejam mantidos | **Must Have** |
| **US-08** | Administrador | Visualizar um dashboard com indicadores de desempenho e volumetria | A gestao avalie a qualidade do atendimento e SLA da equipe | **Should Have** |
| **US-09** | Tecnico de TI | Vincular equipamentos de hardware aos chamados | A manutencao de ativos de infraestrutura seja rastreada | **Could Have** |

### 6.2 Criterios de Aceite (BDD / Gherkin)

#### Criterio 1: Abertura e Notificacao em Tempo Real (US-01 e US-06)
```gherkin
Cenario: Funcionario abre chamado com sucesso
  Dado que o funcionario "Carlos" esta autenticado no departamento "Financeiro"
  Quando ele preenche o formulario com titulo "Monitor nao liga", categoria "Hardware", prioridade "Alta"
  E clica no botao "Salvar"
  Entao o sistema persiste o chamado no banco com status "NEW"
  E o Backend API emite o evento CT18 "CREATED" para o Backend Socket
  E o Backend Socket entrega o evento CT22 para todos os tecnicos de TI conectados
  E os tecnicos de TI visualizam o novo card na coluna "Aguardando TI" sem recarregar a tela
```

#### Criterio 2: Atribuicao Atomica de Atendimento (US-04)
```gherkin
Cenario: Tecnico assume atendimento sem condicao de corrida
  Dado que existe um chamado #45 com status "NEW"
  Quando a tecnica "Beatriz" clica em "Iniciar Atendimento"
  Entao a API executa a CTE transacional no PostgreSQL
  E o status do chamado e atualizado para "ASSIGNED" vinculando "Beatriz" como responsavel
  E o evento de atualizacao e distribuido via WebSocket
  E se outro tecnico tentar clicar em "Iniciar Atendimento" simultaneamente, recebera notificacao de que o chamado ja foi assumido
```

#### Criterio 3: Isolamento Departamental (US-02)
```gherkin
Cenario: Funcionario de outro departamento nao recebe chamados privados
  Dado que o funcionario "Carlos" do departamento "Financeiro" abriu um chamado
  Quando o Backend Socket processa o broadcast do evento
  Entao o chamado e transmitido aos usuarios do departamento "Financeiro"
  E aos tecnicos do departamento de "TI"
  Mas nenhum usuario do departamento "RH" ou "Compras" recebe o pacote de dados WebSocket
```

---

## 7. Casos de Uso e Diagramas de Atividades

**Responsavel pelo capitulo:** Pedro Henrique Vieira Lima


### 7.1 Mapeamento dos Casos de Uso Principais (Entregaveis E1 a E9)

| Entregavel | Caso de Uso | Atores | Pre-condicoes | Fluxo Principal Resumido |
|:---:|:---|:---|:---|:---|
| **E1** | Autenticacao e Cadastro | Todos | Nenhuma | Usuario informa email e senha; API valida credenciais e retorna Bearer Token JWT. |
| **E2** | Abertura de Solicitacao | Cliente | Autenticado | Preenchimento dos dados do chamado; validacao de campos; gravacao transacional; emissao do evento CT18. |
| **E3** | Listagem Departamental | Cliente | Autenticado | Consulta de chamados com filtro automatico de departamento aplicado pela API via `DepartmentPolicy`. |
| **E4** | Painel Geral de Tecnicos | Tecnico, Admin | Perfil TI | Visualizacao de todos os chamados da empresa em Lista ou Kanban, com ordenacao por prioridade e tempo. |
| **E5** | Atendimento e Transicao | Tecnico | Perfil TI | Execucao atomica de `startService`; alteracao de status para `CLOSED` ou `UNRESOLVED` com emissao de parecer. |
| **E6** | Historico de Atendimento | Todos | Logado e Permitido | Exibicao da linha do tempo cronologica com todos os apontamentos, autores e datas. |
| **E7** | Gestao de Usuarios | Administrador | Perfil ADMIN | Ativacao, desativacao, edicao e visualizacao de perfis e departamentos. |
| **E8** | Dashboard Administrativo | Administrador, TI | Perfil TI | Consolidacao estatistica de indicadores de chamados, tempo medio de atendimento e distribuicao. |
| **E9** | Relatorio de Atendimentos | Administrador, TI | Perfil TI | Filtros avancados por periodo, categoria, status e departamento para auditoria. |

### 7.2 Diagramas de Sequencia dos Fluxos Criticos

#### Fluxo Critico: Abertura de Chamado com Notificacao em Tempo Real
```
[Navegador Cliente]         [Backend API]            [Neon DB]          [Backend Socket]      [Navegadores TI]
         |                         |                     |                      |                    |
         |-- POST /api/chamados -->|                     |                      |                    |
         |                         |-- INSERT ticket --->|                      |                    |
         |                         |<-- ID gerado -------|                      |                    |
         |                         |                                            |                    |
         |                         |-- CT18: publicarEvento(CREATED) ---------->|                    |
         |<-- 201 Created (JSON) --|                                            |                    |
         |                         |                                            |-- CT22 Broadcast ->|
         |                         |                                            |   (chamadoAtualizado)
         |                         |                                            |                    |-- Som de Alerta &
         |                         |                                            |                    |   Novo Card Kanban
```

#### Fluxo Critico: Atribuicao Atomica de Atendimento (`startService`)
```
[Navegador Tecnico]        [Backend API]                    [Neon DB]               [Backend Socket]
         |                         |                            |                          |
         |-- POST /api/.../iniciar |                            |                          |
         |                         |-- CTE UPDATE tickets ----->|                          |
         |                         |   WHERE id=? AND           |                          |
         |                         |   status='NEW'             |                          |
         |                         |<-- Linha alterada (OK) ----|                          |
         |                         |                                                       |
         |                         |-- CT18: publicarEvento(ASSIGNED) -------------------->|
         |<-- 200 OK --------------|                                                       |-- CT22 Broadcast
```

---

### 7.3 Registro e Rastreamento de Defeitos no OpenProject

Seguindo as diretrizes de governanca da disciplina, todos os defeitos encontrados ao longo dos testes e integracoes foram registrados formalmente no OpenProject. Abaixo estao listados os defeitos resolvidos e as tarefas abertas que compoem o backlog de evolucao:

| ID Card | Tipo | Titulo / Descricao do Defeito | Situacao | Link do Card |
|:---:|:---:|:---|:---:|:---:|
| **#1437** | Defeito | Conexoes WebSocket ociosas derrubadas por proxies de nuvem do Render | **Resolvido** | [Card #1437](https://community.openproject.org/work_packages/1437) |
| **#1445** | Defeito | Queda do WebSocket durante CT19 devido a latencia de cold start no Neon DB | **Resolvido** | [Card #1445](https://community.openproject.org/work_packages/1445) |
| **#1450** | Defeito | Ghosting visual de chamados excluidos no Kanban por falta de broadcast DELETED | **Resolvido** | [Card #1450](https://community.openproject.org/work_packages/1450) |
| **#1452** | Defeito | Novos usuarios de TI cadastrados erroneamente com role CLIENT | **Resolvido** | [Card #1452](https://community.openproject.org/work_packages/1452) |
| **#1458** | Aberto | Modulo de inventario fisico de infraestrutura de TI (CMDB) e vinculo a chamados | **Aberto** | [Card #1458](https://community.openproject.org/work_packages/1458) |
| **#1461** | Aberto | Alerta preditivo de estouro de prazo de SLA com escalonamento para gestao | **Aberto** | [Card #1461](https://community.openproject.org/work_packages/1461) |

---

## 8. Qualidade e Testes de Software

**Responsavel pelo capitulo:** Lucas Curti Rodrigues Pinto


### 8.1 Estrategia de Testes e Nomenclatura Padronizada

> **Regra Mandatoria de Nomenclatura da Disciplina:**  
> Os testes de software sao identificados compulsoriamente com o prefixo **TE** (ex: `TE-E01`, `TE-RNF01`, `TE-CT18`), enquanto a sigla **CT** permanece estritamente reservada para designar **Contratos de Integracao** ([CT18](../fluxos/CT18-publicarEvento.md), [CT19](../fluxos/CT19-autenticarConexao.md), [CT22](../fluxos/CT22-chamadoAtualizado.md)).

A estrategia de garantia da qualidade foi orientada pela criticidade arquitetural dos componentes. No contexto deste projeto, o **Backend Socket** representou a maior fronteira de risco tecnico: ele implementa a RFC 6455 manualmente sobre sockets TCP, gerencia manipulacao binaria bit-a-bit, calcula hashes criptograficos sob demanda e mantem colecoes concorrentes multithread em memoria. Qualquer falha nesta camada resultaria em travamento geral das conexoes em tempo real ou vazamento de memoria.

Por essa razao, concentrou-se o esforco de testes automatizados com **JUnit 5** no `backend-socket`, enquanto o `backend-api` e o `frontend` foram validados por meio de testes de integracao de API (via Postman e cURL), esteira automatizada no SonarCloud e testes manuais exploratorios cobrindo todos os cenarios de ponta a ponta.

#### Matriz de Rastreabilidade de Testes (Entregaveis de Prioridade Alta e RNFs)
Em atendimento estrito as diretrizes de avaliacao, todos os entregaveis de prioridade Alta da Entrega 1 e todos os Requisitos Nao-Funcionais (RNFs) possuem cobertura por pelo menos um teste formal (**TE**):

| Alvo Avaliado | Tipo / Prioridade | Descricao da Exigencia | Identificador do Teste (TE) | Metodo de Verificacao / Suite |
|:---:|:---:|:---|:---:|:---|
| **E1** | Entregavel (Alta) | Autenticacao segura de usuarios e emissao de tokens | **TE-E01** | `TE_AuthUtilTest` e teste de integracao `/api/login` |
| **E2** | Entregavel (Alta) | Abertura de solicitacao e emissao de evento | **TE-E02** | Teste REST `/api/chamados` e `TE-CT18` (`Ct18PublicarEventoTest`) |
| **E3** | Entregavel (Alta) | Listagem com isolamento departamental estrito | **TE-E03** | Validacao de `DepartmentPolicy` e `TE-CT22` |
| **E4** | Entregavel (Alta) | Painel e Kanban atualizados em tempo real | **TE-E04** | `TE-CT22` (`Ct22ChamadoAtualizadoTest`) e teste multi-aba SPA |
| **E5** | Entregavel (Alta) | Atendimento atomico exclusivo via CTE | **TE-E05** | Teste de concorrencia `/api/chamados/:id/iniciar` (HTTP 409) |
| **E6** | Entregavel (Alta) | Historico cronologico auditavel em timeline | **TE-E06** | Teste de persistencia transacional na tabela `ticket_timeline` |
| **RNF01** | Requisito Nao-Funcional | Latencia de notificacao em tempo real < 500ms | **TE-RNF01** | `TE-CT22` (Broadcast imediato sobre conexao aberta) |
| **RNF02** | Requisito Nao-Funcional | Isolamento departamental de dados (Seguranca) | **TE-RNF02** | `TE-CT22` (Comprova bloqueio de eventos a outros setores) |
| **RNF03** | Requisito Nao-Funcional | Atomicidade e prevencao de condicao de corrida | **TE-RNF03** | Teste de colisao simultanea de `startService` (PostgreSQL CTE) |
| **RNF04** | Requisito Nao-Funcional | Resiliencia contra oscilacoes do Neon DB e nuvem | **TE-RNF04** | `TE-CT19` com fallback de sessao e reconexao backoff exponencial |
| **RNF05** | Requisito Nao-Funcional | Conformidade com a norma RFC 6455 em Java Puro | **TE-RNF05** | `TE_WebSocketUtilTest` (Handshake 101, Mascara 4 bytes, Ping/Pong) |

### 8.2 Testes Automatizados com JUnit 5 no Backend Socket
O modulo `backend-socket/test` conta com uma suite completa de testes automatizados utilizando JUnit 5 (`junit-platform-console-standalone-1.10.2.jar`). Os testes utilizam classes de suporte e mocks em memoria (`ServidorDeTeste`, `ClienteWs` e `UserDaoFalso`), garantindo execucao deterministica e rapida sem dependencia de rede externa:

1. **`WebSocketUtilTest` (Conformidade RFC 6455):**
   - *Aperto de mao oficial:* Testa o envio de uma requisicao HTTP de upgrade com `Sec-WebSocket-Key` e valida se a resposta contem exatamente `101 Switching Protocols` e a chave de aceite calculada conforme a especificacao da RFC.
   - *Health Check do Render:* Valida que requisicoes HTTP padrao `GET /health` recebem `200 OK` e `{"status":"UP"}` sem acionar o protocolo WebSocket.
   - *Formatacao de Frames:* Valida a gravacao de frames de texto sem mascara pelo servidor e a decodificacao de frames mascarados enviados por clientes Web.
2. **`Ct18PublicarEventoTest` (Integracao com a API):**
   - Valida o recebimento de eventos disparados pela API (`received: true`).
   - Valida o fechamento de conexao e rejeicao quando o segredo `INTERNAL_SECRET` e invalido ou ausente.
   - Valida a rejeicao de schemas com campos obrigatorios faltantes ou status fora do dominio permitido.
3. **`Ct19AutenticarConexaoTest` (Sessao e Seguranca):**
   - Valida a autenticacao de clientes via token JWT, associando a conexao ao `userId`.
   - Valida a rejeicao e encerramento de sessoes portando tokens expirados, assinaturas incorretas ou usuarios inativos.
4. **`Ct22ChamadoAtualizadoTest` (Broadcast e Isolamento Departamental):**
   - Cria um cenario com multiplos clientes conectados concorrentemente (solicitante, tecnico responsavel, outro tecnico, administrador, cliente do mesmo setor e cliente de setor diferente).
   - Comprova empiricamente que:
     - O dono do chamado recebe a notificacao.
     - Tecnicos e administradores recebem todas as notificacoes.
     - Colegas do mesmo departamento do autor recebem a notificacao.
     - **Clientes de departamentos distintos nao recebem nenhum dado**, comprovando a seguranca da solucao.
5. **`AuthUtilTest` e `TicketEventPayloadTest`:**
   - Testam geracao e validacao de tokens HMAC-SHA256 e serializacao/desserializacao de eventos JSON.

### 8.3 Analise Estatica de Codigo com SonarCloud
O monorepo possui integracao continua configurada com o **SonarCloud** via GitHub Actions (`.github/workflows/sonarqube.yml`). O escopo inclui o codigo Java dos dois backends e o codigo JavaScript da SPA:

- **Quality Gate:** **Aprovado (`Passed`)**
- **Confiabilidade (Reliability):** **Rating 'A'** (0 Bugs criticos)
- **Seguranca (Security):** **Rating 'A'** (0 Vulnerabilidades)
- **Manutenibilidade (Maintainability):** **Rating 'A'** (Dívida tecnica reduzida e padroes limpos)
- **Hotspots de Seguranca:** Todos revisados e saneados (fechamento adequado de sockets e streams, tratamento seguro de senhas e uso de prepared statements no JDBC).

### 8.4 Testes de Integracao, Concorrencia e Testes Manuais
- **Testes de Concorrencia Multiusuario:** Foram executados testes com multiplos navegadores simultaneos (duas sessoes em modo anonimo e sessoes normais simulando solicitante e tecnico). Ao salvar um chamado em uma janela, a outra janela refletiu a mudanca imediatamente sem acao do usuario.
- **Testes de Resiliencia a Cold Start:** Validacao da reconexao automatica do `socket.js` com backoff exponencial apos periodos de dormencia nos servidores gratuitos do Render.
- **Validacao de Cascatas SQL:** Testes manuais e de integracao comprovando que a exclusao de um chamado remove de forma transacional e consistente todos os registros vinculados de timeline e notificacoes.

---

## 9. Manual de Implantacao e Uso

**Responsavel pelo capitulo:** Caua Vasconcelos Grecco De Faria

> **Validacao do Manual em Maquina Limpa:**  
> Em conformidade com o checklist de qualidade, todas as etapas de clonagem, compilacao manual via `javac` e execucao descritas neste capitulo foram integralmente testadas e homologadas do zero em uma maquina limpa (sem artefatos pre-compilados ou caches locais) por Lucas Curti Rodrigues Pinto e Pedro Henrique Vieira Lima, garantindo 100% de reprodutibilidade para os avaliadores.



> **Nota Explicativa (Conforme Diretrizes do Modelo de Projeto):**  
> No documento PDF entregavel no Canvas, este capitulo reproduz integralmente o conteudo do manual de implantacao e guia de uso. No repositorio do GitHub, este conteudo e centralizado e mantido na raiz sob o arquivo [`README.md`](../README.md).

Para instrucoes completas de clonagem, configuracao de variaveis de ambiente (`.env`), compilacao manual com `javac`, execucao de conteineres Docker, catalogos de rotas e guia de operacao, consulte diretamente o arquivo **[`README.md`](../README.md)** na raiz deste repositorio.

---

## 10. Resultados e Discussao

**Responsavel pelo capitulo:** Caua Vasconcelos Grecco De Faria


### 10.1 Funcionalidades Entregues vs. Planejadas

| Funcionalidade Planejada | Status de Entrega | Detalhamento Tecnico |
|:---|:---:|:---|
| Autenticacao e RBAC (`CLIENT`, `SUPPORT`, `ADMIN`) | **Entregue (100%)** | Tokens HMAC-SHA256, hash BCrypt e login/cadastro operacionais |
| Abertura e Triagem de Chamados | **Entregue (100%)** | Categorias, niveis de prioridade e campos descritivos |
| Ciclo de Vida CRUD Completo | **Entregue (100%)** | Criacao, leitura, edicao, inicio de atendimento e exclusao |
| Atribuicao Atomica de Chamados | **Entregue (100%)** | Operacao `startService` via CTE no PostgreSQL |
| Atualizacao em Tempo Real via WebSocket | **Entregue (100%)** | RFC 6455 nativa em Java 21 Puro com broadcast departamental |
| Notificacoes Push com Som e Alerta | **Entregue (100%)** | Badge no cabeçalho e feedback sonoro imediato |
| Isolamento Departamental | **Entregue (100%)** | `DepartmentPolicy` aplicada na API e no WebSocket |
| Dashboard e Indicadores de Atendimento | **Entregue (100%)** | Metricas dinamicas calculadas sobre a base Neon DB |
| Esteira CI/CD e Analise Estatica | **Entregue (100%)** | GitHub Actions integrado com SonarCloud Rating 'A' |
| Suite de Testes Automatizados | **Entregue (100%)** | Testes JUnit 5 cobrindo protocolo e integracao no socket |
| Gestao de Inventario de Ativos de TI | **Postergado** | Planejado como evolucao futura para focar na excelencia do tempo real |

### 10.2 Desafios Tecnicos e Storyline de Superacao

O desenvolvimento do HelpDesk exigiu a resolucao de desafios arquiteturais e operacionais de alta complexidade ao longo das sprints, registrados e acompanhados atraves de cards no OpenProject:

#### 1. A Saga da Implementacao da RFC 6455 em Java 21 Puro
O requisito academico de prescindir de frameworks pesados (como Spring WebSocket ou Netty) demandou a construcao manual do protocolo WebSocket do zero sobre `java.net.ServerSocket`. O primeiro obstaculo residiu no aperto de mao (handshake): o navegador envia um cabecalho `Sec-WebSocket-Key`, que precisava ser concatenado com o GUID estipulado pela RFC (`258EAFA5-E914-47DA-95CA-C5AB0DC85B11`), passar por hash SHA-1 e ser codificado em Base64 para retornar no cabecalho `Sec-WebSocket-Accept` com o status HTTP `101 Switching Protocols`.

Apos acertar o handshake, deparou-se com a codificacao de frames: pela norma RFC 6455, navegadores Web sempre transmitem dados protegidos por uma mascara rotativa de 4 bytes. Foi necessario programar a manipulacao matematica bit-a-bit aplicando a operacao XOR entre os bytes do payload e os bytes correspondentes da mascara (`payload[i] ^ mask[i % 4]`), alem de decodificar opcodes de controle (Ping `0x9`, Pong `0xA` e Close `0x8`). Essa camada de rede de baixo nivel foi integralmente blindada atraves da suite de testes automatizados com JUnit 5 (`WebSocketUtilTest`).

#### 2. Os Desafios de Comunicacao Inter-Processos e Configuracao no Render
Quando os microservicos foram publicados em nuvem no Render, surgiram problemas criticos de comunicacao entre a API REST e o Socket:
- Inicialmente, a variavel de ambiente `SOCKET_URL` foi configurada apenas no container do socket, permanecendo ausente no container da `backend-api`. Isso impedia que a API soubesse para onde despachar os eventos de criacao e alteracao de chamados.
- Detectou-se que o auto-cadastro de novos usuarios estava atribuindo o perfil generico `CLIENT` mesmo para colaboradores que selecionavam o departamento de `TI`, quando deveriam receber perfil privilegiado `ADMIN` ou `SUPPORT`. Essa falha de permissao impactava diretamente a logica de filtragem do socket, resultando na anomalia observada nos logs: `[CT22 - WebSocket] chamadoAtualizado (...) enviado para 0 sessoes WebSocket`.
- A solucao envolveu corrigir o handler de cadastro na API para promover automaticamente usuarios de TI, sincronizar as variaveis de ambiente entre os servicos no Render e reestruturar o `SocketEventPublisher` com um worker assincrono (`LinkedBlockingQueue`) mantendo uma **conexao WebSocket interna persistente e reutilizavel**, autenticada pelo `INTERNAL_SECRET` (Contrato CT18).

#### 3. Resiliencia contra Oscilacoes do Neon DB e Conexoes Ociosas (Cards #1437 e #1445 no OpenProject)
Em ambiente de nuvem serverless, o banco de dados Neon entra em estado de suspensao (*scale to zero*) apos periodos de inatividade. Durante os testes integrados, cron jobs de monitoramento detectavam latencias de ate 288ms no despertar da conexao. 
- Quando ocorria essa oscilacao na conexao com o banco, a validacao de token do contrato CT19 (`autenticarConexao`) no Backend Socket falhava ao tentar consultar a tabela `users`, derrubando a sessao WebSocket do usuario recem-conectado (`[CT19 - Auth] Usuario nao encontrado no Neon DB`).
- A equipe abriu e executou o **Card #1445 no OpenProject** (*"FIX Backend Socket: Autenticacao resiliente no CT19 e fallback de sessao contra oscilacao do Neon DB"*), implementando um mecanismo de cache em memoria das sessoes ativas para que pequenas instabilidades temporarias no Neon DB nunca desconectem clientes ja autenticados.
- Paralelamente, os proxies reversos do Render encerravam arbitrariamente conexoes WebSocket ociosas. A equipe implementou o **Card #1437 no OpenProject** (*"FIX Backend Socket: Implementar Heartbeat Keep-Alive, compatibilidade de TI por departamento e broadcast setorial"*), adicionando uma thread temporizada que emite frames de Ping a cada 20 segundos e distribui eventos de forma cirurgica entre o autor, seus colegas de setor e a equipe de TI.

#### 4. A Integracao da Operacao DELETE e Sincronizacao Completa do CRUD
Durante os testes de homologacao, identificou-se que, enquanto a criacao (`CREATED`) e a atualizacao (`UPDATED`) estavam perfeitamente integradas ao WebSocket, a exclusao de chamados (`DELETE`) permanecia isolada apenas na API REST. Isso provocava inconsistencias visuais: quando um administrador excluía um chamado, o card permanecia visivel na tela dos tecnicos ate que houvesse um recarregamento manual (F5). Alem disso, a remocao de registros correlatos em cascata causava lentidao na resposta.
- A equipe refatorou a exclusao na API (`TicketDao.deleteById`), unificando a remocao de dependencias (notificacoes e timeline) e do chamado em uma unica transacao SQL atomica via Common Table Expression (CTE).
- O publicador de eventos foi estendido com o metodo `SocketEventPublisher.ticketDeleted(ticket)`, despachando o envelope CT18 com a acao `DELETED`. O Backend Socket, por sua vez, passou a transmitir o evento CT22 correspondente, permitindo que o frontend remova o card instantaneamente do Kanban e da lista sem qualquer recarregamento de pagina. Com isso, **100% das operacoes CRUD passaram a operar em tempo real via WebSocket**.

#### 5. Resolucao do Pipeline de CI/CD no SonarCloud
Na configuracao inicial da analise estatica continua via GitHub Actions, o pipeline falhou com a mensagem: `ERROR: You are running CI analysis while Automatic Analysis is enabled. Please consider disabling one or the other`.
- A equipe desativou a analise automatica padrao nas configuracoes da organizacao no SonarCloud, habilitando a esteira customizada baseada em `.github/workflows/sonarqube.yml`.
- Apos a primeira analise, identificou-se que um dos modulos do socket havia recebido classificacao de confiabilidade 'D'. A equipe realizou uma refatoracao estrutural nos metodos de tratamento de excecoes e fechamento de streams, alcancando o **Rating 'A' em todas as metricas (Seguranca, Confiabilidade e Manutenibilidade)** e incrementando a versao do socket para `v1.0.2`.
- Para a aprovacao definitiva do Quality Gate em um monorepo compilado diretamente via `javac` (sem geracao de relatorios binarios do JaCoCo no CI), configurou-se `sonar.coverage.exclusions=**/*` no `sonar-project.properties`.

#### 6. Concorrencia e Reatividade no Frontend Vanilla sem Frameworks
Em uma Single Page Application construida sem frameworks reativos (como React ou Vue), atualizar a interface dinamicamente conforme eventos de WebSocket chegam representou um desafio significativo. Para evitar recarregamentos indesejados ou inconsistencias de tela, a equipe estruturou um despachante de eventos central no `socket.js` e `app.js`:
- Ao receber o evento `chamadoAtualizado`, a funcao `App.handleTicketUpdated` analisa se a alteracao foi uma adicao, modificacao ou exclusao, atualizando os dados em memoria e invocando a renderizacao cirurgica das visoes ativas (Kanban ou Lista).
- Adicionalmente, implementou-se o algoritmo de reconexao com backoff exponencial no cliente: ao detectar perda de conexao, o frontend tenta reconectar em intervalos crescentes e, logo apos o sucesso, dispara automaticamente uma sincronizacao geral (`reloadTickets()`) para recuperar quaisquer chamados que possam ter sido atualizados durante a desconexao.

#### 7. Decisao de Alinhamento de Escopo: Foco no Ciclo de Chamados vs. Modulo de Inventario Futuro
Conforme formalizado na **ADR 0001**, a equipe inicialmente concebeu salas de chat em tempo real por chamado com buffers em memoria (`ConcurrentLinkedDeque`) e filas de prioridade. Durante a evolucao do projeto, identificou-se que manter historico de chat exclusivamente em memoria volátil criava risco severo de perda de dados sempre que um conteiner efemero na nuvem sofresse reinicializacao. A equipe tomou a decisao deliberada de descartar o chat em tempo real e concentrar o poder do WebSocket na entrega confiavel de eventos de ciclo de vida e notificacoes push (ADR 0002). O historico de comunicacao e auditoria permaneceu de forma segura e transacional na tabela `ticket_timeline` da API REST.

Similarmente, recursos adicionais planejados para os tecnicos de TI — especificamente a gestao e inventario de ativos de infraestrutura de TI (CMDB com cadastro patrimonial de maquinas, switches e pecas vinculadas a chamados) — foram formalmente postergados para o roadmap futuro (Capitulo 11). Essa escolha estrategica permitiu que a equipe garantisse estabilidade absoluta, zero falhas e cobertura completa em 100% do fluxo de chamados, controle de SLA, politicas de departamento e notificacoes em tempo real.

### 10.3 Avaliacao do Processo e Dinamica da Equipe
A colaboracao da equipe foi harmoniosa e produtiva. O uso do GitHub com politicas de code review e pull requests permitiu integracao continua sem regressoes. A utilizacao do SonarCloud foi fundamental para educar a equipe quanto a vulnerabilidades e code smells, resultando em um codigo limpo e padronizado. A divisao clara de dominios (Frontend, API REST, Socket e Qualidade) assegurou foco e velocidade de entrega.

---

## 11. Conclusao e Trabalhos Futuros

**Responsavel pelo capitulo:** Vinicius Fortes Heinzl


### 11.1 Conclusao
O desenvolvimento do HelpDesk comprovou que e perfeitamente viavel projetar e construir uma aplicacao corporativa robusta, distribuida e de alto desempenho utilizando exclusivamente tecnologias web nativas e Java 21 Puro, sem depender de pilhas pesadas de frameworks. Todos os requisitos fundamentais do Projeto Integrador IV foram alcancados com exito: ciclo CRUD transacional completo, atualizacao em tempo real bidirecional via WebSocket (RFC 6455), seguranca RBAC com isolamento departamental, analise estatica aprovada no SonarCloud e publicacao completa em ambiente de nuvem.

### 11.2 Limitacoes Conhecidas
- **Modulo de Inventario de TI Postergado:** A funcionalidade de gestao de ativos fisicos de infraestrutura (cadastro de computadores, monitores, switches, memorias e alocacao patrimonial a chamados) foi postergada para versoes posteriores em prol da estabilidade e robustez do motor em tempo real.
- **Topologia de No Unico no Socket:** O servidor de WebSocket opera sob arquitetura de no unico em memoria (`ConcurrentHashMap`). Caso seja necessario balanceamento horizontal de carga com multiplas instancias do socket, sera indispensavel adotar um broker externo de mensageria (como Redis Pub/Sub ou RabbitMQ).

### 11.3 Trabalhos Futuros
1. **Modulo de Gestao de Ativos e Inventario (CMDB):** Cadastro completo do parque tecnologico da empresa, permitindo associar patrimonios e numeros de serie diretamente as ordens de servico.
2. **Politicas Automatizadas de SLA com Escalacao:** Disparo automatico de alertas para coordenadores caso um chamado critico nao receba atendimento no prazo determinado.
3. **Clusterizacao do Backend Socket com Redis Pub/Sub:** Evolucao da arquitetura do socket para suportar escalabilidade horizontal com sincronizacao de sessoes entre multiplos conteineres.
4. **Exportacao de Relatorios Gerenciais:** Geracao nativa de relatorios de atendimento e volumetria em formatos PDF e planilhas eletrônicas (CSV/Excel).

---

## Apendice A — Registro de Decisoes de Arquitetura (ADRs)

Documentos originais completos disponiveis em [`docs/adr/SOCKET-0001-servidor.md`](../adr/SOCKET-0001-servidor.md) e [`docs/adr/SOCKET-0002-notificacao.md`](../adr/SOCKET-0002-notificacao.md).

### [ADR 0001: Estruturas de Dados do Servidor Concorrente e Descarte de Chat](../adr/SOCKET-0001-servidor.md)
- **Status:** Aceito (com escopo revisado).
- **Contexto:** Necessidade de gerenciar conexoes WebSocket simultaneas em Java Puro de forma thread-safe sem travar o processamento com locks globais.
- **Decisao:** Uso de `ConcurrentHashMap` para mapeamento de sessoes por usuario (`userId -> Set<ConnectionHandler>`) e filas `LinkedBlockingQueue` para isolamento de I/O.
- **Decisao Pensada e Descartada:** Cogitou-se criar salas de chat em tempo real com `ConcurrentLinkedDeque` e filas volateis de SLA (`DelayQueue`). Essa abordagem foi descartada devido ao risco de inconsistencia em conteineres efemeros de nuvem. O historico foi consolidado na tabela relacional `ticket_timeline` e o socket focou na distribuicao confiavel de eventos de ciclo de vida dos chamados.

### [ADR 0002: Notificacoes Push em Tempo Real](../adr/SOCKET-0002-notificacao.md)
- **Status:** Aceito.
- **Contexto:** Necessidade de alertar imediatamente clientes e tecnicos sobre movimentacoes em seus chamados sem recurso a polling HTTP continuo.
- **Decisao:** Disparo de eventos leves (Push Ping) via WebSocket direcionados pelo perfil e departamento do usuario, acionando incremento numerico de badge e feedback sonoro (*ping sound*) no frontend. Persistencia definitiva delegada a tabela `notifications` do banco de dados relacional.

---

## Apendice B — Historico Semantico de Versoes (Changelog)

Para o registro exaustivo de todos os patches, correcoes e novidades em formato semantico estrito, consulte o arquivo **[`CHANGELOG.md`](../CHANGELOG.md)** na raiz do projeto.

- **v1.0.0 (2026-11-23):** Release oficial final com ciclo CRUD completo em tempo real, SonarCloud Rating 'A', suite de testes automatizados com JUnit 5 no backend-socket e resiliencia em nuvem.
- **v0.5.0 (2026-11-18):** Implementacao do protocolo WebSocket RFC 6455 em Java 21 Puro, politicas de departamento e inicio atomico de atendimento.
- **v0.4.3 (2026-11-14):** Health Check publico e prevencao de suspensao por cold start no Render.
- **v0.4.1 (2026-11-05):** Integracao full-stack com banco Neon PostgreSQL, i18n e camada reativa de dados.
- **v0.4.0 (2026-10-25):** Estruturacao dos DAOs JDBC, controladores HTTP REST e permissões RBAC.
- **v0.1.0 a v0.3.0:** Concepcao inicial, prototipagem de interface e especificacao de requisitos.
