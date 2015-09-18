package spring.osgi.example.item.integration.file.internal;

import org.apache.camel.CamelContext;
import org.apache.camel.osgi.OsgiSpringCamelContext;
import org.osgi.framework.BundleContext;
import org.springframework.context.annotation.Configuration;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.Services;
import spring.osgi.context.BundleContextAware;
import spring.osgi.utils.OsgiStringUtils;

/**
 * Created by nico.
 */
@Services(
        provides = @ProvideService(CamelContext.class)
)
@Configuration
public class CamelConfiguration extends org.apache.camel.spring.javaconfig.CamelConfiguration implements BundleContextAware{

    private BundleContext bundleContext;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        OsgiSpringCamelContext camelContext = new OsgiSpringCamelContext(getApplicationContext(), bundleContext);
        camelContext.setName(OsgiStringUtils.nullSafeSymbolicName(bundleContext.getBundle()));
        return camelContext;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
