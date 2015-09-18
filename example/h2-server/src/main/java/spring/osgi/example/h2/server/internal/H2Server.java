package spring.osgi.example.h2.server.internal;

import org.h2.tools.Server;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.sql.SQLException;

/**
 * Created by nico.
 */
public class H2Server implements DisposableBean, InitializingBean{


    private final String host;
    private final int port;

    private Server server;

    public H2Server(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void start () throws SQLException {
        server = Server.createTcpServer(buildArgs()).start();
    }

    public void stop (){
        if (server != null){
            server.stop();
        }
    }


    @Override
    public void destroy() throws Exception {
        stop();
    }

    private String[] buildArgs() {
        return new String[]{
                "-tcp",
                "-tcpAllowOthers",
                "-tcpPort",
                Integer.toString(port)
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Server getServer() {
        return server;
    }
}
