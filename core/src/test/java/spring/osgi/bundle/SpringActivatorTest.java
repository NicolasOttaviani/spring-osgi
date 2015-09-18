package spring.osgi.bundle;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.osgi.bundle.annotation.ProvideService;
import spring.osgi.bundle.annotation.RequireService;
import spring.osgi.bundle.annotation.Services;

import java.util.Dictionary;

import static org.mockito.Mockito.*;
import static spring.osgi.bundle.SpringActivatorTest.SpringActivatorWithBundleContext.create;

/**
 * Created by nico.
 */
public class SpringActivatorTest {

    @Test
    public void shouldTrackService() throws Exception {

        SpringActivatorWithBundleContext springActivator = create(ConfigurationRequires.class);
        springActivator.doOpen();
        verify(springActivator).trackService(ServiceA.class.getName(), ServiceA.class, null);
        verify(springActivator).trackService("serviceB", ServiceB.class, null);
        verify(springActivator).trackService("serviceC", ServiceC.class, "test.id = cool");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldRegisterService() throws Exception {

        SpringActivatorWithBundleContext springActivator = create(ConfigurationProvides.class);
        springActivator.doStart();
        verify(springActivator).register(eq(ServiceA.class), any(ServiceA.class), (Dictionary<String, ?>) isNull());
        verify(springActivator).register(eq(ServiceB.class), any(ServiceB.class), (Dictionary<String, ?>) isNull());
    }


    @Configuration
    @Services(
            requires = {
                    @RequireService(ServiceA.class),
                    @RequireService(id = "serviceB", value = ServiceB.class),
                    @RequireService(id = "serviceC", value = ServiceC.class, filter = "test.id = cool"),
            }
    )
    public static class ConfigurationRequires {

    }

    @Configuration
    @Services(
            provides = {
                    @ProvideService(ServiceA.class),
                    @ProvideService(ServiceB.class),
            }
    )
    public static class ConfigurationProvides {

        @Bean
        public ServiceA serviceA() {
            return new ServiceA();
        }

        @Bean
        public ServiceB serviceB() {
            return new ServiceB();
        }

    }


    public static class ServiceA {

    }

    public static class ServiceB {

    }

    public static class ServiceC {

    }

    public static class SpringActivatorWithBundleContext extends SpringActivator {

        public SpringActivatorWithBundleContext(Class<?> annotatedClass) throws ClassNotFoundException {
            super(annotatedClass);
            Bundle bundle = mock(Bundle.class);
            when(bundle.loadClass(anyString())).then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    String className = (String) invocationOnMock.getArguments()[0];
                    return SpringActivatorWithBundleContext.class.getClassLoader().loadClass(className);

                }
            });
            this.bundleContext = mock(BundleContext.class);
            when(this.bundleContext.getBundle()).thenReturn(bundle);

        }

        public static SpringActivatorWithBundleContext create(Class<?> annotatedClass) throws ClassNotFoundException {
            return spy(new SpringActivatorWithBundleContext(annotatedClass));
        }

        @Override
        public <T> void register(Class<T> clazz, T service, Dictionary<String, ?> props) {
            super.register(clazz, service, props);
        }

        @Override
        public void trackService(String id, Class<?> clazz, String filter) throws InvalidSyntaxException {
            super.trackService(id, clazz, filter);
        }
    }

}
