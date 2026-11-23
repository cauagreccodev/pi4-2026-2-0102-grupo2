# Changelog

Todas as alteracoes notaveis neste projeto serao documentadas neste arquivo.

O formato e baseado em [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-11-23

### Adicionado
- **Backend-Socket (v1.0.2)**:
  - Servidor WebSocket em conformidade estrita com a norma RFC 6455 desenvolvido diretamente sobre `java.net.ServerSocket` em Java 21 Puro sem frameworks externos.
  - Mecanismo de Heartbeat Keep-Alive com emissao periodica (20s) de frames de Ping (`opcode 0x9`) para evitar encerramentos indevidos de conexao ociosa por proxies reversos em nuvem (Render, Cloudflare).
  - Suite de testes automatizados com JUnit 5 (`junit-platform-console-standalone-1.10.2.jar`) cobrindo conformidade RFC 6455, autenticacao e distribuicao setorial de eventos.
  - Integracao completa do ciclo CRUD em tempo real via envelopes de eventos com as acoes `CREATED`, `UPDATED` e `DELETED`.
- **Qualidade e CI/CD**:
  - Pipeline de analise estatica continua com **SonarCloud** via GitHub Actions (`.github/workflows/sonarqube.yml`).
  - Obtencao do selo oficial **Rating 'A'** em Seguranca, Confiabilidade e Manutenibilidade, com Quality Gate aprovado.
- **Documentacao**:
  - Documento de Projeto oficial unificado (`docs/documento-de-projeto.md`) cobrindo os capitulos 1 a 11 com especificacao completa de arquitetura, banco, requisitos e testes.
  - Artefatos para submissao academica em HTML e PDF (`PROJETO-0102-grupo02.pdf`).

### Modificado
- **Backend-API**:
  - Refatoracao do componente `SocketEventPublisher` para manter uma unica conexao WebSocket interna persistente e reutilizavel com o Backend Socket, eliminando handshakes redundantes sob demanda.
  - Desacoplamento do envio de eventos atraves de fila em memoria thread-safe (`LinkedBlockingQueue`) consumida por worker daemon assincrono.
  - Otimizacao da exclusao de chamados (`TicketDao.deleteById`) utilizando Common Table Expression (CTE) atomica no PostgreSQL, removendo timeline, notificacoes e chamado em uma unica transacao.
- **Backend-Socket**:
  - Refatoracao profunda no tratamento de excecoes e fechamento seguro de sockets e streams, elevando a classificacao do SonarCloud de 'D' para 'A' (release v1.0.2).

### Corrigido
- **Card #1445 no OpenProject**: Correcao de falha na autenticacao do contrato CT19 (`autenticarConexao`) decorrente da latencia de despertar (*cold start* de ate 288ms) do banco serverless Neon DB, implementando fallback resiliente de sessao em memoria.
- **Card #1437 no OpenProject**: Correcao de quedas de conexao por inatividade em proxies de nuvem e garantia de broadcast setorial seletivo por departamento.
- Correcao no manipulador de cadastro de usuarios da API (`UserHandler`): atribuicao automatica do perfil `ADMIN` para colaboradores cadastrados no departamento de `TI`, solucionando a anomalia onde eventos eram encaminhados para 0 sessoes ativas.
- Correcao de propagacao da variavel de ambiente `SOCKET_URL` no servico da `backend-api` no Render.
- Sincronizacao em tempo real da exclusao de chamados (`DELETED`): eliminacao do problema de cards "fantasmas" no frontend atraves do envio imediato do evento CT22 para remocao dinamica do DOM.

### Seguranca
- Autenticacao inter-processos blindada entre Backend API e Backend Socket utilizando segredo compartilhado (`INTERNAL_SECRET`).
- Aplicacao estrita de isolamento de dados via `DepartmentPolicy`: solicitacoes de setores restritos (como Financeiro ou RH) sao transmitidas exclusivamente para o autor, colegas do mesmo setor e para a equipe de TI.
- Hashing de senhas corporativas utilizando algoritmo BCrypt e assinatura digital de sessoes via HMAC-SHA256.

---

## [0.5.0] - 2026-11-18

### Adicionado
- Implementacao de `DepartmentPolicy`: isolamento estrito de visibilidade de chamados e notificacoes de acordo com o setor funcional do usuario.
- Atribuicao atomica de atendimento (`POST /api/chamados/:id/iniciar`): operacao CTE no PostgreSQL (`TicketDao.startService`) que previne condicoes de corrida caso dois tecnicos tentem assumir o mesmo chamado simultaneamente.
- Cliente WebSocket nativo no frontend (`socket.js`) com maquina de estados e reconexao automatica com backoff exponencial.

### Modificado
- Corrigida a maquina de estados de chamados: solicitacoes recem-criadas permanecem compulsoriamente em status `NEW` (Aguardando TI) ate o inicio formal do atendimento.
- Aprimoramento do tratamento de erros SQL na camada de persistencia JDBC da API.

---

## [0.4.3] - 2026-11-14

### Adicionado
- Endpoints publicos de Health Check (`/health`, `/api/health`, `/`) respondendo `200 OK` na API e no Socket para verificacao de infraestrutura e prevencao de suspensao (*cold start*) no plano gratuito do Render.

---

## [0.4.2] - 2026-11-10

### Adicionado
- Integracao do documento de especificacao formal de escopo em `docs/escopo.pdf`.
- Registro cadastral dos integrantes da equipe e seus respectivos Registros Academicos (RA).

---

## [0.4.1] - 2026-11-05

### Adicionado
- Modulo centralizado de chamadas HTTP REST (`api.js`) com injecao automatica de Bearer Token e tratamento unificado de erros.
- Modulo de internacionalizacao client-side (`i18n.js`) com suporte nativo a PT-BR.
- Exclusao transacional de chamados com remocao em cascata na base relacional.

### Modificado
- Reestruturacao completa de `data.js` para sincronizacao reativa direta com o PostgreSQL Neon, eliminando dados estaticos de demonstracao.

---

## [0.4.0] - 2026-10-25

### Adicionado
- Estrutura completa de Data Access Objects (DAOs) em JDBC Puro sem frameworks ORM.
- Controladores HTTP baseados no servidor nativo `com.sun.net.httpserver.HttpServer`.
- Suporte a Cross-Origin Resource Sharing (CORS) e padronizacao das rotas sob `/api`.
- Controle de acesso baseado em papeis (RBAC: `CLIENT`, `SUPPORT`, `ADMIN`).

---

## [0.3.0] - 2026-10-12

### Adicionado
- Criacao do microservico `backend-socket` focado em comunicacao concorrente via sockets TCP nativos.
- Algoritmo de Handshake RFC 6455 com calculo da chave `Sec-WebSocket-Accept` via SHA-1/Base64.
- Estruturas de dados concorrentes `ConcurrentHashMap` e `LinkedBlockingQueue` para mapeamento thread-safe de sessoes.

---

## [0.2.0] - 2026-09-28

### Adicionado
- Prototipo inicial da interface Single Page Application (SPA) em HTML5, CSS3 Vanilla e JavaScript ES6+.
- Design System modular com suporte a temas Claro e Escuro.
- Modelagem conceitual do banco de dados relacional em 5 tabelas normalizadas.

---

## [0.1.0] - 2026-09-10

### Adicionado
- Concepcao inicial da arquitetura do HelpDesk para a disciplina Projeto Integrador IV.
- Mapeamento das personas (Cliente Solicitante, Tecnico de Suporte de TI, Administrador).
- Especificacao dos 9 entregaveis fundamentais (E1 a E9) e dos contratos de integracao.

---

[1.0.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/releases/tag/v1.0
[0.5.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.4.3...v0.5.0
[0.4.3]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.4.2...v0.4.3
[0.4.2]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/cauagreccodev/pi4-2026-2-0102-grupo2/releases/tag/v0.1.0
