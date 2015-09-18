package spring.osgi.example.h2.server.internal;

import org.osgi.service.cm.ManagedService;
import org.slf4j.LoggerFactory;
import spring.osgi.bundle.SpringActivator;

/**
 * Created by nico.
 */
public class H2Activator extends SpringActivator implements ManagedService {

    public H2Activator() {
        super(LoggerFactory.getLogger(H2Activator.class),
                H2Configuration.class);
    }
}
