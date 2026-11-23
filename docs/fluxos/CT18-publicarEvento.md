# CT18: publicarEvento

| Linha | Valor |
|---|---|
| Fronteira | TCP interno (entre os dois backends) |
| Quem chama | Backend API, logo depois de gravar um chamado novo (CT4) ou uma mudança de chamado (CT7) |
| Quem responde | Backend Socket |
| Atende | E4 e E5 |
| Como chamar | Conexão TCP na porta interna 5001 do Backend Socket. Uma linha JSON por evento: `{"type":"publicarEvento","data":{...}}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| secret | texto | sim | valor da variável `INTERNAL_SECRET`, igual nos dois backends |
| action | um de: CREATED, UPDATED | sim | UPDATED |
| ticketId | inteiro | sim | 105 |
| title | texto | sim | Notebook não liga |
| status | um de: NEW, ASSIGNED, CLOSED, UNRESOLVED | sim | ASSIGNED |
| priority | um de: LOW, MEDIUM, HIGH, URGENT | sim | HIGH |
| clientId | inteiro | sim | 12 |
| supportId | inteiro | não | 7 |
| supportName | texto | não | Carlos Mendes |
| updatedAt | data e hora | sim | 2026-10-13T10:30:00 |

Ao receber, o Backend Socket repassa o evento como chamadoAtualizado (CT22).

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| received | booleano | sim | true |

## Erros

| Quando | O que volta |
|---|---|
| `secret` errado ou ausente | O Backend Socket fecha a conexão sem responder. O Backend API registra a falha no log. |
| O Backend Socket está fora do ar | O Backend API registra a falha e não tenta de novo. A operação HTTP já foi gravada e respondida; os painéis veem a mudança na próxima vez que chamarem listarChamados (CT5). |
| JSON inválido ou campo obrigatório faltando | `{"received": false}` e o evento é descartado |
