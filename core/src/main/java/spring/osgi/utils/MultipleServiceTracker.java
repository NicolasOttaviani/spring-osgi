package spring.osgi.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nico.
 */
public class MultipleServiceTracker<T> {

    public static interface MultipleServiceListener<T> {
        void serviceAdded(T service, Map<String, String> properties);

        void serviceRemoved(T service, Map<String, String> properties);
    }

    private final ServiceTracker<T, T> serviceTracker;

    public MultipleServiceTracker(BundleContext bundleContext, Class<T> clazz, String filter, MultipleServiceListener<T> listener) {
        this.serviceTracker = createServiceTracker(bundleContext, clazz, filter, listener);
    }

    public MultipleServiceTracker(BundleContext bundleContext, Class<T> clazz, MultipleServiceListener<T> listener) {
        this.serviceTracker = createServiceTracker(bundleContext, clazz, null, listener);
    }

    public void open() {
        this.serviceTracker.open();
    }

    public void close() {
        this.serviceTracker.close();
    }


    private static <T> ServiceTracker<T, T> createServiceTracker(BundleContext bundleContext, Class<T> clazz, String filter, final MultipleServiceListener<T> listener) {

        String filterString = '(' + Constants.OBJECTCLASS + '=' + clazz.getName() + ')';
        if (filter != null) filterString = "(&" + filterString + filter + ')';

        try {
            return new ServiceTracker<T, T>(bundleContext, bundleContext.createFilter(filterString), null) {
                @Override
                public T addingService(ServiceReference<T> reference) {
                    T service = super.addingService(reference);
                    listener.serviceAdded(service, retrieveProperties(reference));
                    return service;
                }

                @Override
                public void removedService(ServiceReference<T> reference, T service) {
                    listener.serviceRemoved(service, retrieveProperties(reference));
                    super.removedService(reference, service);
                }

                private Map<String, String> retrieveProperties(ServiceReference<T> reference) {
                    Map<String, String> properties = new HashMap<>();
                    for (String key: reference.getPropertyKeys()){
                        Object value = reference.getProperty(key);
                        if (value instanceof String){
                            properties.put(key, (String) value);
                        }
                    }
                    return properties;
                }
            };
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(String.format("Could not create service tracker. Filter [%s] is invalid", filter), e);
        }
    }
}