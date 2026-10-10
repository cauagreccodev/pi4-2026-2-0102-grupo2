# ADR 0002: Usar Java puro com JDBC e PostgreSQL na persistência

**Status:** aceito

**Contexto:** O projeto precisa guardar usuários, categorias, chamados, histórico do atendimento e notificações, com relacionamentos entre eles (um chamado pertence a um funcionário, pode ter um técnico e tem vários eventos no histórico). O relatório e o dashboard administrativo (E9 e E10) dependem de consultas agregadas por estado, gravidade e datas. A disciplina pede Java puro, sem frameworks como Spring Boot ou Hibernate, e o grupo tem cerca de 30 horas por semana no total, sem tempo para aprender um framework novo.

**Decisão:** Usar PostgreSQL como banco relacional e acessá-lo diretamente com JDBC nativo (`PreparedStatement` e SQL escrito à mão), organizado em classes DAO por tabela e com um pool de conexões simples implementado no próprio projeto.

**Alternativas consideradas:**
- Spring Boot com JPA/Hibernate: descartado porque contraria o requisito de Java puro e esconderia justamente o acesso a dados que a disciplina quer que o grupo pratique.
- Banco NoSQL (MongoDB): descartado porque os dados são fortemente relacionais (chamado, usuário, técnico, histórico) e os relatórios por estado e gravidade são mais simples com SQL e chaves estrangeiras.
- MySQL: viável, mas descartado porque o PostgreSQL tem hospedagem gratuita usada pelo grupo (Neon) e tipos como `TIMESTAMPTZ`, úteis para registrar as datas do atendimento.

**Consequências:**
- Positivas: o grupo controla exatamente cada query executada, o que facilita depurar e otimizar o relatório e o dashboard; integridade garantida pelo banco com chaves estrangeiras e restrições; só duas bibliotecas externas (driver PostgreSQL e Gson), sem dependência de framework.
- Negativas: muito código repetitivo nos DAOs (mapear cada `ResultSet` para objeto, abrir e fechar recursos); mudanças no esquema exigem alterar o SQL e as classes manualmente, sem migrações automáticas; o pool de conexões caseiro não tem os recursos de um pool de mercado (validação de conexão caída, timeout), e erros como esquecer de devolver a conexão ao pool podem travar a API; proteção contra SQL injection depende de todo mundo usar sempre `PreparedStatement`.
