/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spring.osgi.utils;

import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseActivator implements BundleActivator, SingleServiceTracker.SingleServiceListener<Object>, Runnable {

    protected final Logger logger;
    protected BundleContext bundleContext;

    protected final ExecutorService executor = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    private final AtomicBoolean scheduled = new AtomicBoolean();

    private long schedulerStopTimeout = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

    private List<ServiceRegistration> registrations;
    private final Map<String, SingleServiceTracker<?>> trackers = new HashMap<>();
    private ServiceRegistration managedServiceRegistration;
    private Dictionary<String, ?> configuration;

    public BaseActivator(Logger logger) {
        if (logger == null){
            this.logger = LoggerFactory.getLogger(getClass());
        }
        else{
            this.logger = logger;
        }
    }

    public BaseActivator (){
        this(null);
    }

    public long getSchedulerStopTimeout() {
        return schedulerStopTimeout;
    }

    public void setSchedulerStopTimeout(long schedulerStopTimeout) {
        this.schedulerStopTimeout = schedulerStopTimeout;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        scheduled.set(true);
        doOpen();
        scheduled.set(false);
        if (managedServiceRegistration == null && trackers.isEmpty()) {
            try {
                doStart();
            } catch (Exception e) {
                logger.warn("Error starting activator", e);
                doStop();
            }
        } else {
            reconfigure();
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        scheduled.set(true);
        doClose();
        executor.shutdown();
        executor.awaitTermination(schedulerStopTimeout, TimeUnit.MILLISECONDS);
        doStop();
    }

    protected void doOpen() throws Exception {

    }

    protected void doClose() {
        if (managedServiceRegistration != null) {
            managedServiceRegistration.unregister();
        }
        for (SingleServiceTracker tracker : trackers.values()) {
            tracker.close();
        }
    }

    protected void doStart() throws Exception {
    }

    protected void doStop() {
        if (registrations != null) {
            for (ServiceRegistration reg : registrations) {
                reg.unregister();
            }
            registrations = null;
        }
    }

    /**
     * Called in {@link #doOpen()}
     */
    protected void manage(String pid) {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(Constants.SERVICE_PID, pid);
        managedServiceRegistration = bundleContext.registerService(
                "org.osgi.service.cm.ManagedService", this, props);
    }

    public void updated(Dictionary<String, ?> properties) {
        this.configuration = properties;
        reconfigure();
    }

    protected Dictionary<String, ?> getConfiguration() {
        return configuration;
    }

    /**
     * Called in {@link #doStart()}
     */
    protected int getInt(String key, int def) {
        if (configuration != null) {
            Object val = configuration.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            } else if (val != null) {
                return Integer.parseInt(val.toString());
            }
        }
        return def;
    }

    /**
     * Called in {@link #doStart()}
     */
    protected boolean getBoolean(String key, boolean def) {
        if (configuration != null) {
            Object val = configuration.get(key);
            if (val instanceof Boolean) {
                return (Boolean) val;
            } else if (val != null) {
                return Boolean.parseBoolean(val.toString());
            }
        }
        return def;
    }

    /**
     * Called in {@link #doStart()}
     */
    protected long getLong(String key, long def) {
        if (configuration != null) {
            Object val = configuration.get(key);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            } else if (val != null) {
                return Long.parseLong(val.toString());
            }
        }
        return def;
    }

    /**
     * Called in {@link #doStart()}
     */
    protected String getString(String key, String def) {
        if (configuration != null) {
            Object val = configuration.get(key);
            if (val != null) {
                return val.toString();
            }
        }
        return def;
    }

    @Override
    public void serviceFound(Object service) {
        reconfigure();
    }

    @Override
    public void serviceLost(Object service) {
        reconfigure();
    }

    @Override
    public void serviceReplaced(Object service) {
        reconfigure();
    }

    protected void reconfigure() {
        if (scheduled.compareAndSet(false, true)) {
            executor.submit(this);
        }
    }

    @Override
    public void run() {
        scheduled.set(false);
        doStop();
        try {
            doStart();
        } catch (Exception e) {
            logger.warn("Error starting activator", e);
            doStop();
        }
    }


    @SuppressWarnings("unchecked")
    protected void trackService(String id, Class<?> clazz, String filter) throws InvalidSyntaxException {
        if (!trackers.containsKey(id)) {
            SingleServiceTracker tracker = new SingleServiceTracker(bundleContext, clazz, filter, this);
            tracker.open();
            trackers.put(id, tracker);
        }
    }


    /**
     * Called in {@link #doStart()}
     */
    protected void registerMBean(Object mbean, String type) {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("jmx.objectname", "org.apache.karaf:" + type + ",name=" + System.getProperty("karaf.name"));
        trackRegistration(bundleContext.registerService(getInterfaceNames(mbean), mbean, props));
    }

    /**
     * Called in {@link #doStart()}
     */
    protected <T> void register(Class<T> clazz, T service) {
        register(clazz, service, null);
    }

    /**
     * Called in {@link #doStart()}
     */
    protected <T> void register(Class<T> clazz, T service, Dictionary<String, ?> props) {
        trackRegistration(bundleContext.registerService(clazz, service, props));
    }

    /**
     * Called in {@link #doStart()}
     */
    protected void register(Class[] clazz, Object service) {
        register(clazz, service, null);
    }

    /**
     * Called in {@link #doStart()}
     */
    protected void register(Class[] clazz, Object service, Dictionary<String, ?> props) {
        String[] names = new String[clazz.length];
        for (int i = 0; i < clazz.length; i++) {
            names[i] = clazz[i].getName();
        }
        trackRegistration(bundleContext.registerService(names, service, props));
    }

    protected boolean waitingTracker() {
        for (SingleServiceTracker<?> tracker : trackers.values()) {
            if (tracker.getService() == null) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, SingleServiceTracker<?>> getServicesResolved() {
        return new HashMap<>(trackers);
    }

    private void trackRegistration(ServiceRegistration registration) {
        if (registrations == null) {
            registrations = new ArrayList<>();
        }
        registrations.add(registration);
    }

    protected String[] getInterfaceNames(Object object) {
        List<String> names = new ArrayList<>();
        for (Class cl = object.getClass(); cl != Object.class; cl = cl.getSuperclass()) {
            addSuperInterfaces(names, cl);
        }
        return names.toArray(new String[names.size()]);
    }

    private void addSuperInterfaces(List<String> names, Class clazz) {
        for (Class cl : clazz.getInterfaces()) {
            names.add(cl.getName());
            addSuperInterfaces(names, cl);
        }
    }

}