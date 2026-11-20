package com.helpdesk.api.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool de conexões JDBC simples (sem lib externa).
 * Lê configuração das variáveis de ambiente DB_URL, DB_USER, DB_PASS.
 */
public class Database {

    private static final String DB_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:postgresql://localhost:5432/helpdesk");
    private static final String DB_USER = System.getenv().getOrDefault(
            "DB_USER", "root");
    private static final String DB_PASS = System.getenv().getOrDefault(
            "DB_PASSWORD", System.getenv().getOrDefault("DB_PASS", "rootpassword"));

    private static final int POOL_SIZE = 10;
    // Valida a conexão apenas se estiver ociosa há mais de X segundos,
    // evitando a chamada isValid() (consulta ao banco) a cada checkout do pool.
    private static final long VALIDATION_CACHE_SECONDS = 30;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final ConcurrentHashMap<Connection, Long> lastValidation = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    /**
     * Inicializa o pool criando as conexões.
     * Chamado uma vez na Main antes de iniciar o servidor.
     */
    public static synchronized void init() throws SQLException {
        if (initialized) return;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado no classpath.", e);
        }

        for (int i = 0; i < POOL_SIZE; i++) {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            pool.offer(conn);
        }
        initialized = true;
        System.out.println("  Pool de conexoes inicializado (" + POOL_SIZE + " conexoes)");
        System.out.println("  Banco: " + DB_URL);
    }

    /**
     * Obtém uma conexão do pool. Bloqueia se não houver conexão disponível.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.take();
            long now = System.currentTimeMillis();
            Long last = lastValidation.get(conn);
            boolean needsValidation = (last == null) || ((now - last) / 1000 > VALIDATION_CACHE_SECONDS);

            // Valida apenas se a conexão estiver fechada ou ociosa há mais de VALIDATION_CACHE_SECONDS
            if (conn.isClosed() || needsValidation) {
                if (!conn.isValid(2)) {
                    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                }
                lastValidation.put(conn, System.currentTimeMillis());
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrompido ao aguardar conexão do pool.", e);
        }
    }

    /**
     * Devolve uma conexão ao pool.
     * Deve ser chamado em bloco finally após usar a conexão.
     */
    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true); // Reseta estado
                    pool.offer(conn);
                } else {
                    lastValidation.remove(conn);
                }
            } catch (SQLException e) {
                System.err.println("Erro ao devolver conexão ao pool: " + e.getMessage());
            }
        }
    }
}
