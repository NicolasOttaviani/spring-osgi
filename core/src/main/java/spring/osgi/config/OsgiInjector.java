package spring.osgi.config;

import spring.osgi.utils.MultipleServiceTracker;
import spring.osgi.utils.SingleServiceTracker;

/**
 * Created by nico.
 */
public interface OsgiInjector {

    final String SERVICE_ID_PROPERTY_KEY = "serviceId";

    <T> void registerService(String id, T o, Class<T> clazz);
    void unregisterService (String id);

    <T> void registerSingleton(Class<T> clazz);
    void unregisterSingleton(Class<?> clazz);

    <T> void trackService(Class<T> clazz, String filter, SingleServiceTracker.SingleServiceListener<T> listener);

    <T> void trackService(Class<T> clazz, SingleServiceTracker.SingleServiceListener<T> listener);

    <T> void trackServices(Class<T> clazz, String filter, MultipleServiceTracker.MultipleServiceListener<T> listener);

    <T> void trackServices(Class<T> clazz, MultipleServiceTracker.MultipleServiceListener<T> listener);


}
