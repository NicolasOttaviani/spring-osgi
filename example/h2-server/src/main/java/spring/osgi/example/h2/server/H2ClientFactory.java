package spring.osgi.example.h2.server;

import javax.sql.DataSource;

/**
 * Created by nico.
 */
public interface H2ClientFactory {

    final String DEFAULT_USERNAME = "sa";
    final String DEFAULT_PASSWORD = "sa";

    DataSource createDataSource (String id);
    DataSource createDataSource (String id, String username, String password);

}
