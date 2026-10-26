# HelpDesk — Sistema de Chamados de TI

> **Projeto Integrador IV** — Sistema de gerenciamento de chamados de suporte técnico com painel administrativo, fluxo de status em tempo real via WebSocket nativo e notificações push orientadas a eventos.

<p align="center">
  <img src="https://img.shields.io/badge/Vers%C3%A3o-v0.5.0-00B4D8?style=for-the-badge&logo=git&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21_Puro-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSocket-RFC_6455-10B981?style=for-the-badge&logo=socketdotio&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Multi--Stage-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <a href="https://help-desk-pi-iv.vercel.app" target="_blank">
    <img src="https://img.shields.io/badge/Acessar_Aplicação-Vercel-000?style=for-the-badge&logo=vercel&logoColor=white" />
  </a>
</p>

---

## Sumário

- [Acesso à Aplicação](#acesso-à-aplicação)
- [Sobre o Projeto](#sobre-o-projeto)
  - [Fluxo e Ciclo de Vida dos Chamados](#fluxo-e-ciclo-de-vida-dos-chamados)
- [Status Atual do Projeto (v0.5.0)](#status-atual-do-projeto-v050)
  - [O que estamos fazendo hoje](#o-que-estamos-fazendo-hoje)
  - [Descarte de Ideias e Alinhamento de Escopo](#descarte-de-ideias-e-alinhamento-de-escopo)
- [Histórico de Versões (Changelog)](#histórico-de-versões-changelog)
- [Arquitetura do Sistema](#arquitetura-do-sistema)
  - [Diagrama de Contêineres (C4 Nível 2)](#diagrama-de-contêineres-c4-nível-2)
  - [Contratos de Integração em Tempo Real](#contratos-de-integração-em-tempo-real)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Documentação do Projeto](#documentação-do-projeto)
- [Equipe e Licença](#equipe-e-licença)

---

## Acesso à Aplicação

O sistema está publicado e acessível diretamente pelo navegador:

👉 **[https://help-desk-pi-iv.vercel.app](https://help-desk-pi-iv.vercel.app)**

---

## Sobre o Projeto

Sistema Help Desk corporativo voltado para ambientes corporativos e educacionais, desenvolvido sob os seguintes pilares operacionais:

- **Clientes / Solicitantes**: Abertura rápida de chamados, visualização restrita ao escopo do seu departamento, acompanhamento de status em tempo real e histórico auditado.
- **Técnicos de TI**: Visualização da fila centralizada de chamados, controle do ciclo de atendimento, início atômico de atendimento (`startService`), alteração de status e registro de ocorrências na timeline.
- **Administradores**: Gestão de usuários, gerenciamento de categorias, métricas de SLA e controle de acesso baseado em papéis e departamentos (RBAC + Department Scope).

### Fluxo e Ciclo de Vida dos Chamados

O ciclo de vida garante integridade operacional: chamados novos criados por solicitantes permanecem em estado `NOVO` (`NEW`) até que um técnico do departamento de TI inicie formalmente o atendimento.

```
┌─────────────────┐       Técnico de TI Inicia Atendimento       ┌─────────────────┐
│      NOVO       │ ───────────────────────────────────────────> │    ATRIBUÍDO    │
│ (Aguardando TI) │   (POST /api/chamados/:id/iniciar - Atômico)  │ (Em Atendimento)│
└─────────────────┘                                              └────────┬────────┘
                                                                          │
                                     ┌────────────────────────────────────┴────────────────────────────────────┐
                                     │                                                                         │
                                     ▼ Conclusão do Atendimento                                               ▼ Impedimento / Recusa
                           ┌─────────────────┐                                                       ┌─────────────────┐
                           │     FECHADO     │                                                       │  NÃO RESOLVIDO  │
                           │   (Resolvido)   │                                                       │ (Não Concluído) │
                           └─────────────────┘                                                       └─────────────────┘
```

---

## Status Atual do Projeto (v0.5.0)

> **Módulo Backend Socket ativo com WebSocket RFC 6455 em Java puro, isolamento de escopo por departamento, início de atendimento atômico e formalização de decisões arquiteturais (ADRs).**

### O que estamos fazendo hoje

1. **Ativação e Implementação do Backend Socket (Java Puro & RFC 6455)**:
   - Implementação do servidor de sockets sobre `java.net.ServerSocket` nativo sem bibliotecas de terceiros (`WebSocketUtil.java` e `Main.java`).
   - **Handshake HTTP -> WebSocket**: Detecção de `Sec-WebSocket-Key` e negociação formal via status `101 Switching Protocols` utilizando hash SHA-1 e codificação Base64 com o Magic GUID (`258EAFA5-E914-47DA-95CA-C5AB0DC85B11`).
   - **Frames RFC 6455**: Codificação e decodificação de frames mascarados do navegador, suporte a payloads estendidos (16 e 64 bits), e tratamento de frames de controle (`Ping`, `Pong` e `Close`).
   - **Health Check & Keep-Alive HTTP**: Atendimento na porta única a requisições `GET /health` e `GET /` com resposta `200 OK` para prevenção de *cold start* no Render.
   - **Pipeline de Integração Contínua (CI)**: Configuração de GitHub Actions no repositório dedicado `pi4-2026-2-0102-grupo2-backend-socket` que sincroniza automaticamente o código-fonte com o repositório principal.

2. **Políticas de Departamento & Controle de Acesso (Backend API)**:
   - Nova política de departamento (`DepartmentPolicy.java`):
     - **Isolamento de Visibilidade (`canViewTicket`)**: Usuários comuns agora só podem visualizar chamados cujo solicitante ou técnico pertença ao seu mesmo departamento.
     - **Privilégios de Gerenciamento (`canManageTickets`)**: Exclusividade para usuários vinculados ao departamento de `"TI"` para gerenciar chamados e iniciar atendimentos.
     - **Notificações Filtradas**: Notificações restritas aos chamados visíveis pelo departamento do usuário.

3. **Ciclo de Vida de Chamados & Início de Atendimento Atômico**:
   - Correção do fluxo de criação: chamados recém-abertos persistem obrigatoriamente como status `NEW` até intervenção da TI.
   - Endpoint dedicado: `POST /api/chamados/:id/iniciar` acionado quando o técnico abre ou inicia o chamado.
   - Operação atômica em PostgreSQL via Common Table Expression (CTE) no `TicketDao.startService`:
     ```sql
     WITH started AS (
       UPDATE tickets SET status = 'ASSIGNED', support_id = ?, updated_at = CURRENT_TIMESTAMP 
       WHERE id = ? AND status = 'NEW' RETURNING id
     )
     INSERT INTO ticket_timeline (ticket_id, author_id, event_type, message)
     SELECT id, ?, 'STATUS_CHANGE', ? FROM started
     ```
     *Garante que dois técnicos abrindo simultaneamente o mesmo chamado nunca gerem duplicidade de atendimento.*
   - Propagação estrita de exceções SQL e garantia transacional em atualizações (`TicketDao.update`).

4. **Sincronização Unificada da Documentação**:
   - Integração das **ADRs (Architecture Decision Records)**: ADR 0001 (Estruturas Concorrentes) e ADR 0002 (Notificações Push).
   - Mapeamento detalhado dos 9 entregáveis oficiais do projeto (**E1 a E9**) e contratos de integração (**CT18, CT19, CT22**).

---

### Descarte de Ideias e Alinhamento de Escopo

Durante a evolução do projeto, decisões de arquitetura conscientes foram tomadas para evitar complexidade acidental, garantir a entrega no prazo e priorizar a estabilidade:

| Ideia / Abordagem Inicial | Decisão | Justificativa Técnica & Solução Adotada |
|:---|:---:|:---|
| **Chat em Tempo Real por Chamado (Estilo Teams / Slack)** | ❌ **DESCARTADO** | **ADR 0001:** Estruturar salas por chamado (`ticketId` → conexões), buffers de mensagens em memória (`ConcurrentLinkedDeque`), filas de prioridade (`PriorityBlockingQueue`) e controle de SLA via `DelayQueue` geraria alto risco de inconsistência em caso de reinicialização de contêineres.<br>👉 **Solução adotada:** O servidor Socket foca estritamente na entrega confiável de eventos de chamados (`chamadoAtualizado` no Kanban / CT22) e notificações push leves (ADR 0002). O histórico de interações e auditoria permanece seguro e transacional na tabela `ticket_timeline` via REST API. |
| **Polling HTTP Repetitivo no Frontend para Notificações** | ❌ **DESCARTADO** | **ADR 0002:** Requisições periódicas a cada poucos segundos degradam o banco Neon e aumentam a latência percebida.<br>👉 **Solução adotada:** Push Ping orientado a eventos via WebSocket com direcionamento por `userId`, disparando feedback sonoro (*ping sound*) e badge visual no sino do header. |
| **Uso de Bibliotecas Pesadas para WebSocket (ex: Netty / Jetty)** | ❌ **DESCARTADO** | Para atender às premissas acadêmicas do Projeto Integrador de uso de **Java Puro**, bibliotecas externas pesadas foram descartadas.<br>👉 **Solução adotada:** Implementação enxuta da RFC 6455 diretamente sobre `ServerSocket`, `InputStream` e `OutputStream` em ~220 linhas de código nativo. |
| **Filas de SLA e Prioridade Voláteis em Memória** | ❌ **DESCARTADO** | Filas em memória não sobrevivem a restarts em ambientes de nuvem efêmeros (como Render free tier).<br>👉 **Solução adotada:** Gestão de estados e métricas de SLA calculadas diretamente no PostgreSQL a partir de timestamps persistidos (`created_at`, `updated_at`, `closed_at`). |

---

## Histórico de Versões (Changelog)

### [v0.5.0] — Implementação do Backend Socket (RFC 6455), Políticas de Departamento e Ciclo de Vida Atômico
*Versão atual*

- **Backend-Socket (v0.3.0 no módulo dedicado)**:
  - Implementação completa de `WebSocketUtil.java` para protocolo RFC 6455 em Java puro.
  - Handshake HTTP 1.1 -> WebSocket (`101 Switching Protocols`) com cálculo SHA-1/Base64 da `Sec-WebSocket-Key`.
  - Tratamento de frames com máscara (clientes Web), decodificação de payloads e frames de controle (`Ping`, `Pong`, `Close`).
  - Suporte nativo a Health Check HTTP (`/health`, `/`) respondendo com `200 OK` na porta única.
  - Servidor `Main.java` com `ExecutorService` desacoplado para atendimento multithread de conexões.
  - Adição de workflow GitHub Actions para sincronização automatizada com o monorepo.
- **Backend-API**:
  - Implementação de `DepartmentPolicy`: isolamento de chamados e notificações com base no departamento do usuário (`canViewTicket`).
  - Exclusividade do departamento de `TI` para gestão e atendimento de chamados (`canManageTickets`).
  - Novo endpoint `POST /api/chamados/:id/iniciar` com execução atômica via CTE no PostgreSQL (`TicketDao.startService`).
  - Correção: chamados recém-criados permanecem com status `NEW` até o aceite formal da TI.
  - Propagação correta de erros em operações de persistência (`TicketDao.update`).
- **Documentação & Alinhamento**:
  - Sincronização de `docs/adr/0001-servidor.md` e `docs/adr/0002-notificacao.md`.
  - Inclusão dos fluxos de entrega `docs/fluxos/` cobrindo E1 a E9 e contratos CT18, CT19 e CT22.
  - Registro explícito do descarte de chat em tempo real e foco na orquestração de eventos.

### [v0.4.3] — Health Check Público e Prevenção de Cold Start no Render

- **Backend-API**:
  - Implementado `HealthHandler` com endpoints públicos `/health`, `/api/health` e `/`.
  - Suporte aos métodos `GET` e `HEAD` com resposta `200 OK` contendo status da API, versão e link do frontend.
  - Eliminação de erro 404 (*No context found for request*) na raiz do deploy no Render.
  - Habilitada integração com serviços de Cron Jobs / Keep-Alive externos para evitar *cold start* no plano gratuito do Render.

### [v0.4.2] — Documentação de Escopo e Identificação da Equipe

- **Documentação**:
  - Adicionado documento oficial de escopo do projeto em `docs/escopo.pdf`.
- **README**:
  - Inclusão dos membros da equipe com RAs na seção Equipe e Licença.

### [v0.4.1] — Integração Full-Stack Neon DB, i18n e Dashboard Reativo

- **Backend**:
  - Adicionado endpoint `DELETE /api/chamados/:id` com exclusão em cascata transacional (timeline → notificações → ticket).
- **Frontend — Integração com API**:
  - Módulo `api.js`: comunicação centralizada com o backend (fetch + Bearer token + mapeamento + notificações).
  - Todas as operações CRUD persistem no banco via API REST.
- **Frontend — Internacionalização (i18n)**:
  - Módulo `i18n.js`: sistema de tradução global EN → PT-BR.
- **Dashboard e Data Layer Responsivos com Neon DB**:
  - `data.js` completamente reestruturado para ser 100% reativo com o Neon DB.
  - Remoção de status e métricas artificiais; cálculo direto de indicadores reais.

### [v0.4] — Backend-API Pronto, Integrado e Regras de Negócio de Chamados

- Implementação completa dos DAOs com JDBC nativo e Handlers HTTP REST.
- Suporte a CORS completo e padronização de rotas com prefixo `/api`.
- Interface com auto-atribuição ("Me Atribuir"), permissões RBAC e tokens de design.

---

## Arquitetura do Sistema

### Diagrama de Contêineres (C4 Nível 2)

O sistema opera de forma desacoplada entre a API de dados e o serviço de mensageria em tempo real:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Cliente (Navegador)                           │
│                     HTML5 / CSS3 Vanilla / JS ES6+                     │
└───────────────┬────────────────────────────────────────┬───────────────┘
                │ Ações & Consultas CRUD                 │ WebSocket Seguro (WSS)
                │ HTTPS REST (:8000)                     │ Sessão & Eventos
                ▼                                        ▼
┌──────────────────────────────────────┐   ┌─────────────────────────────┐
│       Contêiner 1: Backend API       │   │ Contêiner 2: Backend Socket │
│        (com.sun.net.httpserver)      │   │   (java.net.ServerSocket)   │
│   Auth, CRUD, DAOs, Regras e SLA     │   │   RFC 6455 & Notificações   │
└───────────────┬──────────────────────┘   └─────────────▲───────────────┘
                │                                        │
                │ Eventos de Chamados (CT18)             │
                └────────────────────────────────────────┘
                │ TCP Interno (Porta 5001) com INTERNAL_SECRET
                │
                │ JDBC / TLS                             │ Validação de Token &
                ▼                                        ▼ Perfil de Usuário
┌────────────────────────────────────────────────────────────────────────┐
│                  Banco de Dados Relacional Externo                     │
│                       PostgreSQL 15+ no Neon                           │
└────────────────────────────────────────────────────────────────────────┘
```

### Contratos de Integração em Tempo Real

A comunicação entre módulos obedece às especificações documentadas em `docs/fluxos/`:

| Contrato | Nome | Fronteira | Descrição do Fluxo | Atende aos Entregáveis |
|:---:|:---|:---:|:---|:---:|
| **CT18** | `publicarEvento` | TCP Interno (:5001) | **Backend API → Backend Socket**: Disparado logo após gravação de novo chamado ou alteração de status no banco. Transmite payload autenticado por `INTERNAL_SECRET`. | E4, E5 |
| **CT19** | `autenticarConexao` | WebSocket (:5000) | **Frontend → Backend Socket**: Primeira mensagem enviada após a abertura do socket. Valida token JWT/HMAC e lê o perfil na tabela `users`. | E4, E5, E6 |
| **CT22** | `chamadoAtualizado` | WebSocket (:5000) | **Backend Socket → Frontend**: Notifica em tempo real os painéis do solicitante, do técnico atribuído e de todos os membros de suporte/admin conectados. | E4, E5 |

---

## Stack Tecnológica

| Camada | Tecnologia | Detalhes |
|:---|:---|:---|
| **Frontend** | HTML5, CSS3 (Vanilla), JS (ES6+) | SPA nativa sem frameworks, design system com tema dark/light, módulo de i18n |
| **Backend API** | Java 21 Puro | `com.sun.net.httpserver`, JDBC Nativo, DAOs estruturados, sem ORM |
| **Backend Socket** | Java 21 Puro | `java.net.ServerSocket`, RFC 6455 nativa, concorrência thread-safe |
| **Banco de Dados** | PostgreSQL 15+ | Hospedado na nuvem no Neon.tech com conexões seguras TLS |
| **Serialização JSON**| Google Gson 2.11.0 | Serialização de DTOs, entidades e mensagens de eventos |
| **Containerização** | Docker | Imagens multi-stage leves baseadas em Alpine Linux |
| **Hospedagem & Deploy**| Vercel + Render + Neon | Vercel (Frontend) · Render (API REST & Socket) · Neon (PostgreSQL) |

---

## Estrutura do Projeto

```
Help-Desk-PI-IV/
├── frontend/                   # Interface do Usuário (SPA)
│   ├── index.html              # Dashboard e painel principal
│   ├── login.html              # Tela de login
│   ├── register.html           # Tela de cadastro de novos usuários
│   ├── css/
│   │   ├── variables.css       # Tokens, paleta e temas (light/dark)
│   │   ├── base.css            # Reset e tipografia global
│   │   ├── layout.css          # Grid e estrutura da SPA
│   │   ├── components.css      # Botões, tabelas, cards, modais e badges
│   │   ├── animations.css      # Animações e micro-interações
│   │   └── login.css           # Estilos das telas de autenticação
│   └── js/
│       ├── data.js             # Estado global reativo com Neon DB
│       ├── i18n.js             # Sistema de tradução EN → PT-BR
│       ├── api.js              # Cliente HTTP REST centralizado
│       ├── theme.js            # Controle de tema light/dark
│       ├── router.js           # Roteamento SPA client-side
│       ├── components.js       # Renderização dinâmica dos componentes
│       ├── auth.js             # Gestão de token, login e cadastro
│       └── app.js              # Controlador central e regras de negócio
├── backend-api/                # API REST em Java Puro (Contêiner 1)
│   ├── Dockerfile              # Build multi-stage para deploy
│   ├── lib/
│   │   ├── postgresql.jar      # Driver JDBC PostgreSQL 42.7.3
│   │   └── gson.jar            # Gson 2.11.0
│   └── src/com/helpdesk/api/
│       ├── Main.java           # Ponto de entrada e rotas HTTP
│       ├── config/
│       │   └── Database.java   # Pool e conexão JDBC com variáveis de ambiente
│       ├── dao/                # Camada de persistência (SQL nativo)
│       │   ├── CategoryDao.java
│       │   ├── NotificationDao.java
│       │   ├── TicketDao.java  # Operações atômicas de status e CTE startService
│       │   ├── TicketTimelineDao.java
│       │   └── UserDao.java
│       ├── handler/            # Controladores HTTP (HttpHandler)
│       │   ├── CategoryHandler.java
│       │   ├── HealthHandler.java
│       │   ├── LoginHandler.java
│       │   ├── NotificationHandler.java
│       │   ├── TicketHandler.java
│       │   └── UserHandler.java
│       ├── model/              # Modelos de domínio
│       │   ├── Category.java
│       │   ├── Notification.java
│       │   ├── Ticket.java
│       │   ├── TicketTimeline.java
│       │   └── User.java
│       └── util/               # Utilitários auxiliares
│           ├── AuthUtil.java
│           ├── DepartmentPolicy.java # Regras de visibilidade e gestão por departamento
│           ├── HttpHelper.java
│           └── JsonUtil.java
├── backend-socket/             # Servidor WebSocket RFC 6455 em Java Puro (Contêiner 2)
│   ├── Dockerfile              # Build multi-stage do Socket
│   ├── lib/
│   │   ├── postgresql.jar
│   │   └── gson.jar
│   └── src/com/helpdesk/socket/
│       ├── Main.java           # Listener ServerSocket e pool de conexões
│       └── util/
│           └── WebSocketUtil.java # Handshake RFC 6455, codificação/decodificação de frames
├── docs/                       # Documentação de Arquitetura, ADRs e Fluxos
│   ├── adr/
│   │   ├── 0001-servidor.md    # Estruturas concorrentes e descarte de chat
│   │   └── 0002-notificacao.md # Notificações push em tempo real
│   ├── fluxos/                 # Especificação dos Entregáveis E1 a E9 e Contratos CT18, CT19, CT22
│   ├── backend_architecture.md
│   ├── database_modeling.md
│   └── escopo.pdf              # Documento oficial de escopo do projeto
└── .gitignore                  # Arquivos ignorados
```

---

## Banco de Dados

Modelagem relacional em 5 tabelas normalizadas no PostgreSQL:

| Tabela | Finalidade |
|:---|:---|
| `users` | Usuários do sistema (`CLIENT`, `SUPPORT`, `ADMIN`), departamento e hash de senha |
| `categories` | Categorias técnicas de chamados para classificação |
| `tickets` | Chamados de suporte com controle de status (`NEW`, `ASSIGNED`, `CLOSED`, `UNRESOLVED`), prioridade e responsáveis |
| `ticket_timeline` | Histórico cronológico auditável de alterações, transferências e mensagens |
| `notifications` | Notificações do sistema persistidas por usuário |

---

## Documentação do Projeto

A documentação detalhada encontra-se na pasta [`docs/`](./docs/):

- **[Visão Geral e Fluxos de Atendimento](./docs/fluxos/visao-geral.md)**: Mapeamento dos entregáveis E1 a E9 e arquitetura C4 Nível 2.
- **[ADR 0001 — Estruturas Concorrentes do Servidor](./docs/adr/0001-servidor.md)**: Decisões técnicas de concorrência e justificativa do descarte do chat em tempo real.
- **[ADR 0002 — Notificações em Tempo Real](./docs/adr/0002-notificacao.md)**: Mecânica do Push Ping via WebSocket e feedback visual/sonoro.
- **[Modelagem do Banco de Dados](./docs/database_modeling.md)**: Esquema SQL, tipos de dados e constraints.
- **[Escopo Oficial do Projeto](./docs/escopo.pdf)**: Especificação de requisitos original.

---

## Equipe e Licença

* **Cauã Vasconcelos Grecco De Faria (25006367)**
* **Rafael Trevisan (25002001)**
* **Vinicius Fortes Heinzl (25008058)**
* **Pedro Henrique Vieira Lima (25018202)** 
* **Kaue Rodrigues Seixas (23011884)**

**Projeto Integrador IV** — Curso de Análise e Desenvolvimento de Sistemas.  
Este projeto é de caráter acadêmico e livre para fins de estudo.
