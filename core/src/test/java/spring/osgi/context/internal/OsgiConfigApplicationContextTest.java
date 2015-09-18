package spring.osgi.context.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import spring.osgi.utils.SingleServiceTracker;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nico.
 */
public class OsgiConfigApplicationContextTest {

    private OsgiConfigApplicationContext applicationContext;

    @Before
    public void initMockAndCreateContext() throws ClassNotFoundException {
        Bundle bundle = mock(Bundle.class);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String name = (String) invocation.getArguments()[0];
                return OsgiConfigApplicationContextTest.class.getClassLoader().loadClass(name);
            }
        });
        this.applicationContext = new OsgiConfigApplicationContext(bundleContext);
    }

    @After
    public void destroyContext() {
        this.applicationContext.destroy();
        this.applicationContext = null;
    }


    @Test
    public void shouldFindRequiredService() {

        this.applicationContext.setRequiredServices(createServices(TestClass.class, TestClass.INSTANCE));
        this.applicationContext.register(MyConfiguration.class);
        this.applicationContext.refresh();

        Object bean = this.applicationContext.getBean(TestClass.class);

        assertThat(bean, instanceOf(TestClass.class));

        MyConfiguration myConfiguration = this.applicationContext.getBean(MyConfiguration.class);
        assertThat(myConfiguration.getTestClass(), instanceOf(TestClass.class));
    }

    private Map<String, SingleServiceTracker<?>> createServices(Class<TestClass> testClassClass, TestClass instance) {
        Map<String, SingleServiceTracker<?>> services = new HashMap<>();
        SingleServiceTracker singleServiceTracker = mock(SingleServiceTracker.class);
        when(singleServiceTracker.getService()).thenReturn(instance);
        when(singleServiceTracker.getClazz()).thenReturn(testClassClass);
        services.put(testClassClass.getName(), singleServiceTracker);
        return services;
    }

    public static interface TestClass {
        public static final TestClass INSTANCE = new TestClassImpl();
    }

    public static class TestClassImpl implements TestClass {
    }

    @Configuration
    public static class MyConfiguration {

        @Autowired
        private TestClass testClass;

        public TestClass getTestClass() {
            return testClass;
        }
    }
}
