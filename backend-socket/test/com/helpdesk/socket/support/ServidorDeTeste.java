package com.helpdesk.socket.support;

import com.helpdesk.socket.dao.UserDao;
import com.helpdesk.socket.server.WebSocketClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Sobe o servidor do socket numa porta livre do computador (a porta 0 faz o sistema escolher).
 * Cada conexao recebida vai para o mesmo WebSocketClientHandler que roda em producao.
 */
public class ServidorDeTeste implements AutoCloseable {

    private final ServerSocket serverSocket;

    public ServidorDeTeste(UserDao userDao) throws IOException {
        serverSocket = new ServerSocket(0);

        Thread aceitador = new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket cliente = serverSocket.accept();
                    Thread conexao = new Thread(new WebSocketClientHandler(cliente, userDao));
                    conexao.setDaemon(true);
                    conexao.start();
                }
            } catch (IOException e) {
                // o servidor foi fechado no fim do teste
            }
        });
        aceitador.setDaemon(true);
        aceitador.start();
    }

    public int getPorta() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            // nada a fazer
        }
    }
}
