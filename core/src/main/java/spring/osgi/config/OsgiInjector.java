package spring.osgi.config;

import spring.osgi.utils.MultipleServiceTracker;
import spring.osgi.utils.SingleServiceTracker;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by nico.
 */
public interface OsgiInjector {


    <T> void registerService(T o, Class<T> clazz, Dictionary<String, String> properties);
    void unregisterService (Object o);

    <T> void registerSingleton(Class<T> clazz, Dictionary<String, String> properties);
    void unregisterSingleton(Class<?> clazz);

    <T> void trackService(Class<T> clazz, String filter, SingleServiceTracker.SingleServiceListener<T> listener);

    <T> void trackService(Class<T> clazz, SingleServiceTracker.SingleServiceListener<T> listener);

    <T> void trackServices(Class<T> clazz, String filter, MultipleServiceTracker.MultipleServiceListener<T> listener);

    <T> void trackServices(Class<T> clazz, MultipleServiceTracker.MultipleServiceListener<T> listener);
}
