package spring.osgi.config.internal;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import spring.osgi.config.OsgiInjector;
import spring.osgi.utils.MultipleServiceTracker;
import spring.osgi.utils.ServiceExporter;
import spring.osgi.utils.SingleServiceTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nico.
 */
public class OsgiInjectorImpl implements OsgiInjector, DisposableBean {

    private final BundleContext bundleContext;
    private final ApplicationContext applicationContext;
    private final ServiceExporter exporter;
    private final List<SingleServiceTracker<?>> singleServiceTrackers = new ArrayList<>();
    private final List<MultipleServiceTracker<?>> multipleServiceTrackers = new ArrayList<>();


    public OsgiInjectorImpl(BundleContext bundleContext, ApplicationContext applicationContext, ServiceExporter exporter) {
        this.bundleContext = bundleContext;
        this.applicationContext = applicationContext;
        this.exporter = exporter;
    }

    @Override
    public <T> void registerService(String id, T o, Class<T> clazz) {
        this.exporter.registerService(id, o, clazz);
    }

    @Override
    public void unregisterService(String id) {
        this.exporter.unregister(id);
    }

    @Override
    public <T> void registerSingleton(Class<T> clazz) {
        String beanName = getSingletonName(clazz);
        this.exporter.registerService(beanName, applicationContext.getBean(clazz), clazz);
    }

    @Override
    public void unregisterSingleton(Class<?> clazz) {
        String beanName = getSingletonName(clazz);
        this.exporter.unregister(beanName);
    }

    @Override
    public <T> void trackService(Class<T> clazz, String filter, SingleServiceTracker.SingleServiceListener<T> listener){
        SingleServiceTracker<T> tracker = new SingleServiceTracker<>(bundleContext, clazz, filter, listener);
        this.singleServiceTrackers.add(tracker);
        tracker.open();
    }

    @Override
    public <T> void trackService(Class<T> clazz, SingleServiceTracker.SingleServiceListener<T> listener){
        this.trackService(clazz, null, listener);
    }

    @Override
    public <T> void trackServices(Class<T> clazz, String filter, final MultipleServiceTracker.MultipleServiceListener<T> listener){
        MultipleServiceTracker<T> multipleServiceTracker = new MultipleServiceTracker<>(this.bundleContext, clazz, filter, listener);
        multipleServiceTrackers.add(multipleServiceTracker);
        multipleServiceTracker.open();
    }

    @Override
    public <T> void trackServices(Class<T> clazz, MultipleServiceTracker.MultipleServiceListener<T> listener){
        this.trackServices(clazz, null, listener);
    }

    @Override
    public void destroy() {
        this.exporter.destroy();
        for (SingleServiceTracker<?> singleServiceTracker : this.singleServiceTrackers) {
            singleServiceTracker.close();
        }
        this.singleServiceTrackers.clear();
        for (MultipleServiceTracker<?> multipleServiceTracker : this.multipleServiceTrackers) {
            multipleServiceTracker.close();
        }
        this.multipleServiceTrackers.clear();
    }

    private <T> String getSingletonName(Class<T> clazz) {
        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        if (beanNames.length == 0){
            throw new IllegalArgumentException(String.format("Could not register singleton with class[%s]. Singleton not found.", clazz));
        }
        if (beanNames.length > 1){
            throw new IllegalArgumentException(String.format("Could not register singleton with class[%s]. Expected 1 singleton, found %d", clazz, beanNames.length));
        }
        return beanNames[0];
    }
}
