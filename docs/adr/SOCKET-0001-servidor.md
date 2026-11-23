# 1. Estruturas de dados do servidor

## Status

Aceito (com escopo revisado: chat em tempo real descartado)

## Contexto

O servidor de socket do Help Desk gerencia comunicação em tempo real (notificações push e atualização de eventos dos chamados) sob concorrência. O sistema precisa:

- Mapear sessões ativas por usuário (suportando múltiplas abas) para entrega direta de eventos.
- Propagar atualizações de status de chamados aos painéis dos técnicos e solicitantes em tempo real.
- Enviar mensagens sem que clientes lentos ou oscilações de rede travem os demais.
- Validar tokens e permissões de sessão no PostgreSQL.

Todas as conexões rodam concorrentemente e demandam estruturas *thread-safe* na memória, sem locks globais que criem gargalos.

## Decisão

Usaremos coleções e filas concorrentes da biblioteca padrão do Java (`java.util.concurrent`):

- **`ConcurrentHashMap` com conjuntos concorrentes**: armazena as conexões de cada usuário (`userId` → conexões ativas), permitindo múltiplas abas e broadcast em O(1).
- **`LinkedBlockingQueue` (com limite por cliente)**: buffer de saída (*outbox*) exclusivo de cada conexão. Nenhuma thread escreve direto no socket de outra; clientes que acumularem mensagens acima do limite são desconectados.
- **`LinkedBlockingQueue` (buffer de persistência/eventos)**: fila assíncrona consumida por workers dedicados para operações no PostgreSQL, desacoplando o I/O do banco do loop de rede.

### Decisão pensada e descartada: Salas de Chat e Buffer de Mensagens

Durante a modelagem inicial, cogitou-se estruturar salas de atendimento por chamado (`ticketId` → conexões), fila de atendimento prioritário (`PriorityBlockingQueue`), controle de SLA por `DelayQueue` e buffer de mensagens em memória (`ConcurrentLinkedDeque`) para viabilizar um chat em tempo real (estilo Teams/WhatsApp).

Essa abordagem foi **descartada** do escopo do projeto para simplificar a entrega e focar o servidor na entrega confiável de eventos de chamados (`chamadoAtualizado` no Kanban) e notificações. O histórico de atendimentos permanece persistido e auditado na tabela `ticket_timeline` via Backend API.

O acesso a essas estruturas fica centralizado em uma classe única de estado do servidor.

## Consequências

- Concorrência de alto desempenho usando apenas o Java nativo, sem dependências externas.
- Escopo focado e enxuto: o servidor atua na orquestração de sessões WebSocket e distribuição de eventos de chamados.
- O fluxo de eventos fica totalmente isolado de clientes lentos e da latência do banco de dados.
- Limpeza em O(1) e capacidade limitada nas filas evitam vazamentos e estouro de memória.
- Estado primário volátil em memória; reinicializações demandam recarga a partir do PostgreSQL.
- Válido para arquitetura de nó único; clustering exigirá broker externo (nova ADR).

