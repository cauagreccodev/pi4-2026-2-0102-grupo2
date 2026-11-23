# CT19: autenticarConexao

| Linha | Valor |
|---|---|
| Fronteira | WebSocket |
| Quem chama | Frontend, logo depois de abrir a conexão (após o login) |
| Quem responde | Backend Socket |
| Atende | E4, E5 e E6 |
| Como chamar | `wss://<endereço do Backend Socket>/ws`, primeira mensagem: `{"type":"autenticarConexao","data":{...}}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| token | texto | sim | eyJ1c2VySWQiOjEyfQ.a1b2c3 |

É o mesmo token devolvido por autenticarUsuario (CT1). O Backend Socket confere a assinatura HMAC com o mesmo `JWT_SECRET` do Backend API e lê o perfil na tabela `users` (CT13).

## Saída

Mensagem `{"type":"conexaoAutenticada","data":{...}}`:

| Campo | Tipo | Obrigatório | Exemplo |
|---|---|---|---|
| userId | inteiro | sim | 12 |
| role | um de: CLIENT, SUPPORT, ADMIN | sim | CLIENT |

A partir daqui a conexão fica registrada para esse `userId`. Um usuário com várias abas tem várias conexões, e todas recebem as mensagens.

## Erros

| Quando | O que volta |
|---|---|
| Token ausente, adulterado ou vencido | Mensagem `{"type":"erro","data":{"code":"TOKEN_INVALIDO"}}` e a conexão é fechada |
| O usuário está desativado | Mensagem `{"type":"erro","data":{"code":"USUARIO_INATIVO"}}` e a conexão é fechada |
| Qualquer outra mensagem chega antes da autenticação | Mensagem `{"type":"erro","data":{"code":"NAO_AUTENTICADO"}}` e a conexão é fechada |
