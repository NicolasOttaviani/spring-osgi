package spring.osgi.context.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import spring.osgi.config.internal.OsgiInjectorImpl;
import spring.osgi.config.internal.SpringOsgiConfigurationProcessor;
import spring.osgi.context.BundleContextAware;
import spring.osgi.context.OsgiApplicationContext;
import spring.osgi.context.internal.classloader.ClassLoaderFactory;
import spring.osgi.io.OsgiBundleResource;
import spring.osgi.io.OsgiBundleResourcePatternResolver;
import spring.osgi.services.SingleServiceTrackerProxyFactoryBean;
import spring.osgi.utils.OsgiStringUtils;
import spring.osgi.utils.ServiceExporter;
import spring.osgi.utils.SingleServiceTracker;

import java.io.IOException;
import java.util.Map;

/**
 * Created by nico.
 */
public class OsgiConfigApplicationContext extends AnnotationConfigApplicationContext implements OsgiApplicationContext {

    /**
     * OSGi bundle - determined from the BundleContext
     */
    private final Bundle bundle;

    /**
     * OSGi bundle context
     */
    private final BundleContext bundleContext;

    private final OsgiBundleResourcePatternResolver osgiPatternResolver;
    private final OsgiInjectorImpl osgiInjector;
    private ClassLoader classLoader;

    private Map<String, SingleServiceTracker<?>> requiredServices;

    public OsgiConfigApplicationContext(BundleContext bundleContext) {
        super();
        this.bundleContext = bundleContext;
        this.bundle = bundleContext.getBundle();
        this.setClassLoader(createBundleClassLoader(this.bundle));

        this.setDisplayName(ClassUtils.getShortName(getClass()) + "(bundle=" + getBundleSymbolicName() + ")");
        this.osgiPatternResolver = createOsgiBundleResourcePatternResolver();
        this.osgiInjector = new OsgiInjectorImpl(bundleContext, this, new ServiceExporter(bundleContext));
    }

    public void setRequiredServices(Map<String, SingleServiceTracker<?>> requiredServices) {
        this.requiredServices = requiredServices;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }


    /**
     * This implementation supports pattern matching inside the OSGi bundle.
     *
     * @see spring.osgi.io.OsgiBundleResourcePatternResolver
     */
    @Override
    protected ResourcePatternResolver getResourcePatternResolver() {
        return osgiPatternResolver;
    }

    // delegate methods to a proper osgi resource loader


    @Override
    public Resource getResource(String location) {
        return (osgiPatternResolver != null ? osgiPatternResolver.getResource(location) : null);
    }

    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        return (osgiPatternResolver != null ? osgiPatternResolver.getResources(locationPattern) : null);
    }


    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);

        beanFactory.addBeanPostProcessor(new BundleContextAwareProcessor(this.bundleContext));
        beanFactory.addBeanPostProcessor(new SpringOsgiConfigurationProcessor(this.osgiInjector));
        beanFactory.ignoreDependencyInterface(BundleContextAware.class);

        //enforceExporterImporterDependency(beanFactory);

        // add bundleContext bean
        if (!beanFactory.containsLocalBean(BUNDLE_CONTEXT_BEAN_NAME)) {
            logger.trace("Registering BundleContext as a bean named " + BUNDLE_CONTEXT_BEAN_NAME);
            beanFactory.registerSingleton(BUNDLE_CONTEXT_BEAN_NAME, this.bundleContext);
        } else {
            logger.warn("A bean named " + BUNDLE_CONTEXT_BEAN_NAME
                    + " already exists; the bundleContext will not be registered as a bean");
        }

        // add bundleContext bean
        if (!beanFactory.containsLocalBean(OSGI_INJECTOR_BEAN_NAME)) {
            logger.trace("Registering osgiInjector as a bean named " + OSGI_INJECTOR_BEAN_NAME);
            beanFactory.registerSingleton(OSGI_INJECTOR_BEAN_NAME, this.osgiInjector);
        } else {
            logger.warn("A bean named " + OSGI_INJECTOR_BEAN_NAME
                    + " already exists; the osgiInjector will not be registered as a bean");
        }


        if (requiredServices != null) {
            for (Map.Entry<String, SingleServiceTracker<?>> service : requiredServices.entrySet()) {

                String beanName = service.getKey();
                if (!beanFactory.containsLocalBean(beanName)) {
                    logger.trace("Registering " + beanName
                            + " as a bean");

                    beanFactory.registerSingleton(beanName, new SingleServiceTrackerProxyFactoryBean(service.getValue()));
                } else {
                    logger.warn("A bean named " + beanName
                            + " already exists; it will not be registered as a bean");
                }

            }
        }
        // register a 'bundle' scope
        beanFactory.registerScope(OsgiBundleScope.SCOPE_NAME, new OsgiBundleScope());
    }


    private String getBundleSymbolicName() {
        return OsgiStringUtils.nullSafeSymbolicName(getBundle());
    }


    @Override
    public OsgiBundleResourcePatternResolver createOsgiBundleResourcePatternResolver() {
        return new OsgiBundleResourcePatternResolver(getBundle(), getClassLoader());
    }


    // delegate methods to a proper osgi resource loader

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }


    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected Resource getResourceByPath(String path) {
        Assert.notNull(path, "Path is required");
        return new OsgiBundleResource(this.bundle, path);
    }

    /**
     * Create the class loader that delegates to the underlying OSGi bundle.
     *
     * @param bundle bundle
     * @return classLoader
     */
    private ClassLoader createBundleClassLoader(Bundle bundle) {
        return ClassLoaderFactory.getBundleClassLoaderFor(bundle);
    }

    private void cleanOsgiBundleScope(ConfigurableListableBeanFactory beanFactory) {
        Scope scope = beanFactory.getRegisteredScope(OsgiBundleScope.SCOPE_NAME);
        if (scope != null && scope instanceof OsgiBundleScope) {
            if (logger.isDebugEnabled())
                logger.trace("Destroying existing bundle scope beans...");
            ((OsgiBundleScope) scope).destroy();
        }
    }

    /*
    * Clean up any beans from the bundle scope.
    */
    protected void destroyBeans() {
        super.destroyBeans();

        try {
            cleanOsgiBundleScope(getBeanFactory());
        } catch (Exception ex) {
            logger.warn("got exception when closing", ex);
        }
    }


    @Override
    public void destroy() {
        super.destroy();
        osgiInjector.destroy();
    }
}
