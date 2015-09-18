/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package spring.osgi.utils;

import org.osgi.framework.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

//This is from aries util
public class SingleServiceTracker<T> implements ServiceListener {


    public static interface SingleServiceListener<T> {
        public void serviceFound(T service);

        public void serviceLost(T service);

        public void serviceReplaced(T service);
    }

    private final BundleContext ctx;
    private final Class<T> clazz;
    private final String className;
    private final AtomicReference<T> service = new AtomicReference<>();
    private final AtomicReference<ServiceReference> ref = new AtomicReference<>();
    private final AtomicBoolean open = new AtomicBoolean(false);
    private final SingleServiceListener<T> serviceListener;
    private final String filterString;
    private final Filter filter;


    public SingleServiceTracker(BundleContext context, Class<T> clazz, String filterString, SingleServiceListener<T> sl) {
        this.ctx = context;
        this.clazz = clazz;
        this.className = clazz.getName();
        this.serviceListener = sl;
        if (filterString == null || filterString.isEmpty()) {
            this.filterString = null;
            this.filter = null;
        } else {
            this.filterString = filterString;
            try {
                this.filter = context.createFilter(filterString);
            } catch (InvalidSyntaxException e) {
                throw new IllegalArgumentException(String.format("Could not create service tracker : Filter [%s] is invalid", filterString), e);
            }
        }
    }

    public T getService() {
        return clazz.cast(service.get());
    }

    public ServiceReference getServiceReference() {
        return ref.get();
    }

    public void open() {
        if (open.compareAndSet(false, true)) {
            try {
                String filterString = '(' + Constants.OBJECTCLASS + '=' + className + ')';
                if (filter != null) filterString = "(&" + filterString + filter + ')';
                ctx.addServiceListener(this, filterString);
                findMatchingReference(null);
            } catch (InvalidSyntaxException e) {
                // this can never happen. (famous last words :)
            }
        }
    }

    public void serviceChanged(ServiceEvent event) {
        if (open.get()) {
            if (event.getType() == ServiceEvent.UNREGISTERING) {
                ServiceReference deadRef = event.getServiceReference();
                if (deadRef.equals(ref.get())) {
                    findMatchingReference(deadRef);
                }
            } else if (event.getType() == ServiceEvent.REGISTERED && ref.get() == null) {
                findMatchingReference(null);
            }
        }
    }

    private void findMatchingReference(ServiceReference original) {
        try {
            boolean clear = true;
            ServiceReference[] refs = ctx.getServiceReferences(className, filterString);
            if (refs != null && refs.length > 0) {
                if (refs.length > 1) {
                    Arrays.sort(refs);
                }
                @SuppressWarnings("unchecked")
                T service = (T) ctx.getService(refs[0]);
                if (service != null) {
                    clear = false;

                    // We do the unget out of the lock so we don't exit this class while holding a lock.
                    if (!update(original, refs[0], service)) {
                        ctx.ungetService(refs[0]);
                    }
                }
            } else if (original == null) {
                clear = false;
            }

            if (clear) {
                update(original, null, null);
            }
        } catch (InvalidSyntaxException e) {
            // this can never happen. (famous last words :)
        }
    }

    private boolean update(ServiceReference deadRef, ServiceReference newRef, T service) {
        boolean result = false;
        int foundLostReplaced = -1;

        // Make sure we don't try to get a lock on null
        Object lock;

        // we have to choose our lock.
        if (newRef != null) lock = newRef;
        else if (deadRef != null) lock = deadRef;
        else lock = this;

        // This lock is here to ensure that no two threads can set the ref and service
        // at the same time.
        synchronized (lock) {
            if (open.get()) {
                result = this.ref.compareAndSet(deadRef, newRef);
                if (result) {
                    this.service.set(service);

                    if (deadRef == null && newRef != null) foundLostReplaced = 0;
                    if (deadRef != null && newRef == null) foundLostReplaced = 1;
                    if (deadRef != null && newRef != null) foundLostReplaced = 2;
                }
            }
        }

        if (serviceListener != null) {
            if (foundLostReplaced == 0) serviceListener.serviceFound(this.service.get());
            else if (foundLostReplaced == 1) serviceListener.serviceLost(this.service.get());
            else if (foundLostReplaced == 2) serviceListener.serviceReplaced(this.service.get());
        }

        return result;
    }

    public void close() {
        if (open.compareAndSet(true, false)) {
            ctx.removeServiceListener(this);

            ServiceReference deadRef;
            synchronized (this) {
                deadRef = ref.getAndSet(null);
                service.set(null);
            }
            if (deadRef != null) {
                serviceListener.serviceLost(this.service.get());
                ctx.ungetService(deadRef);
            }
        }
    }

    public Class<T> getClazz() {
        return clazz;
    }
}