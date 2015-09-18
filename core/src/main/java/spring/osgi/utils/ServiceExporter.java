package spring.osgi.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static spring.osgi.config.OsgiInjector.SERVICE_ID_PROPERTY_KEY;

/**
 * Created by nico.
 */
public class ServiceExporter {

    private final Map<String, ServiceRegistration<?>> registrations = new HashMap<>();

    private final BundleContext bundleContext;

    public ServiceExporter(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public <T> void registerService(String id, T o, Class<T> clazz, Dictionary<String, String> properties) {
        checkIdNotRegistered(id);
        registrations.put(id, bundleContext.registerService(clazz, o, createDefaultProperties(id, properties)));
    }

    public <T> void registerService(String id, T o, Class<T> clazz) {
        this.registerService(id, o, clazz, createDefaultProperties(id));
    }

    public void unregister (String id){
        ServiceRegistration<?> registration = this.registrations.get(id);
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

    public Dictionary<String, String> createDefaultProperties(String id) {
        return createDefaultProperties(id, new Hashtable<String, String>());
    }

    private Dictionary<String,String> createDefaultProperties(String id, Dictionary<String,String> properties) {
        properties.put(SERVICE_ID_PROPERTY_KEY, id);
        return properties;
    }


    private void checkIdNotRegistered(String id) {
        if (registrations.containsKey(id)){
            throw new IllegalArgumentException(String.format("could not register a service with id %s. A service with the same id is already registered", id));
        }
    }


}