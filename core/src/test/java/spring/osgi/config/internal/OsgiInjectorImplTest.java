package spring.osgi.config.internal;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import spring.osgi.utils.ServiceExporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by nico.
 */
public class OsgiInjectorImplTest {

    private ApplicationContext applicationContext;
    private ServiceExporter serviceExporter;
    private OsgiInjectorImpl osgiInjector;

    @Before
    public void initMock() {
        BundleContext bundleContext = mock(BundleContext.class);
        this.applicationContext = mock(ApplicationContext.class);
        this.serviceExporter = mock(ServiceExporter.class);
        this.osgiInjector = new OsgiInjectorImpl(bundleContext, applicationContext, serviceExporter);
    }

    @Test
    public void shouldCloseServiceOnDestroy() throws Exception {
        osgiInjector.destroy();
        verify(serviceExporter).destroy();
    }

}
