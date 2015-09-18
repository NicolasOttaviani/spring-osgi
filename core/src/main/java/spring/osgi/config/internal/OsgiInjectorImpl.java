package spring.osgi.config.internal;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import spring.osgi.config.OsgiInjector;
import spring.osgi.utils.MultipleServiceTracker;
import spring.osgi.utils.ServiceExporter;
import spring.osgi.utils.SingleServiceTracker;

import java.util.ArrayList;
import java.util.Dictionary;
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
    public <T> void registerService(T o, Class<T> clazz, Dictionary<String, String> properties) {
        this.exporter.registerService(o, clazz, properties);
    }

    @Override
    public void unregisterService(Object o) {
        this.exporter.unregister(o);
    }

    @Override
    public <T> void registerSingleton(Class<T> clazz, Dictionary<String, String> properties) {
        this.exporter.registerService(applicationContext.getBean(clazz), clazz, properties);
    }

    @Override
    public void unregisterSingleton(Class<?> clazz) {
        this.exporter.unregister(applicationContext.getBean(clazz));
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
}
