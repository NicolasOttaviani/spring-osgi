package spring.osgi.context.internal;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import spring.osgi.config.OsgiConfiguration;
import spring.osgi.context.BundleContextAware;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by nico.
 */
public class BundleContextAwareProcessorTest {

    private BundleContext bundleContext;
    private BundleContextAwareProcessor processor;

    @Before
    public void initMock() {
        this.bundleContext = mock(BundleContext.class);
        this.processor = new BundleContextAwareProcessor(this.bundleContext);
    }

    @Test
    public void shouldNotTransformBeanWhenPre() {
        OsgiConfiguration osgiConfiguration = mock(OsgiConfiguration.class);
        Object postO = processor.postProcessAfterInitialization(osgiConfiguration, "beanName");
        assertEquals(osgiConfiguration, postO);
    }

    @Test
    public void shouldCallSetupConfigurationOnOsgiConfiguration() throws Exception {
        BundleContextAware bundleContextAware = mock(BundleContextAware.class);
        processor.postProcessBeforeInitialization(bundleContextAware, "beanName");

        verify(bundleContextAware).setBundleContext(bundleContext);

    }


}
