/*
 * Copyright 2006-2009 the original author or authors.
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

package spring.osgi.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.*;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Costin Leau
 */
public class OsgiStringUtilsTest {

    private static int state;

    private Bundle bundle;


    @Before
    public void setUp() throws Exception {
        OsgiStringUtilsTest.state = Bundle.UNINSTALLED;
        bundle = mock(Bundle.class);
        when(bundle.getState()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return state;
            }
        });
    }

    @After
    public void tearDown() {
        bundle = null;
    }

    @Test
    public void testGetBundleEventAsString() {
        Assert.assertEquals("INSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.INSTALLED));
        Assert.assertEquals("STARTING", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.STARTING));
        Assert.assertEquals("UNINSTALLED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UNINSTALLED));
        Assert.assertEquals("UPDATED", OsgiStringUtils.nullSafeBundleEventToString(BundleEvent.UPDATED));
        Assert.assertTrue(OsgiStringUtils.nullSafeBundleEventToString(-1324).startsWith("UNKNOWN"));
    }

    @Test
    public void testGetBundleStateAsName() throws Exception {
        OsgiStringUtilsTest.state = Bundle.ACTIVE;
        Assert.assertEquals("ACTIVE", OsgiStringUtils.bundleStateAsString(bundle));
        OsgiStringUtilsTest.state = Bundle.STARTING;
        Assert.assertEquals("STARTING", OsgiStringUtils.bundleStateAsString(bundle));
        OsgiStringUtilsTest.state = Bundle.STOPPING;
        Assert.assertEquals("STOPPING", OsgiStringUtils.bundleStateAsString(bundle));
        OsgiStringUtilsTest.state = -123;
        Assert.assertEquals("UNKNOWN STATE", OsgiStringUtils.bundleStateAsString(bundle));
    }

    @Test
    public void testNullSafeToStringBundleEvent() throws Exception {
        Assert.assertEquals("INSTALLED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.INSTALLED, bundle)));
        Assert.assertEquals("UPDATED", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.UPDATED, bundle)));
        Assert.assertEquals("STOPPING", OsgiStringUtils.nullSafeToString(new BundleEvent(BundleEvent.STOPPING, bundle)));
    }

    @Test
    public void testNullSafeToStringBundleEventNull() throws Exception {
        Assert.assertNotNull(OsgiStringUtils.nullSafeToString((BundleEvent) null));
    }

    @Test
    public void testNullSafeToStringBundleEventInvalidType() throws Exception {
        Assert.assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new BundleEvent(-123, bundle)));
    }

    @Test
    public void testNullSafeToStringServiceEvent() throws Exception {
        ServiceReference ref = new MockServiceReference();
        Assert.assertEquals("REGISTERED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.REGISTERED, ref)));
        Assert.assertEquals("MODIFIED", OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.MODIFIED, ref)));
        Assert.assertEquals("UNREGISTERING",
                OsgiStringUtils.nullSafeToString(new ServiceEvent(ServiceEvent.UNREGISTERING, ref)));
    }

    @Test
    public void testNullSafeToStringServiceEventNull() throws Exception {
        Assert.assertNotNull(OsgiStringUtils.nullSafeToString((ServiceEvent) null));
    }

    @Test
    public void testNullSafeToStringServiceEventInvalidType() throws Exception {
        Assert.assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new ServiceEvent(-123,
                new MockServiceReference())));
    }

    @Test
    public void testNullSafeToStringFrameworkEvent() throws Exception {
        Bundle bundle = mock(Bundle.class);
        Throwable th = new Exception();
        Assert.assertEquals("STARTED",
                OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.STARTED, bundle, th)));
        Assert.assertEquals("ERROR", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.ERROR, bundle, th)));

        Assert.assertEquals("WARNING",
                OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.WARNING, bundle, th)));

        Assert.assertEquals("INFO", OsgiStringUtils.nullSafeToString(new FrameworkEvent(FrameworkEvent.INFO, bundle, th)));

        Assert.assertEquals("PACKAGES_REFRESHED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
                FrameworkEvent.PACKAGES_REFRESHED, bundle, th)));

        Assert.assertEquals("STARTLEVEL_CHANGED", OsgiStringUtils.nullSafeToString(new FrameworkEvent(
                FrameworkEvent.STARTLEVEL_CHANGED, bundle, th)));
    }

    @Test
    public void testNullSafeToStringFrameworkEventNull() throws Exception {
        Assert.assertNotNull(OsgiStringUtils.nullSafeToString((FrameworkEvent) null));
    }

    @Test
    public void testNullSafeToStringFrameworkEventInvalidType() throws Exception {
        Assert.assertEquals("UNKNOWN EVENT TYPE", OsgiStringUtils.nullSafeToString(new FrameworkEvent(-123, bundle,
                new Exception())));
    }

    @Test
    public void testNullSafeToStringServiceReference() throws Exception {
        String symName = "symName";

        Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn(symName);
        when(bundle.getHeaders()).thenReturn(new Hashtable<String, String>());
        Properties props = new Properties();
        String header = "HEADER";
        String value = "VALUE";
        props.put(header, value);
        MockServiceReference<Object> ref = new MockServiceReference<>(bundle, props, null);
        String out = OsgiStringUtils.nullSafeToString(ref);
        Assert.assertTrue(out.contains(symName));
        Assert.assertTrue(out.contains(header));
        Assert.assertTrue(out.contains(value));
    }

    @Test
    public void testNullSafeToStringServiceReferenceNull() throws Exception {
        Assert.assertNotNull(OsgiStringUtils.nullSafeToString((ServiceReference) null));
    }

    /**
     * ServiceReference mock.
     * <p/>
     * <p/> This mock tries to adhere to the OSGi spec as much as possible by
     * providing the mandatory serviceId properties such as
     * {@link Constants#SERVICE_ID}, {@link Constants#OBJECTCLASS} and
     * {@link Constants#SERVICE_RANKING}.
     *
     * @author Costin Leau
     */
    static class MockServiceReference<T> implements ServiceReference<T> {

        private final Bundle bundle;

        private static long GLOBAL_SERVICE_ID = System.currentTimeMillis();

        private long serviceId;

        // private ServiceRegistration registration;
        private final Dictionary<Object, Object> properties;

        private String[] objectClass = new String[]{Object.class.getName()};


        /**
         * Constructs a new <code>MockServiceReference</code> instance using
         * defaults.
         */
        public MockServiceReference() {
            this(null, null, null);
        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance associated
         * with the given bundle.
         *
         * @param bundle associated reference bundle
         */
        public MockServiceReference(Bundle bundle) {
            this(bundle, null, null);
        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance matching
         * the given class namess.
         *
         * @param classes associated class names
         */
        public MockServiceReference(String[] classes) {
            this(null, null, null, classes);

        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance associated
         * with the given bundle and matching the given class names.
         *
         * @param bundle  associated bundle
         * @param classes matching class names
         */
        public MockServiceReference(Bundle bundle, String[] classes) {
            this(bundle, null, null, classes);
        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance associated
         * with the given service registration.
         *
         * @param registration service registration
         */
        public MockServiceReference(ServiceRegistration<T> registration) {
            this(null, null, registration);
        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance associated
         * with the given bundle, service registration and having the given service
         * properties.
         *
         * @param bundle       associated bundle
         * @param properties   reference properties
         * @param registration associated service registrations
         */
        public MockServiceReference(Bundle bundle, Dictionary<Object,Object> properties, ServiceRegistration<T> registration) {
            this(bundle, properties, registration, null);
        }

        /**
         * Constructs a new <code>MockServiceReference</code> instance. This
         * constructor gives access to all the parameters of the mock service
         * reference such as associated bundle, reference properties, service
         * registration and reference class names.
         *
         * @param bundle       associated bundle
         * @param properties   reference properties
         * @param registration service registration
         * @param classes      reference class names
         */
        public MockServiceReference(Bundle bundle, Dictionary<Object,Object>  properties, ServiceRegistration<T> registration, String[] classes) {
            this.bundle = (bundle == null ? mock(Bundle.class) : bundle);
            // this.registration = (registration == null ? new
            // MockServiceRegistration() :
            // registration);
            this.properties = (properties == null ? new Hashtable<>() : properties);
            if (classes != null && classes.length > 0)
                this.objectClass = classes;
            addMandatoryProperties(this.properties);
        }

        private void addMandatoryProperties(Dictionary<Object,Object>  dict) {
            // add mandatory properties
            Object id = dict.get(Constants.SERVICE_ID);
            if (id == null || !(id instanceof Long))
                dict.put(Constants.SERVICE_ID, GLOBAL_SERVICE_ID++);

            if (dict.get(Constants.OBJECTCLASS) == null)
                dict.put(Constants.OBJECTCLASS, objectClass);

            Object ranking = dict.get(Constants.SERVICE_RANKING);
            if (ranking == null || !(ranking instanceof Integer))
                dict.put(Constants.SERVICE_RANKING, 0);

            serviceId = (Long) dict.get(Constants.SERVICE_ID);
        }

        public Bundle getBundle() {
            return bundle;
        }

        public Object getProperty(String key) {
            return properties.get(key);
        }

        public String[] getPropertyKeys() {
            String[] keys = new String[this.properties.size()];
            Enumeration ks = this.properties.keys();

            for (int i = 0; i < keys.length && ks.hasMoreElements(); i++) {
                keys[i] = (String) ks.nextElement();
            }

            return keys;
        }

        public Bundle[] getUsingBundles() {
            return new Bundle[]{};
        }

        public boolean isAssignableTo(Bundle bundle, String className) {
            return false;
        }

        /**
         * Two mock service references are equal if they contain the same service
         * id.
         */
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj instanceof MockServiceReference<?>) {
                return this.hashCode() == obj.hashCode();
            }
            return false;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Returns a hash code based on the class and service id.
         */
        public int hashCode() {
            return MockServiceReference.class.hashCode() * 13 + (int) serviceId;
        }

        public String toString() {
            return "mock service reference [owning bundle id=" + bundle.hashCode() + "|props : " + properties + "]";
        }

        public int compareTo(Object reference) {
            @SuppressWarnings("unchecked")
            ServiceReference<T> other = (ServiceReference<T>) reference;

            // compare based on service ranking

            Object ranking = this.getProperty(Constants.SERVICE_RANKING);
            // if the property is not supplied or of incorrect type, use the default
            int rank1 = ((ranking != null && ranking instanceof Integer) ? (Integer) ranking : 0);
            ranking = other.getProperty(Constants.SERVICE_RANKING);
            int rank2 = ((ranking != null && ranking instanceof Integer) ? (Integer) ranking : 0);

            int result = rank1 - rank2;

            if (result == 0) {
                long id1 = serviceId;
                long id2 = (Long) other.getProperty(Constants.SERVICE_ID);

                // when comparing IDs, make sure to return inverse results (i.e. lower
                // id, means higher service)
                return (int) (id2 - id1);
            }

            return result;
        }
    }
}