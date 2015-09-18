package spring.osgi.example.item.service.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.example.h2.server.H2ClientFactory;
import spring.osgi.example.item.service.Item;
import spring.osgi.example.item.service.ItemService;

import javax.sql.DataSource;

/**
 * Created by nico.
 */
@Services(
        requires = {
                @RequireService(H2ClientFactory.class)
        },
        provides = {
                @ProvideService(ItemService.class)
        }
)
@Configuration
@Import(JpaConfiguration.class)
@EnableJpaRepositories(basePackageClasses = Item.class)
public class ItemServiceConfiguration {

    @Autowired
    private H2ClientFactory h2ClientFactory;
    @Autowired
    private ItemRepository itemRepository;

    @Bean
    public ItemService itemService (){
        return new ItemServiceImpl(itemRepository);
    }

    @Bean
    public DataSource dataSource() {
        return h2ClientFactory.createDataSource("item");
    }
}
