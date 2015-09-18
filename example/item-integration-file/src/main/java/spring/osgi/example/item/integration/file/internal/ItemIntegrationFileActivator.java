package spring.osgi.example.item.integration.file.internal;

import org.slf4j.LoggerFactory;
import spring.osgi.bundle.SpringActivator;

/**
 * Created by nico.
 */
public class ItemIntegrationFileActivator extends SpringActivator {

    public ItemIntegrationFileActivator() {
        super(LoggerFactory.getLogger(ItemIntegrationFileActivator.class), ItemIntegrationFileConfiguration.class);
    }
}
