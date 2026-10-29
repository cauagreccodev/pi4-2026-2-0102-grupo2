package com.helpdesk.socket.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Gerenciador de conexoes JDBC para o PostgreSQL em nuvem no Neon.tech.
 * Compativel com as mesmas variaveis de ambiente da Backend API no Render:
 * DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD (ou DB_URL / DATABASE_URL).
 */
public class Database {

    private static final int MAX_POOL_SIZE = 5;
    private static final BlockingQueue<Connection> pool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
    private static volatile boolean driverLoaded = false;

    static {
        loadDriver();
    }

    private static synchronized void loadDriver() {
        if (driverLoaded) return;
        try {
            Class.forName("org.postgresql.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver PostgreSQL nao encontrado em lib/postgresql.jar: " + e.getMessage());
        }
    }

    /**
     * Monta a URL JDBC do Neon.tech a partir de DB_URL ou de DB_HOST/DB_PORT/DB_NAME,
     * garantindo sslmode=require para conexoes em nuvem.
     */
    private static String resolveJdbcUrl() {
        String explicitUrl = getEnv("DB_URL", getEnv("DATABASE_URL", null));
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            String url = explicitUrl.trim();
            if (url.startsWith("postgres://")) {
                url = "jdbc:postgresql://" + url.substring("postgres://".length());
            } else if (url.startsWith("postgresql://")) {
                url = "jdbc:" + url;
            }
            if (!url.contains("sslmode=")) {
                url += (url.contains("?") ? "&" : "?") + "sslmode=require";
            }
            return url;
        }

        String host = getEnv("DB_HOST", null);
        String port = getEnv("DB_PORT", "5432");
        String dbName = getEnv("DB_NAME", "neondb");

        if (host == null || host.isBlank()) {
            throw new IllegalStateException(
                    "Variaveis do Neon DB ausentes! Configure DB_URL ou (DB_HOST, DB_NAME, DB_USER, DB_PASSWORD).");
        }

        return "jdbc:postgresql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() + "?sslmode=require";
    }

    public static synchronized void init() throws SQLException {
        loadDriver();
        Connection testConn = createNewConnection();
        if (!pool.offer(testConn)) {
            testConn.close();
        }
        System.out.println("  [DB - Neon.tech] Conexao TLS validada com sucesso!");
    }

    private static Connection createNewConnection() throws SQLException {
        String jdbcUrl = resolveJdbcUrl();
        String user = getEnv("DB_USER", null);
        String pass = getEnv("DB_PASSWORD", getEnv("DB_PASS", null));

        Properties props = new Properties();
        if (user != null && !user.isBlank()) {
            props.setProperty("user", user.trim());
        }
        if (pass != null && !pass.isBlank()) {
            props.setProperty("password", pass.trim());
        }
        props.setProperty("connectTimeout", "10");
        props.setProperty("socketTimeout", "15");
        props.setProperty("tcpKeepAlive", "true");

        return DriverManager.getConnection(jdbcUrl, props);
    }

    /**
     * Obtem uma conexao ativa com o Neon.
     * Como o Neon Serverless hiberna conexoes inativas, valida com isValid(3)
     * e abre uma nova caso a conexao tenha sido encerrada pelo pooler do Neon.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = pool.poll();
        while (conn != null) {
            try {
                if (!conn.isClosed() && conn.isValid(3)) {
                    return conn;
                }
                conn.close();
            } catch (SQLException ignored) {
            }
            conn = pool.poll();
        }
        return createNewConnection();
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.setAutoCommit(true);
                    if (!pool.offer(conn)) {
                        conn.close();
                    }
                }
            } catch (SQLException e) {
                System.err.println("[DB] Erro ao devolver conexao ao pool: " + e.getMessage());
            }
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
