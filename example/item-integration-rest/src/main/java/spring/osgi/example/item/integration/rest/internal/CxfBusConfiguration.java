package spring.osgi.example.item.integration.rest.internal;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.bus.osgi.OSGIBusListener;
import org.apache.cxf.bus.spring.BusApplicationContextResourceResolver;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.resource.ClassLoaderResolver;
import org.apache.cxf.resource.ResourceManager;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.context.BundleContextAware;

import java.util.Arrays;
import java.util.List;

/**
 * Created by nico.
 */
@Services(
        provides = @ProvideService(Bus.class)
)
@Configuration
public class CxfBusConfiguration implements ApplicationContextAware, BundleContextAware{

    private ApplicationContext applicationContext;
    private BundleContext bundleContext;

    @Bean
    public Bus cxf () {
        Bus bus = BusFactory.newInstance().createBus();
        bus.setExtension(applicationContext.getClassLoader(), ClassLoader.class);
        bus.setExtension(applicationContext, ApplicationContext.class);

        ResourceManager rm = bus.getExtension(ResourceManager.class);
        rm.addResourceResolver(new ClassLoaderResolver(applicationContext.getClassLoader()));
        rm.addResourceResolver(new BusApplicationContextResourceResolver(applicationContext));

        BindingFactoryManager manager = bus.getExtension(BindingFactoryManager.class);
        JAXRSBindingFactory factory = new JAXRSBindingFactory();
        factory.setBus(bus);
        manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);

        BusLifeCycleManager lifeCycleManager = bus.getExtension(BusLifeCycleManager.class);
        if (null != lifeCycleManager) {
            lifeCycleManager.registerLifeCycleListener(new OSGIBusListener(bus, new Object[]{bundleContext}));
        }

        return bus;
    }

    @Bean
    public JacksonJsonProvider jacksonProvider (){
        return new JacksonJsonProvider();
    }

    @Bean
    public JAXRSServerFactory jaxrsServerFactory() {
        return new JAXRSServerFactory(cxf(), applicationContext, Arrays.asList(jacksonProvider()));
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public static class JAXRSServerFactory {

        private final Bus bus;
        private final ApplicationContext applicationContext;
        private final List<?> defaultProviders;

        public JAXRSServerFactory(Bus bus, ApplicationContext applicationContext, List<?> defaultProviders) {
            this.bus = bus;
            this.applicationContext = applicationContext;
            this.defaultProviders = defaultProviders;
        }

        public JAXRSServerFactoryBean createJAXRSServerFactoryBean (String address, Class<?> ... endpointClasses) {
            JAXRSServerFactoryBean jaxrsServerFactoryBean = new JAXRSServerFactoryBean();
            jaxrsServerFactoryBean.setBus(bus);
            jaxrsServerFactoryBean.setAddress(address);
            jaxrsServerFactoryBean.setProviders(defaultProviders);
            jaxrsServerFactoryBean.setResourceClasses(endpointClasses);

            for (Class<?> endpointClass : endpointClasses) {
                Object endpointInstance = applicationContext.getBean(endpointClass);
                jaxrsServerFactoryBean.setResourceProvider(endpointClass, new SingletonResourceProvider(endpointInstance));
            }
            return jaxrsServerFactoryBean;
        }
    }
}
