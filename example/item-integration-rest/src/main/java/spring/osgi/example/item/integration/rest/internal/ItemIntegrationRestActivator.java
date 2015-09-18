package spring.osgi.example.item.integration.rest.internal;

import org.slf4j.LoggerFactory;
import spring.osgi.bundle.SpringActivator;

/**
 * Created by nico.
 */
public class ItemIntegrationRestActivator extends SpringActivator {

    public ItemIntegrationRestActivator() {
        super(LoggerFactory.getLogger(ItemIntegrationRestConfiguration.class),
                ItemIntegrationRestConfiguration.class);
    }
}
