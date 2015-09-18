package spring.osgi.example.h2.server.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.example.h2.server.H2ClientFactory;

/**
 * Created by nico.
 */
@Services(
        provides = @ProvideService(H2ClientFactory.class)
)
@Configuration
public class H2Configuration {

    @Bean
    public H2Server h2Server (){
        return new H2Server("localhost", 9092);
    }

    @Bean
    public H2ClientFactory h2ClientFactory (){
        return new H2ClientFactoryImpl(h2Server ());
    }

}
