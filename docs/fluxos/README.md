# Contratos do Backend Socket

Os fluxos e a visão geral ficam no repositório do frontend: https://github.com/Vinicius-Heinzl/pi4-0102-g2-frontend/tree/main/docs/fluxos

Os contratos CT1 a CT17 (operações HTTP e tabelas do banco) ficam no repositório do backend-api.

## Regras que valem para todas as fichas WebSocket

- Toda mensagem, nos dois sentidos, tem o formato `{"type": "<nome da operação ou mensagem>", "data": { ...campos da ficha... }}`.
- A primeira mensagem de toda conexão é autenticarConexao (CT19).
- Erro volta como `{"type": "erro", "data": {"code": "<CÓDIGO>"}}`.
- O navegador só abre conexões WebSocket, não TCP puro. Por isso a porta pública do Backend Socket fala o protocolo WebSocket (handshake HTTP + frames), implementado em Java puro sobre `ServerSocket`.

## Índice

| ID | Nome | Fronteira | Quem chama → quem responde | Atende |
|---|---|---|---|---|
| CT18 | [publicarEvento](CT18-publicarEvento.md) | TCP interno | Backend API → Backend Socket | E4, E5 |
| CT19 | [autenticarConexao](CT19-autenticarConexao.md) | WebSocket | Frontend → Backend Socket | E4, E5 |
| CT22 | [chamadoAtualizado](CT22-chamadoAtualizado.md) | WebSocket | Backend Socket → Frontend | E4, E5 |

> **Nota sobre o escopo:** Funcionalidades de chat em tempo real (como Teams/Slack) e seus respectivos contratos foram descartados do projeto para simplificação do escopo. O Backend Socket é dedicado à entrega em tempo real de eventos de chamados (`chamadoAtualizado`) para os painéis dos técnicos e do solicitante.

O Backend Socket também lê dados no banco usando a ficha CT13 (users) para autenticação de sessões (CT19), e suporta persistência de eventos conforme a modelagem do backend-api.
