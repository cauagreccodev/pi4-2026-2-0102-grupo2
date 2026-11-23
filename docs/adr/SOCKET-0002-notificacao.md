# 2. Notificações em tempo real

## Status

Aceito

## Contexto

Clientes e técnicos precisam ser alertados imediatamente sobre interações importantes no sistema (novas mensagens no chat, chamados atribuídos e mudanças de status).

Atualmente, o frontend exibe um sino de notificações dependente de chamadas HTTP REST. Para oferecer uma experiência ágil semelhante a aplicativos de mensagens (como WhatsApp), o servidor de socket deve notificar o navegador instantaneamente sem a necessidade de polling contínuo.

## Decisão

Implementaremos notificações push orientadas a eventos via socket gerenciado pelo backend, alinhadas às seguintes diretrizes:

- **Disparo de Evento Leve (Push Ping)**: o servidor de socket envia um evento JSON direcionado às sessões ativas do usuário destinatário (`userId`), contendo tipo do evento, resumo da mensagem e contagem atualizada de não lidas.
- **Contrato com o Frontend**:
  - Payload padronizado para integração: `{"event": "NOTIFICATION_PING", "data": {"id": 123, "ticketId": 45, "title": "Nova mensagem", "unreadCount": 3, "timestamp": 1728512400}}`.
- **Feedback Visual no Navegador**: ao receber o evento, o frontend incrementa em tempo real o badge numérico no sino do header e atualiza o indicador na aba da aplicação.
- **Feedback Sonoro**: o frontend executará um áudio curto e genérico (*ping sound*) ao receber a notificação, chamando a atenção do usuário mesmo com a aba em segundo plano.
- **Separação de Responsabilidades**: o socket atua como gatilho de entrega instantânea; a persistência definitiva e a listagem histórica permanecem na tabela `notifications` do PostgreSQL via API REST, sendo consultadas na carga inicial e reconciliadas em reconexões.

## Consequências

- Notificações instantâneas com tráfego leve de rede e eliminação de polling HTTP repetitivo.
- Contrato claro e desacoplado para integração entre o time de socket e o time de frontend.
- Dependência de interação prévia: políticas dos navegadores (*autoplay policy*) exigem interação do usuário antes de permitir reprodução sonora automática.
- Tolerância a falhas: se o socket oscilar, nenhuma notificação é perdida, pois o estado persistido é sincronizado via API REST na reconexão.
