# Fluxo do E7: Gerenciamento de usuários

**Feito quando (da Entrega 1):** O administrador visualiza os usuários cadastrados e consegue cadastrar, editar e controlar os usuários e seus respectivos perfis.

> Era o E8 na Entrega 1.

## Passos

1. O administrador abre a página de usuários.
2. O Frontend chama o Backend API com a operação `listUsers` (CT13).
3. O Backend API confere o perfil Administrador e busca os usuários na tabela `users` (CT2).
4. O Backend API responde ao Frontend e a lista de usuários aparece com o perfil e a situação (ativo ou inativo) de cada um.
5. O administrador cadastra um usuário novo ou edita um existente, definindo nome, e-mail, perfil e situação, e clica em "Salvar".
6. O Frontend chama o Backend API com a operação `saveUser` (CT14).
7. O Backend API confere os dados (e-mail único, perfil válido) e grava o usuário na tabela `users` (CT2).
8. O Backend API responde ao Frontend e a lista mostra o usuário atualizado.

## Diagrama

```mermaid
sequenceDiagram
    actor A as Administrador
    participant FE as Frontend
    participant API as Backend API
    participant DB as Banco
    A->>FE: abre a página de usuários
    FE->>API: CT13 listUsers
    API->>API: confere o perfil Administrador
    API->>DB: CT2 users
    DB-->>API: usuários cadastrados
    API-->>FE: resposta do CT13
    FE-->>A: mostra a lista de usuários
    A->>FE: cadastra ou edita um usuário e seu perfil
    FE->>API: CT14 saveUser
    API->>API: confere e-mail e perfil
    API->>DB: CT2 users
    DB-->>API: usuário gravado
    API-->>FE: resposta do CT14
    FE-->>A: lista mostra o usuário atualizado
```

## Contratos usados

| ID | Nome | De → Para |
|:---|:---|:---|
| CT13 | `listUsers` | Frontend → Backend API |
| CT14 | `saveUser` | Frontend → Backend API |
| CT2 | `users` | Backend API → Banco |
