/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spring.osgi.context.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OSGi bundle {@link org.springframework.beans.factory.config.Scope}
 * implementation.
 * <p/>
 * Will allow per--calling-bundle object instance, thus this scope becomes
 * useful when enabled on localBeans exposed as OSGi services.
 *
 * @author Costin Leau
 */

// This class relies heavily on the OSGi ServiceFactory (SF) behaviour.
// Since the OSGi platform automatically calls get/ungetService on a SF
// and caches the getService() object there is no need for caching inside the
// scope.
// This also means that the scope cannot interact with the cache and acts
// only as an object creator and nothing more in favor of the ServiceFactory.
// However, note that for the inner bundle, the scope has to mimic the OSGi
// behaviour.
// 
public class OsgiBundleScope implements Scope, DisposableBean {

    public static final String SCOPE_NAME = "bundle";

    private static final Logger log = LoggerFactory.getLogger(OsgiBundleScope.class);


    /**
     * Decorating {@link org.osgi.framework.ServiceFactory} used for supporting
     * 'bundle' scoped localBeans.
     *
     * @author Costin Leau
     */
    public static class BundleScopeServiceFactory<T> implements ServiceFactory<T> {

        private final ServiceFactory<T> decoratedServiceFactory;

        /**
         * destruction callbacks for bean instances
         */
        private final Map<Bundle, Runnable> callbacks = Collections.synchronizedMap(new HashMap<Bundle, Runnable>());


        public BundleScopeServiceFactory(ServiceFactory<T> serviceFactory) {
            Assert.notNull(serviceFactory);
            this.decoratedServiceFactory = serviceFactory;
        }

        /**
         * Called if a bundle requests a service for the first time (start the
         * scope).
         *
         * @see org.osgi.framework.ServiceFactory#getService(org.osgi.framework.Bundle,
         * org.osgi.framework.ServiceRegistration)
         */
        public T getService(Bundle bundle, ServiceRegistration<T> registration) {
            try {
                // tell the scope, it's an outside bundle that does the call
                EXTERNAL_BUNDLE.set(Boolean.TRUE);

                // create the new object (call the container)
                T obj = decoratedServiceFactory.getService(bundle, registration);

                // get callback (registered through the scope)
                Object passedObject = OsgiBundleScope.EXTERNAL_BUNDLE.get();

                // make sure it's not the marker object
                if (passedObject != null && passedObject instanceof Runnable) {
                    Runnable callback = (Runnable) OsgiBundleScope.EXTERNAL_BUNDLE.get();
                    if (callback != null)
                        callbacks.put(bundle, callback);
                }
                return obj;
            } finally {
                // clean ThreadLocal
                OsgiBundleScope.EXTERNAL_BUNDLE.set(null);
            }
        }

        /**
         * Called if a bundle releases the service (stop the scope).
         *
         * @see org.osgi.framework.ServiceFactory#ungetService(org.osgi.framework.Bundle,
         * org.osgi.framework.ServiceRegistration, Object)
         */
        public void ungetService(Bundle bundle, ServiceRegistration<T> registration, T service) {
            try {
                // tell the scope, it's an outside bundle that does the call
                EXTERNAL_BUNDLE.set(Boolean.TRUE);
                // unget object first
                decoratedServiceFactory.ungetService(bundle, registration, service);

                // then apply the destruction callback (if any)
                Runnable callback = callbacks.remove(bundle);
                if (callback != null)
                    callback.run();
            } finally {
                // clean ThreadLocal
                EXTERNAL_BUNDLE.set(null);
            }
        }
    }


    /**
     * ThreadLocal used for passing objects around {@link spring.osgi.context.internal.OsgiBundleScope} and
     * {@link spring.osgi.context.internal.OsgiBundleScope.BundleScopeServiceFactory} (there is only one scope instance but
     * multiple BSSFs).
     */
    public static final ThreadLocal<Object> EXTERNAL_BUNDLE = new ThreadLocal<>();

    /**
     * Map of localBeans imported by the current bundle from other bundles. This map
     * is sychronized and is used by
     * {@link spring.osgi.context.internal.OsgiBundleScope}.
     */
    private final Map<String, Object> localBeans = new LinkedHashMap<>(4);

    /**
     * Unsynchronized map of callbacks for the services used by the running
     * bundle.
     * <p/>
     * Uses the bean name as key and as value, a list of callbacks associated
     * with the bean instances.
     */
    private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>(8);


    private boolean isExternalBundleCalling() {
        return (EXTERNAL_BUNDLE.get() != null);
    }

    public Object get(String name, ObjectFactory objectFactory) {
        // outside bundle calling (no need to cache things)
        if (isExternalBundleCalling()) {
            return objectFactory.getObject();
        }
        // in-appCtx call
        else {
            // use local bean repository
            // cannot use a concurrent map since we want to postpone the call to
            // getObject
            synchronized (localBeans) {
                Object bean = localBeans.get(name);
                if (bean == null) {
                    bean = objectFactory.getObject();
                    localBeans.put(name, bean);
                }
                return bean;
            }
        }

    }

    public String getConversationId() {
        return null;
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        // pass the destruction callback to the ServiceFactory
        if (isExternalBundleCalling())
            EXTERNAL_BUNDLE.set(callback);
            // otherwise destroy the bean from the local cache
        else {
            destructionCallbacks.put(name, callback);
        }
    }

    /*
     * Unable to do this as we cannot invalidate the OSGi cache.
     */
    public Object remove(String name) {
        throw new UnsupportedOperationException();
    }

    /*
     * Clean up the scope (context refresh/close()).
     */
    public void destroy() {
        boolean debug = log.isDebugEnabled();

        // handle only the local cache/localBeans
        // the ServiceFactory object will be destroyed upon service
        // unregistration
        for (Map.Entry<String, Runnable> stringRunnableEntry : destructionCallbacks.entrySet()) {
            Map.Entry entry = (Map.Entry) stringRunnableEntry;
            Runnable callback = (Runnable) entry.getValue();

            if (debug)
                log.debug("destroying local bundle scoped bean [" + entry.getKey() + "]");

            callback.run();
        }

        destructionCallbacks.clear();
        localBeans.clear();
    }

    // handle Spring 3.0 expression related scope
    public Object resolveContextualObject(String key) {
        return null;
    }
}