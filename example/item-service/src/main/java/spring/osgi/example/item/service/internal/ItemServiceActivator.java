package spring.osgi.example.item.service.internal;

import org.slf4j.LoggerFactory;
import spring.osgi.bundle.SpringActivator;

/**
 * Created by nico.
 */
public class ItemServiceActivator extends SpringActivator{
    public ItemServiceActivator() {
        super(LoggerFactory.getLogger(ItemServiceActivator.class),
              ItemServiceConfiguration.class);
    }
}
