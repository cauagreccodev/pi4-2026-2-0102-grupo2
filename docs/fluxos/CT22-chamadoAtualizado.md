# CT22: chamadoAtualizado

| Linha | Valor |
|---|---|
| Fronteira | WebSocket |
| Quem chama | Backend Socket, sempre que recebe publicarEvento (CT18) |
| Quem responde | Frontend, nas conexões do solicitante, do técnico do chamado e de todos os SUPPORT e ADMIN conectados. Não há resposta. |
| Atende | E4 e E5 |
| Como chamar | Mensagem `{"type":"chamadoAtualizado","data":{...}}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| action | um de: CREATED, UPDATED | sim | UPDATED |
| ticketId | inteiro | sim | 105 |
| title | texto | sim | Notebook não liga |
| status | um de: NEW, ASSIGNED, CLOSED, UNRESOLVED | sim | ASSIGNED |
| priority | um de: LOW, MEDIUM, HIGH, URGENT | sim | HIGH |
| clientId | inteiro | sim | 12 |
| supportId | inteiro | não | 7 |
| supportName | texto | não | Carlos Mendes |
| updatedAt | data e hora | sim | 2026-10-13T10:30:00 |

Com `action` = CREATED, o painel dos técnicos (E4) mostra o chamado novo na coluna NEW. Com UPDATED, o painel move o chamado para a coluna do novo `status` (E5).

## Saída

Não tem.

## Erros

| Quando | O que volta |
|---|---|
| O painel estava fechado ou desconectado na hora do envio | Nada. Ao abrir ou reconectar, o painel busca a lista completa por listarChamados (CT5). |
