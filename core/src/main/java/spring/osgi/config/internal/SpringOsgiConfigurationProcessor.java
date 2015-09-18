package spring.osgi.config.internal;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import spring.osgi.config.OsgiConfiguration;
import spring.osgi.config.OsgiInjector;

/**
 * Created by nico.
 */
public class SpringOsgiConfigurationProcessor implements BeanPostProcessor {
    private final OsgiInjector osgiInjector;

    public SpringOsgiConfigurationProcessor(OsgiInjector osgiInjector) {
        this.osgiInjector = osgiInjector;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof OsgiConfiguration) {
            try {
                ((OsgiConfiguration) bean).setupConfiguration(this.osgiInjector);
            } catch (Exception e) {
                throw new BeanInitializationException("Error during the osgi configuration setup", e);
            }
        }
        return bean;
    }
}
