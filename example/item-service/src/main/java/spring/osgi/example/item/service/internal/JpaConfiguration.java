package spring.osgi.example.item.service.internal;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.context.OsgiApplicationContext;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by nico.
 */
@Configuration
@Services(
        requires = {
                @RequireService(PersistenceProvider.class),
                @RequireService(value = PlatformTransactionManager.class, id="transactionManager"),
        }
)
@EnableTransactionManagement
public class JpaConfiguration {

    @Autowired
    private OsgiApplicationContext applicationContext;
    @Autowired
    private PersistenceProvider persistenceProvider;
    @Autowired
    private DataSource dataSource;

    @Bean
    public EntityManagerFactory entityManagerFactory() {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setResourceLoader(applicationContext.createOsgiBundleResourcePatternResolver());
        factory.setJpaProperties(additionalProperties());
        factory.setPersistenceProvider(persistenceProvider);
        factory.setJtaDataSource(dataSource);

        factory.afterPropertiesSet();

        return factory.getObject();
    }


    @Bean
    public HibernateExceptionTranslator exceptionTranslation(){
        return new HibernateExceptionTranslator();
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, H2Dialect.class.getName());
        return properties;
    }
}
