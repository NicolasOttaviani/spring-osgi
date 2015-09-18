package spring.osgi.config.internal;

import org.junit.Before;
import org.junit.Test;
import spring.osgi.config.OsgiConfiguration;
import spring.osgi.config.OsgiInjector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by nico.
 */
public class SpringOsgiConfigurationProcessorTest {

    private OsgiInjector osgiInjector;
    private SpringOsgiConfigurationProcessor processor;

    @Before
    public void initMock() {
        this.osgiInjector = mock(OsgiInjector.class);
        this.processor = new SpringOsgiConfigurationProcessor(this.osgiInjector);
    }

    @Test
    public void shouldNotTransformBeanWhenPre() {
        OsgiConfiguration osgiConfiguration = mock(OsgiConfiguration.class);
        Object postO = processor.postProcessBeforeInitialization(osgiConfiguration, "beanName");
        assertEquals(osgiConfiguration, postO);
    }

    @Test
    public void shouldCallSetupConfigurationOnOsgiConfiguration() throws Exception {
        OsgiConfiguration osgiConfiguration = mock(OsgiConfiguration.class);
        processor.postProcessAfterInitialization(osgiConfiguration, "beanName");

        verify(osgiConfiguration).setupConfiguration(osgiInjector);

    }


}
