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

package spring.osgi.context;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ConfigurableApplicationContext;
import spring.osgi.io.OsgiBundleResourcePatternResolver;

/**
 * Interface that extends <code>ConfigurableApplicationContext</code> to
 * provides OSGi specific functionality.
 * <p/>
 * <p/>
 * <strong>Note:</strong> Just like its ancestors,the setters of this interface
 * should be called before <code>refresh</code>ing the
 * <code>ApplicationContext</code>
 *
 * @author Costin Leau
 */
public interface OsgiApplicationContext extends ConfigurableApplicationContext {

    /**
     * Bean name under which the OSGi bundle context is published as a
     * singleton.
     */
    static final String BUNDLE_CONTEXT_BEAN_NAME = "bundleContext";

    /**
     * Bean name under which the OSGi bundle injector is published as a
     * singleton.
     */
    static final String OSGI_INJECTOR_BEAN_NAME = "osgiInjector";


    /**
     * Return the <code>BundleContext</code> for this application context.
     * This method is offered as a helper since as of OSGi 4.1, the bundle
     * context can be discovered directly from the given bundle.
     *
     * @return the <code>BundleContext</code> in which this application
     * context runs
     * @see #getBundle()
     */
    BundleContext getBundleContext();

    /**
     * Returns the OSGi <code>Bundle</code> for this application context.
     *
     * @return the <code>Bundle</code> for this OSGi bundle application
     * context.
     */
    Bundle getBundle();

    OsgiBundleResourcePatternResolver createOsgiBundleResourcePatternResolver();

    void destroy();

    void register(java.lang.Class<?>... annotatedClasses);
}