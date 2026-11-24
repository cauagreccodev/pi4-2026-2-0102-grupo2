# HelpDesk — Sistema de Gerenciamento de Chamados de TI

> **Projeto Integrador IV** — Sistema corporativo de gestao de chamados de suporte tecnico com painel administrativo, fluxo de status atômico, atualizacao em tempo real via WebSocket nativo (RFC 6455 em Java Puro) e notificacoes push orientadas a eventos.

<p align="center">
  <img src="https://img.shields.io/badge/Versao-v1.0.0-00B4D8?style=for-the-badge&logo=git&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21_Puro-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSocket-RFC_6455-10B981?style=for-the-badge&logo=socketdotio&logoColor=white" />
  <img src="https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Multi--Stage-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <a href="https://help-desk-pi-iv.vercel.app" target="_blank">
    <img src="https://img.shields.io/badge/Acessar_Aplicacao-Vercel-000?style=for-the-badge&logo=vercel&logoColor=white" />
  </a>
</p>

---

## Sumario

- [Acesso a Aplicacao em Producao](#acesso-a-aplicacao-em-producao)
- [Visao Geral do Projeto](#visao-geral-do-projeto)
  - [Objetivo e Publico-Alvo](#objetivo-e-publico-alvo)
  - [Ciclo de Vida dos Chamados](#ciclo-de-vida-dos-chamados)
  - [Diferenciais e Arquitetura em Tempo Real](#diferenciais-e-arquitetura-em-tempo-real)
- [Arquitetura do Sistema](#arquitetura-do-sistema)
  - [Diagrama de Conteineres (C4 Nivel 2)](#diagrama-de-conteineres-c4-nivel-2)
  - [Contratos de Integracao (CT18, CT19, CT22)](#contratos-de-integracao-ct18-ct19-ct22)
- [Stack Tecnologica](#stack-tecnologica)
- [Estrutura do Repositorio](#estrutura-do-repositorio)
- [Pre-requisitos](#pre-requisitos)
- [Instalacao e Configuracao Local](#instalacao-e-configuracao-local)
  - [1. Clonagem do Repositorio](#1-clonagem-do-repositorio)
  - [2. Configuracao de Variaveis de Ambiente](#2-configuracao-de-variaveis-de-ambiente)
  - [3. Execucao Manual do Backend API](#3-execucao-manual-do-backend-api)
  - [4. Execucao Manual do Backend Socket](#4-execucao-manual-do-backend-socket)
  - [5. Execucao do Frontend](#5-execucao-do-frontend)
- [Execucao via Docker](#execucao-via-docker)
- [Qualidade e Testes de Software](#qualidade-e-testes-de-software)
  - [Testes Automatizados com JUnit 5 (Backend Socket)](#testes-automatizados-com-junit-5-backend-socket)
  - [Execucao dos Testes JUnit via Linha de Comando](#execucao-dos-testes-junit-via-linha-de-comando)
  - [Analise Estatica com SonarCloud](#analise-estatica-com-sonarcloud)
- [Catalogo de Endpoints e Contratos](#catalogo-de-endpoints-e-contratos)
  - [Endpoints REST (Backend API)](#endpoints-rest-backend-api)
  - [Eventos e Envelopes WebSocket (Backend Socket)](#eventos-e-envelopes-websocket-backend-socket)
- [Modelagem do Banco de Dados](#modelagem-do-banco-de-dados)
- [Guia de Uso por Perfil](#guia-de-uso-por-perfil)
- [Decisoes Arquiteturais e Alinhamento de Escopo](#decisoes-arquiteturais-e-alinhamento-de-escopo)
- [Equipe e Informacoes Academicas](#equipe-e-informacoes-academicas)

---

## Acesso a Aplicacao em Producao

O sistema encontra-se integralmente publicado em nuvem, pronto para utilizacao direta:

- **Frontend (Interface Web SPA):** [https://help-desk-pi-iv.vercel.app](https://help-desk-pi-iv.vercel.app)
- **Backend API (REST):** `https://backend-api-zdf5.onrender.com`
- **Backend Socket (WSS):** `wss://backend-socket.onrender.com/ws`

Credenciais sugeridas para homologacao e avaliacao:
- **Tecnico / Administrador de TI:** `admin@empresa.com` | Senha: `admin` (ou `ti@empresa.com` | `123456`)
- **Solicitante de Departamento (Financeiro):** `carlos@empresa.com` | Senha: `user123`
- **Solicitante de Departamento (RH):** `ana@empresa.com` | Senha: `user123`

---

## Visao Geral do Projeto

O **HelpDesk** e uma solucao corporativa full-stack voltada para ambientes empresariais e instituicoes de ensino que demandam atendimento agil de suporte tecnico de tecnologia da informacao. 

### Objetivo e Publico-Alvo

- **Clientes / Funcionarios Solicitantes:** Abertura simplificada de chamados com categorizacao e definicao de prioridade, acompanhamento em tempo real da fila de solicitacoes restrita ao seu departamento, visualizacao do historico cronologico e recebimento de alertas imediatos.
- **Tecnicos de TI (Suporte):** Painel centralizado e fila unificada com visualizacao em Lista e Quadro Kanban dinamico, mecanismo atomico de atribuicao de atendimento (`startService`), transicao de estados e insercao de apontamentos de timeline.
- **Administradores:** Controle unificado de usuarios com RBAC (`CLIENT`, `SUPPORT`, `ADMIN`), gestao de categorias, isolamento estrito por departamento (`DepartmentPolicy`) e monitoramento das metricas de atendimento.

### Ciclo de Vida dos Chamados

O ciclo de vida operacional garante integridade rigorosa: chamados recem-abertos permanecem no estado `NEW` (`NOVO`) ate que um tecnico do departamento de TI realize a atribuicao formal e inicie o atendimento.

```
+-----------------+       Tecnico de TI Inicia Atendimento       +-----------------+
|      NOVO       | -------------------------------------------> |    ATRIBUIDO    |
| (Aguardando TI) |   (POST /api/chamados/:id/iniciar - Atomico)  | (Em Atendimento)|
+-----------------+                                              +--------+--------+
                                                                          |
                                     +------------------------------------+------------------------------------+
                                     |                                                                         |
                                     v Conclusao do Atendimento                                               v Impedimento / Recusa
                           +-----------------+                                                       +-----------------+
                           |     FECHADO     |                                                       |  NAO RESOLVIDO  |
                           |   (Resolvido)   |                                                       | (Nao Concluido) |
                           +-----------------+                                                       +-----------------+
```

### Diferenciais e Arquitetura em Tempo Real

1. **Protocolo RFC 6455 100% Nativo:** Servidor WebSocket implementado diretamente sobre `java.net.ServerSocket`, sem uso de bibliotecas de terceiros como Spring WebSocket, Netty ou Tomcat.
2. **Ciclo CRUD Totalmente Sincronizado:** Criacao (`CREATED`), atualizacao (`UPDATED`) e exclusao (`DELETED`) propagadas instantaneamente para todos os navegadores abertos.
3. **Isolamento de Seguranca por Departamento:** Broadcast seletivo no WebSocket; funcionarios de um departamento nao recebem dados confidenciais de solicitacoes de outros setores.
4. **Atribuicao Concorrente Atomica:** Previne atraves de CTEs no PostgreSQL que dois tecnicos assumam simultaneamente o mesmo chamado.
5. **Resiliencia em Nuvem:** Mecanismos de Heartbeat Keep-Alive (Ping frames a cada 20s) e reconexao inteligente no frontend com reconciliacao automatica de dados apos cold start.

---

## Arquitetura do Sistema

### Diagrama de Conteineres (C4 Nivel 2)

O sistema foi arquitetado segundo o modelo de microsserviços desacoplados e conteinerizados:

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

### Contratos de Integracao (CT18, CT19, CT22)

| Contrato | Protocolo / Canal | Origem -> Destino | Descricao Tecnica |
|:---|:---|:---|:---|
| **CT18 — `publicarEvento`** | WebSocket Persistente (Interno) | Backend API -> Backend Socket | Disparado imediatamente apos operacoes de INSERT, UPDATE ou DELETE no banco. Transporta o envelope JSON validado pelo segredo compartilhado `INTERNAL_SECRET`. |
| **CT19 — `autenticarConexao`** | WebSocket Seguro (WSS) | Frontend -> Backend Socket | Primeira mensagem enviada pelo navegador logo apos o handshake. Valida o Bearer Token do usuario e extrai o perfil (`role`) e departamento no Neon DB. |
| **CT22 — `chamadoAtualizado`** | WebSocket Seguro (WSS) | Backend Socket -> Frontend | Broadcast direcionado distribuindo a mudanca de estado do chamado para o autor, colegas do mesmo departamento e para a equipe de TI conectada. |

---

## Stack Tecnologica

| Camada | Tecnologia | Detalhes e Justificativa |
|:---|:---|:---|
| **Frontend** | HTML5, CSS3 Vanilla, JavaScript (ES6+) | Single Page Application (SPA) nativa, sem sobrecarga de frameworks, com Design System light/dark e i18n. |
| **Backend API** | Java 21 Puro (`com.sun.net.httpserver`) | Servidor HTTP nativo sem dependencias externas pesadas, JDBC puro com transacoes ACID manuais. |
| **Backend Socket** | Java 21 Puro (`java.net.ServerSocket`) | Implementacao manual da RFC 6455 (handshake, hashing SHA-1/Base64, frames com mascara e keep-alive). |
| **Banco de Dados** | PostgreSQL 15+ (Neon Serverless) | Banco relacional gerenciado na nuvem com criptografia TLS em repouso e em transito. |
| **Testes Unitarios** | JUnit 5 (`junit-platform-console-standalone`) | Suite automatizada de testes cobrindo conformidade RFC 6455, autenticacao e distribuicao de eventos. |
| **Qualidade de Codigo**| SonarCloud | Analise estatica continua integrada via GitHub Actions, com Rating 'A' em Seguranca e Confiabilidade. |
| **Serializacao** | Google Gson 2.11.0 | Conversao bidirecional entre DTOs e envelopes JSON. |
| **Conteinerizacao** | Docker | Imagens multi-stage minimalistas baseadas em Eclipse Temurin 21 e Alpine Linux. |
| **Hospedagem** | Vercel + Render + Neon | Vercel (Frontend estatico), Render (Servicos Java em conteiner) e Neon (PostgreSQL Serverless). |

---

## Estrutura do Repositorio

```
pi4-2026-2-0102-grupo2/
├── .github/
│   └── workflows/
│       └── sonarqube.yml       # Esteira de analise estatica continua no SonarCloud
├── backend-api/                # Microservico REST em Java 21 Puro
│   ├── Dockerfile              # Build multi-stage conteinerizado
│   ├── lib/
│   │   ├── postgresql.jar      # Driver JDBC PostgreSQL 42.7.3
│   │   └── gson.jar            # Google Gson 2.11.0
│   └── src/com/helpdesk/api/
│       ├── Main.java           # Ponto de entrada e registro de rotas REST
│       ├── config/             # Conexao JDBC com pool e variaveis de ambiente
│       ├── dao/                # Camada de persistencia com SQL nativo e transacoes
│       ├── handler/            # Controladores HTTP (TicketHandler, UserHandler, etc.)
│       ├── model/              # Modelos de dominio (User, Ticket, Category, etc.)
│       ├── socket/             # SocketEventPublisher (CT18 via fila thread-safe)
│       └── util/               # DepartmentPolicy, AuthUtil e validacoes
├── backend-socket/             # Microservico WebSocket RFC 6455 em Java 21 Puro
│   ├── Dockerfile              # Build multi-stage do servidor de sockets
│   ├── lib/
│   │   ├── postgresql.jar      # Conexao com Neon para validacao de credenciais
│   │   └── gson.jar            # Serializacao de envelopes
│   ├── src/com/helpdesk/socket/
│   │   ├── Main.java           # Listener ServerSocket e despacho multithread
│   │   ├── model/              # Payloads de eventos e modelos de sessao
│   │   ├── server/             # Handlers de protocolo (CT18, CT19, CT22)
│   │   └── util/               # WebSocketUtil (RFC 6455), AuthUtil e JsonUtil
│   └── test/com/helpdesk/socket/ # Suite completa de testes automatizados com JUnit 5
│       ├── model/              # TicketEventPayloadTest
│       ├── server/             # Ct18PublicarEventoTest, Ct19AutenticarConexaoTest, Ct22ChamadoAtualizadoTest
│       ├── support/            # ServidorDeTeste, ClienteWs (simulador), UserDaoFalso
│       └── util/               # WebSocketUtilTest, AuthUtilTest
├── frontend/                   # Interface Single Page Application (SPA)
│   ├── index.html              # Painel principal, Kanban, filtros e chamados
│   ├── login.html              # Tela de autenticacao
│   ├── register.html           # Tela de auto-cadastro de funcionarios
│   ├── css/                    # Folhas de estilo modulares (tokens, layout, componentes)
│   └── js/
│       ├── api.js              # Cliente HTTP centralizado com fetch e Bearer token
│       ├── app.js              # Controlador principal da interface e renderizacao
│       ├── auth.js             # Gerenciamento de login, logout e sessao
│       ├── components.js       # Renderizacao dinamica de modais, cards e tabelas
│       ├── data.js             # Estado reativo e sincronizacao com API
│       ├── i18n.js             # Internacionalizacao dinamica (PT-BR)
│       ├── router.js           # Roteamento SPA client-side
│       ├── socket.js           # Cliente WebSocket (CT19/CT22, reconexao inteligente)
│       └── theme.js            # Gerenciamento de temas Claro e Escuro
├── docs/                       # Documentacao tecnica do projeto
│   ├── adr/                    # Architecture Decision Records (ADR 0001 e ADR 0002)
│   ├── fluxos/                 # Especificacao de entregaveis (E1 a E9) e contratos
│   ├── database_modeling.md    # Esquema relacional e dicionario de dados
│   ├── documento-de-projeto.md # Documento de Projeto oficial (Capitulos 1 a 11)
│   └── escopo.pdf              # Especificacao formal original de requisitos
├── CHANGELOG.md                # Historico semantico de versoes do projeto
├── sonar-project.properties    # Configuracoes do SonarCloud Scanner
└── README.md                   # Manual de implantacao e uso (Capitulo 9)
```

---

## Pre-requisitos

Para compilar, testar e executar a solucao localmente, os seguintes softwares sao necessarios:

- **Java Development Kit (JDK):** Versao 21 LTS ou superior.
- **Git:** Para clonagem e gestao de branches.
- **Navegador Web Moderno:** Google Chrome, Mozilla Firefox, Microsoft Edge ou Safari com suporte a WebSockets (RFC 6455).
- **Docker e Docker Compose (Opcional):** Caso deseje executar os servicos conteinerizados.
- **Servidor HTTP Local (Opcional):** Node.js (`npx serve`), Python 3 (`python3 -m http.server`) ou extensao Live Server do VS Code.

---

## Instalacao e Configuracao Local

> **Validacao do Manual em Maquina Limpa:**  
> O roteiro de instalacao, compilacao manual via `javac`, execucao e testes deste manual foi integralmente testado e homologado a partir do zero em um ambiente limpo (sem dependencias em cache) por Lucas Curti Rodrigues Pinto e Pedro Henrique Vieira Lima, assegurando 100% de reprodutibilidade para os avaliadores.

### 1. Clonagem do Repositorio

```bash
git clone https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2.git
cd pi4-2026-2-0102-grupo2
```

### 2. Configuracao de Variaveis de Ambiente

Tanto o `backend-api` quanto o `backend-socket` utilizam variaveis de ambiente para conexao com o banco de dados e seguranca compartilhada.

#### Variaveis para o `backend-api`:
- `PORT`: Porta HTTP para atendimento REST (Padrao: `8000`).
- `DB_URL`: String JDBC de conexao PostgreSQL (ex: `jdbc:postgresql://ep-xyz.us-east-2.aws.neon.tech/neondb?sslmode=require`).
- `DB_USER`: Usuario do banco de dados Neon.
- `DB_PASSWORD`: Senha do banco de dados Neon.
- `JWT_SECRET`: Chave secreta HMAC para geracao e validacao dos tokens de autenticacao.
- `SOCKET_URL`: Endereco WebSocket do Backend Socket para publicacao de eventos (ex: `ws://localhost:5000/ws` ou `wss://backend-socket.onrender.com/ws`).
- `INTERNAL_SECRET`: Chave secreta de autenticacao interna entre os servicos (ex: `segredo-interno-entre-backends`).

#### Variaveis para o `backend-socket`:
- `PORT`: Porta do servidor WebSocket e Health Check (Padrao: `5000`).
- `DB_URL`: String JDBC para validacao de sessoes de usuarios no Neon.
- `DB_USER`: Usuario do banco.
- `DB_PASSWORD`: Senha do banco.
- `JWT_SECRET`: Mesma chave HMAC configurada na API.
- `INTERNAL_SECRET`: Mesma chave secreta configurada na API para validacao do CT18.

### 3. Execucao Manual do Backend API

No diretorio raiz do projeto:

```bash
cd backend-api

# Criar diretorio de saida dos binarios
mkdir -p out

# Compilar todas as classes Java com as bibliotecas Gson e PostgreSQL Driver
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")

# Exportar variaveis de ambiente essenciais (substitua com as credenciais reais do Neon)
export PORT=8000
export DB_URL="jdbc:postgresql://<SEU_HOST_NEON>/neondb?sslmode=require"
export DB_USER="<SEU_USUARIO>"
export DB_PASSWORD="<SUA_SENHA>"
export JWT_SECRET="sua-chave-secreta-de-producao"
export SOCKET_URL="ws://localhost:5000/ws"
export INTERNAL_SECRET="segredo-interno-entre-backends"

# Iniciar o servidor HTTP
java -cp "out:lib/*" com.helpdesk.api.Main
```
*O servidor iniciara e respondera requisicoes na porta `8000`.*

### 4. Execucao Manual do Backend Socket

Abra um novo terminal e execute:

```bash
cd backend-socket

# Criar diretorio de saida dos binarios
mkdir -p out

# Compilar todas as classes Java
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")

# Exportar variaveis de ambiente
export PORT=5000
export DB_URL="jdbc:postgresql://<SEU_HOST_NEON>/neondb?sslmode=require"
export DB_USER="<SEU_USUARIO>"
export DB_PASSWORD="<SUA_SENHA>"
export JWT_SECRET="sua-chave-secreta-de-producao"
export INTERNAL_SECRET="segredo-interno-entre-backends"

# Iniciar o servidor WebSocket
java -cp "out:lib/*" com.helpdesk.socket.Main
```
*O servidor de sockets iniciara na porta `5000`, aceitando conexoes WebSocket em `/ws` e Health Checks em `/health`.*

### 5. Execucao do Frontend

Como o frontend e uma aplicacao puramente estatica (SPA sem build step), voce pode abri-lo atraves de qualquer servidor HTTP de arquivos estaticos:

```bash
cd frontend

# Opcao A: Usando Python 3
python3 -m http.server 3000

# Opcao B: Usando Node.js npx serve
npx serve -l 3000

# Opcao C: Abrir diretamente no navegador o arquivo index.html
```

Acesse no navegador: `http://localhost:3000`.

*Nota de configuracao de rede:* Caso queira apontar o frontend local para os backends locais, insira no `localStorage` do navegador via console do desenvolvedor (`F12`):
```javascript
localStorage.setItem('helpdesk-api-url', 'http://localhost:8000');
localStorage.setItem('helpdesk-socket-url', 'ws://localhost:5000/ws');
```

---

## Execucao via Docker

Tanto o `backend-api` quanto o `backend-socket` possuem `Dockerfile` com compilação multi-stage embutida, gerando imagens enxutas baseadas no `eclipse-temurin:21-jre-alpine`.

### Construindo e Executando os Conteineres

```bash
# Build e execucao do Backend API
docker build -t helpdesk-backend-api ./backend-api
docker run -d --name helpdesk-api -p 8000:8000 \
  -e DB_URL="jdbc:postgresql://<HOST>/neondb?sslmode=require" \
  -e DB_USER="<USER>" \
  -e DB_PASSWORD="<PASS>" \
  -e JWT_SECRET="segredo" \
  -e SOCKET_URL="ws://socket-host:5000/ws" \
  -e INTERNAL_SECRET="segredo-interno" \
  helpdesk-backend-api

# Build e execucao do Backend Socket
docker build -t helpdesk-backend-socket ./backend-socket
docker run -d --name helpdesk-socket -p 5000:5000 \
  -e DB_URL="jdbc:postgresql://<HOST>/neondb?sslmode=require" \
  -e DB_USER="<USER>" \
  -e DB_PASSWORD="<PASS>" \
  -e JWT_SECRET="segredo" \
  -e INTERNAL_SECRET="segredo-interno" \
  helpdesk-backend-socket
```

---

## Qualidade e Testes de Software

### Testes Automatizados com JUnit 5 (Backend Socket)

Devido a alta criticidade e complexidade do `backend-socket` — que implementa manualmente o protocolo RFC 6455 em nivel de byte buffer, decodificacao de mascara de 4 bytes, calculo de hash criptografico e despacho concorrente multithread —, uma suite completa de testes com **JUnit 5** foi estruturada.

A suite cobre:
- **`WebSocketUtilTest`**: Validacao do algoritmo de handshake RFC 6455 (`Sec-WebSocket-Accept`), decodificacao de frames mascarados originados de navegadores Web, codificacao de frames de saida sem mascara e resposta HTTP 200 para probes de Health Check.
- **`Ct18PublicarEventoTest`**: Integracao inter-processos simulando chamadas da API REST para o socket, validacao de obrigatoriedade do `INTERNAL_SECRET`, consistencia de schema e rejeicao de acoes invalidas.
- **`Ct19AutenticarConexaoTest`**: Validacao de tokens JWT/HMAC, associacao de conexoes ativas ao `userId` e recusa de credenciais expiradas ou adulteradas.
- **`Ct22ChamadoAtualizadoTest`**: Validacao da logica de broadcast seletivo; testes automatizados comprovam que:
  - O solicitante dono do chamado recebe o evento.
  - Tecnicos (`SUPPORT`) e Administradores (`ADMIN`) recebem todos os chamados.
  - Colegas do mesmo departamento do autor recebem o chamado em seus paineis.
  - Usuarios de departamentos distintos sao estritamente bloqueados e nao recebem o evento.
- **`AuthUtilTest`**: Criptografia de tokens HMAC-SHA256 e parsing de claims.
- **`TicketEventPayloadTest`**: Serializacao e desserializacao de eventos de CRUD.

### Execucao dos Testes JUnit via Linha de Comando

A execucao dos testes prescinde de instalacao de Maven ou Gradle, rodando diretamente pelo console standalone do JUnit:

```bash
cd backend-socket

# 1. Baixar o runner oficial do JUnit 5 Standalone (caso nao possua)
curl -fsSL -o junit-platform-console-standalone.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar

# 2. Compilar o codigo fonte principal
mkdir -p out/main
javac -encoding UTF-8 -cp "lib/*" -d out/main $(find src -name "*.java")

# 3. Compilar a suite de testes unitarios vinculando as classes de producao e o JUnit
mkdir -p out/test
javac -encoding UTF-8 -cp "out/main:lib/*:junit-platform-console-standalone.jar" -d out/test $(find test -name "*.java")

# 4. Executar os testes automatizados
java -jar junit-platform-console-standalone.jar execute \
  --class-path "out/main:out/test:lib/gson.jar:lib/postgresql.jar" \
  --select-package com.helpdesk.socket
```

Todos os testes sao executados em memoria utilizando `ServidorDeTeste`, `ClienteWs` e `UserDaoFalso`, sem necessidade de conexao externa a internet ou ao banco durante o ciclo de testes.

### Analise Estatica com SonarCloud

O monorepo conta com pipeline de analise estatica automatizada no **SonarCloud** via GitHub Actions (`.github/workflows/sonarqube.yml`). A analise e disparada a cada commit e pull request na branch `main`.

**Metricas Oficiais do SonarCloud no Projeto:**
- **Quality Gate:** Aprovado (`Passed`).
- **Confiabilidade (Reliability Rating):** 'A' (Zero bugs criticos ou bloqueantes).
- **Seguranca (Security Rating):** 'A' (Zero vulnerabilidades).
- **Manutenibilidade (Maintainability Rating):** 'A' (Baixa densidade de code smells).
- **Escopo analisado:** Codigo Java do `backend-api`, codigo Java do `backend-socket` e scripts ES6+ do `frontend/js`.

---

## Catalogo de Endpoints e Contratos

### Endpoints REST (Backend API)

| Metodo | Rota | Autenticacao | Descricao |
|:---|:---|:---:|:---|
| `GET` | `/health` | Publico | Verificacao de saude da API e prevencao de cold start |
| `POST` | `/api/login` | Publico | Autentica o usuario e retorna o Bearer Token JWT |
| `POST` | `/api/users` | Publico | Auto-cadastro de novos funcionarios |
| `GET` | `/api/users` | `ADMIN` / `SUPPORT` | Lista todos os colaboradores cadastrados |
| `GET` | `/api/categories` | Logado | Lista as categorias tecnicas ativas para abertura |
| `GET` | `/api/chamados` | Logado | Lista chamados filtrados pelo departamento do usuario |
| `POST` | `/api/chamados` | Logado | Abertura de novo chamado (persiste e emite CT18 `CREATED`) |
| `GET` | `/api/chamados/:id` | Logado | Detalhes do chamado e timeline completa |
| `PUT` | `/api/chamados/:id` | `TI` / Admin | Atualiza dados do chamado (status, prioridade, tecnico) |
| `POST` | `/api/chamados/:id/iniciar`| `TI` (Suporte) | Atribuicao atomica de atendimento via CTE (`startService`) |
| `DELETE`| `/api/chamados/:id` | `ADMIN` | Exclusao transacional em cascata (emite CT18 `DELETED`) |
| `GET` | `/api/notifications` | Logado | Consulta notificacoes persistidas do usuario logado |

### Eventos e Envelopes WebSocket (Backend Socket)

- **`autenticarConexao` (CT19):**
  - Cliente envia: `{"type":"autenticarConexao","data":{"token":"<JWT_TOKEN>"}}`
  - Servidor responde: `{"type":"conexaoAutenticada","data":{"userId":123,"role":"SUPPORT"}}`
- **`publicarEvento` (CT18):**
  - API envia: `{"type":"publicarEvento","data":{"secret":"...","action":"UPDATED","ticketId":10,"title":"...","status":"ASSIGNED","priority":"HIGH","clientId":5,"department":"FINANCEIRO"}}`
  - Servidor responde: `{"received":true}`
- **`chamadoAtualizado` (CT22):**
  - Servidor entrega ao Frontend: `{"type":"chamadoAtualizado","data":{"action":"UPDATED","ticketId":10,"title":"...","status":"ASSIGNED","priority":"HIGH","supportName":"Tecnico Suporte"}}`

---

## Modelagem do Banco de Dados

O banco de dados relacional (PostgreSQL 15+ no Neon) e composto por 5 tabelas normalizadas:

1. **`users`:** Cadastro de colaboradores, hash BCrypt de senhas, papeis (`CLIENT`, `SUPPORT`, `ADMIN`) e departamento (`TI`, `Financeiro`, `RH`, etc.).
2. **`categories`:** Categorias tecnicas para classificacao de solicitacoes (`Hardware`, `Software`, `Rede`, `Acessos`, etc.).
3. **`tickets`:** Chamados com controle rigoroso de estado (`NEW`, `ASSIGNED`, `CLOSED`, `UNRESOLVED`), prioridades (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), autor e tecnico responsavel.
4. **`ticket_timeline`:** Auditoria cronologica de eventos, mudancas de estado e mensagens de atendimento.
5. **`notifications`:** Notificacoes push persistidas para historico e reconciliacao.

---

## Guia de Uso por Perfil

### 1. Funcionario Solicitante (Cliente)
1. Efetue login com suas credenciais ou realize o auto-cadastro na tela de Registro.
2. Na tela inicial, clique em **"Novo Chamado"**.
3. Preencha o titulo descritivo, selecione a categoria, prioridade e detalhe o problema.
4. Ao clicar em **"Salvar"**, o chamado sera enviado a API e instantaneamente transmitido via WebSocket para a equipe de TI.
5. Acompanhe a evolucao do chamado em tempo real na aba **"Meus Chamados"** ou **"Meu Departamento"**.

### 2. Tecnico de Suporte de TI
1. Faca login com um usuario vinculado ao departamento de TI (`role: SUPPORT` ou `ADMIN`).
2. Acesse a visao de **"Fila de Chamados"** ou o quadro **"Kanban"**.
3. Chamados recem-abertos aparecem na coluna **"Aguardando TI"**.
4. Clique no botao **"Iniciar Atendimento"** no card do chamado. O sistema realizara a atribuicao atomica, transferindo o chamado para **"Em Atendimento"** e notificando o solicitante em tempo real.
5. Ao concluir os procedimentos tecnicos, mude o estado para **"Resolvido"** ou **"Nao Resolvido"**, registrando o parecer final na timeline.

### 3. Administrador
1. Faca login com perfil `ADMIN`.
2. Acesse a aba **"Usuarios"** para gerenciar colaboradores, ativar/desativar contas e atribuir novos administradores.
3. Acesse o **"Dashboard"** com indicadores reativos em tempo real de chamados abertos, tempo medio de atendimento e distribuicao por categoria.

---

## Decisoes Arquiteturais e Alinhamento de Escopo

Durante o ciclo de desenvolvimento do Projeto Integrador IV, decisoes deliberadas de engenharia de software foram tomadas para priorizar estabilidade, robustez e entrega com excelencia:

- **Foco Absoluto no Fluxo de Chamados e Notificacoes:** Toda a esteira de atendimento de ponta a ponta (abertura, triagem, atribuicao atomica, ciclo de vida e encerramento) esta 100% operacional em tempo real.
- **Escopo Futuro (Inventario e Ativos de TI):** As funcionalidades de gestao patrimonial de hardware (inventario de infraestrutura de TI, cadastro de ativos de rede, serial numbers e vinculacao de computadores especificos a chamados) foram planejadas como evolucao futura do produto, evitando inflar o escopo do projeto antes da consolidacao do core de comunicacao em tempo real.
- **Descarte de Chat em Tempo Real (ADR 0001):** O modelo de chat em estilo mensageiro com salas volateis em memoria foi descartado em prol do fluxo auditavel de timeline, eliminando riscos de perda de dados durante reinicializacoes de conteineres na nuvem.
- **Push Ping Orientado a Eventos (ADR 0002):** Eliminou-se a necessidade de polling HTTP a cada poucos segundos, reduzindo custos de infraestrutura e latencia.

---

## Equipe e Informacoes Academicas

Projeto desenvolvido para a disciplina de **Projeto Integrador IV (2026-2)** — Curso de Sistemas de Informacao da **Pontificia Universidade Catolica de Campinas (PUC-Campinas)**.

- **Turma:** 0102 | **Grupo:** Grupo 02

### Integrantes da Equipe
- **Caua Vasconcelos Grecco De Faria** — RA: 25006367
- **Rafael Trevisan** — RA: 25002001
- **Vinicius Fortes Heinzl** — RA: 25008058
- **Pedro Henrique Vieira Lima** — RA: 25018202
- **Kaue Rodrigues Seixas** — RA: 23011884
- **Lucas Curti Rodrigues Pinto** — RA: 25001822

*Este projeto possui finalidade estritamente academica e de demonstracao de engenharia de software full-stack.*
