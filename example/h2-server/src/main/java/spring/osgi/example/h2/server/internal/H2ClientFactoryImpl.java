package spring.osgi.example.h2.server.internal;

import org.h2.jdbcx.JdbcConnectionPool;
import spring.osgi.example.h2.server.H2ClientFactory;

import javax.sql.DataSource;

/**
 * Created by nico.
 */
public class H2ClientFactoryImpl implements H2ClientFactory {

    private final String host;
    private final int port;


    public H2ClientFactoryImpl(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public H2ClientFactoryImpl(H2Server h2Server) {
        this (h2Server.getHost(), h2Server.getPort());
    }

    @Override
    public DataSource createDataSource(String id) {
        return createDataSource(id, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    @Override
    public DataSource createDataSource(String id, String username, String password) {
        return JdbcConnectionPool.create(
                createConnectionUrl(this.host, this.port, id),
                username,
                password);
    }

    private static String createConnectionUrl (String connectionHost, int connectionPort, String id){
        return String.format("jdbc:h2:tcp://%s:%d/~/%s;DB_CLOSE_ON_EXIT=FALSE;MVCC=TRUE", connectionHost, connectionPort, id);
    }
}
