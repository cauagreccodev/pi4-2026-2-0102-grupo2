# ADR 0001: Separar o backend em API HTTP e servidor de socket

**Status:** aceito

**Contexto:** O Help Desk tem dois tipos de interação com perfis de carga diferentes. Abrir, listar e alterar chamados, autenticar e gerenciar usuários são operações curtas de pedido e resposta. Já a atualização em tempo real (mudança de estado, atribuição, notificações e mensagens do atendimento, entregáveis E5 e E6) exige conexões abertas por muito tempo e envio do servidor para o cliente sem que ele peça. Colocar as duas coisas no mesmo processo mistura conexões longas com requisições curtas no mesmo servidor, e o JDK puro não oferece um servidor único que faça bem as duas.

**Decisão:** Dividir o backend em dois serviços Java independentes, cada um no seu contêiner Docker: `backend-api` (HTTP/REST, porta 8000) para as operações de CRUD e autenticação, e `backend-socket` (porta 5000) para os eventos em tempo real. Os dois acessam o mesmo banco PostgreSQL.

**Alternativas consideradas:**
- Um único backend fazendo HTTP e tempo real: descartado porque o `HttpServer` do JDK não mantém conexões persistentes com push; teríamos que juntar dois servidores no mesmo processo, e uma falha ou travamento em um derrubaria o outro.
- Polling (o frontend consulta a API a cada poucos segundos): descartado porque não atende ao "tempo real" do OE3, gera muitas requisições vazias e aumenta a carga no banco.
- Serviço pronto de mensageria/tempo real (Firebase, Pusher etc.): descartado porque foge do requisito de Java puro da disciplina e cria dependência de um serviço externo.

**Consequências:**
- Positivas: cada serviço pode ser desenvolvido, testado e reiniciado separadamente (dá para dividir o grupo entre eles); uma queda do servidor de socket não impede abrir e consultar chamados pela API; o código de cada serviço fica menor e com uma responsabilidade só.
- Negativas: são dois deploys, dois Dockerfiles e duas configurações de ambiente para manter; os dois serviços escrevem no mesmo banco, então regras de negócio (por exemplo, quem pode mudar o estado de um chamado) podem acabar duplicadas; quando a API altera um chamado, o servidor de socket precisa ser avisado para fazer o broadcast, o que é um ponto de integração extra que ainda precisa ser implementado e testado; o frontend passa a falar com dois endereços diferentes.
