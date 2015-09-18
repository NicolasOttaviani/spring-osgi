package spring.osgi.example.item.integration.rest.internal;

import org.apache.camel.Processor;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.config.OsgiConfiguration;
import spring.osgi.config.OsgiInjector;
import spring.osgi.example.item.service.ItemService;
import spring.osgi.utils.MultipleServiceTracker;

import java.util.Map;

/**
 * Created by nico.
 */
@Services(
        requires = @RequireService(value = ItemService.class)
)
@Configuration
@Import(CxfBusConfiguration.class)
public class ItemIntegrationRestConfiguration implements OsgiConfiguration{
    private static final Logger logger = LoggerFactory.getLogger(ItemIntegrationRestConfiguration.class);

    @Autowired
    private CxfBusConfiguration.JAXRSServerFactory jaxrsServerFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean factory = jaxrsServerFactory.createJAXRSServerFactoryBean("/item-service", ItemService.class);
        return factory.create();
    }

    @Override
    public void setupConfiguration(OsgiInjector injector) {
        injector.trackServices(Processor.class, new MultipleServiceTracker.MultipleServiceListener<Processor>() {
            @Override
            public void serviceAdded(Processor service, Map<String, String> properties) {
                String id = properties.get(OsgiInjector.SERVICE_ID_PROPERTY_KEY);
                logger.info("processor added Processor[id={}]", id);
            }

            @Override
            public void serviceRemoved(Processor service, Map<String, String> properties) {
                String id = properties.get(OsgiInjector.SERVICE_ID_PROPERTY_KEY);
                logger.info("processor removed Processor[id={}]", id);
            }
        });
    }
}