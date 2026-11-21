package com.helpdesk.socket.support;

import com.helpdesk.socket.dao.UserDao;
import com.helpdesk.socket.model.UserSessionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * UserDao de mentira para os testes. Em vez de ir no banco Neon,
 * ele devolve os usuarios que o teste cadastrou na memoria.
 */
public class UserDaoFalso extends UserDao {

    private final Map<Integer, UserSessionInfo> usuarios = new HashMap<>();

    public void adicionar(int id, String perfil, boolean ativo) {
        usuarios.put(id, new UserSessionInfo(id, "Usuario " + id, "user" + id + "@teste.com", perfil, ativo));
    }

    @Override
    public UserSessionInfo findSessionInfoById(int userId) {
        return usuarios.get(userId);
    }
}
