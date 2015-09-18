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

package spring.osgi.context.internal.classloader;

import org.osgi.framework.Bundle;
import org.springframework.util.Assert;

/**
 * Simple factory for generating Bundle/AOP-suitable class loaders used
 * internally by Spring-DM for generating proxies. The factory acts as a generic
 * facade for framework components hiding the implementation details (or the
 * changes in strategy).
 * <p/>
 * <p/> Internally the factory will try to use a cache to avoid creating
 * unneeded class loader (even if lightweight) to avoid polluting the JDK/CGLIB
 * class loader maps.
 *
 * @author Costin Leau
 */
public abstract class ClassLoaderFactory {

    /**
     * plug-able, private, bundle loader factory
     */
    private static final BundleClassLoaderFactory bundleClassLoaderFactory = new CachingBundleClassLoaderFactory();


    /**
     * Returns the wrapped class loader for the given bundle.
     * <p/>
     * <p/>
     * <p/> Useful when creating importers/exporters programmatically.
     *
     * @param bundle OSGi bundle
     * @return associated wrapping class loader
     */
    public static ClassLoader getBundleClassLoaderFor(Bundle bundle) {
        Assert.notNull(bundle);
        return bundleClassLoaderFactory.createClassLoader(bundle);
    }
}
