package spring.osgi.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by nico.
 */
public class ServiceExporter {

    private final Map<Integer, ServiceRegistration<?>> registrations = new HashMap<>();

    private final BundleContext bundleContext;

    public ServiceExporter(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public <T> void registerService(T o, Class<T> clazz, Dictionary<String, String> properties) {
        registrations.put(o.hashCode(), bundleContext.registerService(clazz, o, properties));
    }

    public <T> void registerService(T o, Class<T> clazz) {
        this.registerService(o, clazz, createDefaultProperties());
    }

    public void unregister (Object o){
        ServiceRegistration<?> registration = this.registrations.get(o.hashCode());
        if (registration != null){
            registration.unregister();
        }
    }

    public void destroy() {
        for (ServiceRegistration<?> registration : this.registrations.values()) {
            registration.unregister();
        }
        this.registrations.clear();
    }

    public Dictionary<String, String> createDefaultProperties() {
        return new Hashtable<>();
    }

}