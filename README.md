# HelpDesk — Sistema de Chamados de TI

> **Projeto Integrador IV** — Sistema de gerenciamento de chamados de suporte técnico com painel administrativo, fluxo de status em tempo real e notificações via Socket TCP.

<p align="center">
  <img src="https://img.shields.io/badge/Vers%C3%A3o-v0.4.1-00B4D8?style=for-the-badge&logo=git&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-Puro-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <a href="https://help-desk-pi-iv.vercel.app" target="_blank">
    <img src="https://img.shields.io/badge/Acessar_Aplicação-Vercel-000?style=for-the-badge&logo=vercel&logoColor=white" />
  </a>
</p>

---

## Sumário

- [Acesso à Aplicação](#acesso-à-aplicação)
- [Sobre o Projeto](#sobre-o-projeto)
  - [Fluxo de Status dos Chamados](#fluxo-de-status-dos-chamados)
- [Status Atual (v0.4.1)](#status-atual-v041)
- [Histórico de Versões (Changelog)](#histórico-de-versões-changelog)
- [Arquitetura](#arquitetura)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Equipe e Licença](#equipe-e-licença)

---

## Acesso à Aplicação

O sistema está publicado e acessível diretamente pelo navegador:

👉 **[https://help-desk-pi-iv.vercel.app](https://help-desk-pi-iv.vercel.app)**

---

## Sobre o Projeto

Sistema Help Desk corporativo voltado para ambientes corporativos e educacionais que permite:

- **Clientes / Solicitantes**: Abertura rápida de chamados, acompanhamento de status em tempo real e histórico de solicitações.
- **Técnicos de TI**: Visualização da fila de chamados, auto-atribuição ("Me Atribuir"), atualização de status, registro de atendimentos e resolução.
- **Administradores**: Gestão de usuários, gerenciamento de categorias, métricas de SLA e controle de acesso baseado em papéis (RBAC).

### Fluxo de Status dos Chamados

```
┌─────────┐      Atribuir Técnico      ┌───────────────┐      Concluir      ┌────────────┐
│  NOVO   │  ───────────────────────>  │   ATRIBUÍDO   │  ───────────────>  │  FECHADO   │
│ (novo)  │                            │  (atribuido)  │                    │ (fechado)  │
└─────────┘                            └───────┬───────┘                    └────────────┘
                                               │
                                               │ Impedimento / Pendência
                                               ▼
                                       ┌───────────────┐
                                       │ NÃO RESOLVIDO │
                                       │(nao_resolvido)│
                                       └───────────────┘
```

---

## Status Atual (v0.4.1)

> **Frontend totalmente integrado com o Backend — CRUD de chamados persiste no Neon PostgreSQL.**

- **CRUD via API REST**: Todas as operações de chamados (criar, listar, editar, deletar) passam pela API Java e persistem no banco de dados Neon.
- **Endpoint DELETE**: Backend agora suporta exclusão de chamados com limpeza cascata (timeline + notificações + ticket).
- **Dashboard 100% responsivo com Neon DB**: Métricas de chamados (total, novos/aguardando, atribuídos a técnicos, resolvidos, não solucionados, taxa de resolução real e distribuição por categorias do banco) calculadas puramente a partir dos dados persistidos no PostgreSQL, sem status inventados no JS ou trends artificiais.
- **Variáveis em inglês**: Todo o código frontend usa nomes em inglês (`title`, `status`, `priority`, `createdAt`, etc.).
- **Sistema de tradução (i18n)**: Módulo `i18n.js` garante que a interface continue em PT-BR para o usuário final, mapeando os ENums do banco para visualização.
- **Módulo API centralizado**: `api.js` encapsula todas as chamadas HTTP com autenticação via Bearer token, incluindo notificações e endpoints de chamados.
- **Autenticação & Sessão**: Login e registro com validação, geração de token e mapeamento no frontend.
- **Regras de Negócio de Chamados**:
  - Todo novo chamado é criado obrigatoriamente com o status `NEW`.
  - O solicitante é vinculado automaticamente ao usuário autenticado. Apenas administradores e técnicos podem alterar o solicitante na edição.
  - Alocação de técnico avança automaticamente o status para `ASSIGNED`.
  - Técnicos podem se auto-atribuir ao chamado com apenas um clique pelo botão **"Me Atribuir"**.
  - Prioridade padrão definida como `MEDIUM` caso não selecionada.
  - Categorias carregadas dinamicamente do banco de dados Neon.
  - Controle de visualização e edição de campos por perfil (RBAC: Cliente vs TI).

---

## Histórico de Versões (Changelog)

### [v0.4.1] — Integração Full-Stack Neon DB, i18n e Dashboard Reativo
*Versão atual*

- **Backend**:
  - Adicionado endpoint `DELETE /api/chamados/:id` com exclusão em cascata transacional (timeline → notificações → ticket).
- **Frontend — Integração com API**:
  - Novo módulo `api.js`: comunicação centralizada com o backend (fetch + Bearer token + mapeamento de dados + notificações).
  - Todas as operações CRUD (criar, editar, deletar chamados) agora persistem no banco via API REST.
  - Após cada operação de escrita, os dados são recarregados do backend (`reloadTickets()`).
- **Frontend — Internacionalização (i18n)**:
  - Novo módulo `i18n.js`: sistema de tradução global EN → PT-BR.
  - Variáveis internas migradas para inglês (`title`, `status`, `priority`, `createdAt`, etc.).
  - Interface do usuário permanece 100% em português via `i18n.t()`, `i18n.status()`, `i18n.priority()`.
- **Dashboard e Data Layer Responsivos com Neon DB**:
  - `data.js` completamente reestruturado para ser 100% reativo e fiel ao modelo de dados do Neon DB.
  - Removidos status inexistentes no banco (como `IN_PROGRESS` e `PENDING`) e trends fake (+12%, etc.).
  - Dashboard calcula métricas reais: Total, Novos (aguardando), Atribuídos (em atendimento), Resolvidos e Não Solucionados.
  - Gráficos de distribuição por status e por categorias reais do banco de dados Neon.
  - Badges da sidebar e sino de notificações reativos, atualizados automaticamente em cada ação CRUD.
  - Botão de recarga e indicador de status de conexão com a API no topo do Dashboard.

### [v0.4] — Backend-API Pronto, Integrado e Regras de Negócio de Chamados

- **Backend-API**:
  - Implementação completa dos DAOs (`UserDao`, `TicketDao`, `CategoryDao`, `NotificationDao`, `TicketTimelineDao`) com JDBC nativo.
  - Implementação dos Handlers HTTP REST (`LoginHandler`, `UserHandler`, `TicketHandler`, `CategoryHandler`, `NotificationHandler`).
  - Suporte completo a CORS com preflight `OPTIONS` e padronização de rotas com prefixo `/api` e raiz.
  - Tratamento de variáveis de ambiente para conexão segura ao banco PostgreSQL (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).
- **Frontend & Regras de Negócio**:
  - **Criação de Chamado**: Interface limpa exibindo apenas Título, Descrição, Categoria e Prioridade. O solicitante é herdado da sessão e campos de técnico/status foram omitidos na criação.
  - **Auto-Atribuição**: Botão "Me Atribuir" no card de ações rápidas e detalhe do chamado para técnicos de suporte.
  - **Status Automático**: Ao atribuir um técnico a um chamado em status "novo", o sistema atualiza automaticamente para "atribuído".
  - **Permissões (RBAC)**: Apenas usuários com cargo de TI (técnico/admin) visualizam e editam técnico, status e solicitante no modal de chamado.
  - **Prioridade e Categorias**: Prioridade "Normal" adicionada como padrão; 8 categorias padrão integradas ao dropdown.
  - **Design Tokens**: Adição de estilos CSS e badges para status `novo` e `atribuído` e prioridade `normal`.

### [v0.3] — Deploy Cloud & Autenticação Integrada
- Tela de cadastro (`register.html`) e integração completa com `login.html`.
- Migração de endpoints para deploy em nuvem e banco de dados Neon.tech.
- Mapeamento de autenticação no frontend via `localStorage` e redirecionamento de rotas protegidas.
- Remoção de dados fictícios em favor da integração com a API.
- Correção de pré-flight CORS e variáveis de ambiente no deploy.

### [v0.2] — Estrutura Backend, Docker e Modelagem Relacional
- Estruturação dos módulos `backend-api` (API REST) e `backend-socket` (Servidor Socket TCP).
- Inclusão das bibliotecas compiladas na pasta `lib/`: Driver JDBC PostgreSQL 42.7.3 e Gson 2.11.0.
- Criação dos `Dockerfile`s com multi-stage build para compilação e execução leve com OpenJDK.
- Modelagem do banco de dados relacional documentada em `docs/database_modeling.md` e arquitetura em `docs/backend_architecture.md`.
- Definição das tabelas centrais: `users`, `categories`, `tickets`, `ticket_timeline` e `notifications`.

### [v0.1] — Frontend Base & Design System SPA
- Design System completo com variáveis CSS (Light e Dark mode, paleta de cores corporativa, tipografia moderna).
- Layout responsivo com sidebar retrátil, header com ações rápidas e grid adaptável.
- Dashboard com cards de métricas (Total, Abertos, Em Andamento, Concluídos, Críticos).
- Sistema de notificações interativo no header com dropdown, contador de não lidas e marcação rápida.
- Dropdown de perfil de usuário com informações de sessão.
- Telas de listagem em tabela e cards com busca e filtros por status/prioridade.

---

## Arquitetura

O sistema é concebido para operar em contêineres independentes e leves:

```
┌─────────────────────────────────────────────────────────┐
│                    Cliente (Browser)                    │
│              HTML5 / CSS3 Vanilla / JS ES6+             │
└──────────┬──────────────────────────────┬───────────────┘
           │ HTTP/REST (:8000)            │ TCP Socket (:5000)
           ▼                              ▼
┌─────────────────────┐    ┌─────────────────────────────┐
│  Contêiner 1: API   │    │  Contêiner 2: Socket Server │
│  (com.sun.httpserver)│    │  (java.net.ServerSocket)    │
│  CRUD + Auth + DAOs │    │  Tempo real + Notificações  │
└──────────┬──────────┘    └──────────────┬──────────────┘
           │ JDBC                          │ JDBC
           ▼                               ▼
┌─────────────────────────────────────────────────────────┐
│            Contêiner 3: PostgreSQL (:5432)              │
│                 (Local ou Neon.tech)                    │
└─────────────────────────────────────────────────────────┘
```

---

## Stack Tecnológica

| Camada | Tecnologia | Detalhes |
|:---|:---|:---|
| **Frontend** | HTML5, CSS3 (Vanilla), JS (ES6+) | SPA sem frameworks, design system próprio, dark/light mode |
| **Backend API** | Java Puro (OpenJDK 17+) | `com.sun.net.httpserver`, JDBC Nativo, DAOs, sem ORM |
| **Backend Socket** | Java Puro (OpenJDK 17+) | `java.net.ServerSocket` com gerenciamento de threads |
| **Banco de Dados** | PostgreSQL 15+ | Hospedado no Neon.tech (cloud) / PostgreSQL local |
| **Serialização JSON**| Google Gson 2.11.0 | Conversão de DTOs e entidades |
| **Containerização** | Docker | Builds leves multi-stage |
| **Deploy** | Vercel + Neon | Vercel (Frontend) · Neon (Banco de Dados) |

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
│       ├── data.js             # Estado global e helpers (populado via API)
│       ├── i18n.js             # Sistema de tradução EN → PT-BR
│       ├── api.js              # Comunicação HTTP com o backend
│       ├── theme.js            # Controle de tema light/dark
│       ├── router.js           # Roteamento SPA client-side
│       ├── components.js       # Renderização dinâmica dos componentes
│       ├── auth.js             # Gestão de token, login e cadastro
│       └── app.js              # Controlador central e regras de negócio
├── backend-api/                # API REST em Java Puro (Contêiner 1)
│   ├── Dockerfile              # Dockerfile de produção
│   ├── lib/
│   │   ├── postgresql.jar      # Driver JDBC PostgreSQL 42.7.3
│   │   └── gson.jar            # Gson 2.11.0
│   └── src/com/helpdesk/api/
│       ├── Main.java           # Ponto de entrada e registro de rotas
│       ├── config/
│       │   └── Database.java   # Conexão JDBC com pooling e env vars
│       ├── dao/                # Camada de persistência (SQL nativo)
│       │   ├── CategoryDao.java
│       │   ├── NotificationDao.java
│       │   ├── TicketDao.java
│       │   ├── TicketTimelineDao.java
│       │   └── UserDao.java
│       ├── handler/            # Controladores HTTP (HttpHandler)
│       │   ├── CategoryHandler.java
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
│           ├── HttpHelper.java
│           └── JsonUtil.java
├── backend-socket/             # Servidor de Socket TCP (Contêiner 2 - a implementar)
│   ├── Dockerfile
│   └── lib/
├── docs/                       # Documentação de arquitetura e banco
│   ├── backend_architecture.md
│   └── database_modeling.md
└── .gitignore                  # Arquivos ignorados (segurança e envs)
```

---

## Banco de Dados

Modelagem relacional em 5 tabelas normalizadas:

| Tabela | Finalidade |
|:---|:---|
| `users` | Usuários do sistema (`CLIENT`, `SUPPORT`, `ADMIN`) com hash de senha |
| `categories` | Categorias técnicas de chamados |
| `tickets` | Chamados de suporte com controle de status, prioridade e responsáveis |
| `ticket_timeline` | Histórico cronológico de alterações e interações |
| `notifications` | Notificações do sistema para os usuários |

> O dicionário de dados completo e os relacionamentos estão detalhados em [`docs/database_modeling.md`](./docs/database_modeling.md).

---

## Equipe e Licença
* **Cauã Vasconcelos Grecco De Faria (25006367)**
* **Rafael Trevisan (25002001)**
* **Vinicius Fortes Heinzl (25008058)**
* **Pedro Henrique Vieira Lima (25018202)** 
* **Kaue Rodrigues Seixas (23011884)**

**Projeto Integrador IV** — Curso de Análise e Desenvolvimento de Sistemas.  
Este projeto é de caráter acadêmico e livre para fins de estudo.
