package spring.osgi.example.item.integration.file.internal;

/**
 * Created by nico.
 */


import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.config.OsgiConfiguration;
import spring.osgi.config.OsgiInjector;
import spring.osgi.example.item.service.Item;
import spring.osgi.example.item.service.ItemService;

import java.io.InputStream;
import java.util.Properties;

@Services(
        requires = @RequireService(ItemService.class)
)
@Configuration
@Import(CamelConfiguration.class)
public class ItemIntegrationFileConfiguration implements OsgiConfiguration{

    @Autowired
    private ItemService itemService;


    @Bean
    public RouteBuilder route() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("file://tmp/test/in?delete=true").routeId("fromDirectory").
                    log(LoggingLevel.TRACE, "new file found").
                    process(parseFileToItem()).
                    log(LoggingLevel.TRACE, "file parsed, ask to save item ").
                    process(saveItem()).
                    log(LoggingLevel.TRACE, "finish");
                }
        };
    }

    @Bean
    public Processor parseFileToItem(){
        return new Processor(){
            @Override
            public void process(Exchange exchange) throws Exception {

                Properties properties;
                try (InputStream file = exchange.getIn().getBody(InputStream.class)) {
                    properties = new Properties();
                    properties.load(file);
                }

                String title = properties.getProperty("title");
                String text = properties.getProperty("text");
                if (title == null || "".equals(title)) {
                    throw new ValidationException(exchange, "title not found in the message");
                }
                Item order = new Item(title, text);
                Message out = exchange.getOut();
                out.setBody(order, Item.class);
            }
        };
    }

    @Bean
    public Processor saveItem() {
        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Item item = exchange.getIn().getBody(Item.class);
                itemService.create(item);
            }
        };
    }

    @Override
    public void setupConfiguration(OsgiInjector injector) {
        injector.registerService("parseFileToItem", parseFileToItem(), Processor.class);
        injector.registerService("saveItem", saveItem(), Processor.class);
    }
}