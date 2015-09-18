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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Costin Leau
 */
public class OsgiBundleUtilsTest {

    private Bundle bundle;

    private static int state;

    @Before
    public void setUp() throws Exception {
        OsgiBundleUtilsTest.state = Bundle.UNINSTALLED;
        bundle = mock(Bundle.class);
        when(bundle.getState()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return state;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        bundle = null;
    }

    @Test
    public void testIsInActiveBundleState() throws Exception {
        OsgiBundleUtilsTest.state = Bundle.ACTIVE;
        assertTrue(OsgiBundleUtils.isBundleActive(bundle));

        OsgiBundleUtilsTest.state = Bundle.STARTING;
        assertFalse(OsgiBundleUtils.isBundleActive(bundle));

        OsgiBundleUtilsTest.state = Bundle.INSTALLED;
        assertFalse(OsgiBundleUtils.isBundleActive(bundle));
    }

    @Test
    public void testIsBundleResolved() throws Exception {
        OsgiBundleUtilsTest.state = Bundle.UNINSTALLED;
        assertFalse(OsgiBundleUtils.isBundleResolved(bundle));

        OsgiBundleUtilsTest.state = Bundle.INSTALLED;
        assertFalse(OsgiBundleUtils.isBundleResolved(bundle));

        OsgiBundleUtilsTest.state = Bundle.ACTIVE;
        assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

        OsgiBundleUtilsTest.state = Bundle.RESOLVED;
        assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

        OsgiBundleUtilsTest.state = Bundle.STOPPING;
        assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

        OsgiBundleUtilsTest.state = Bundle.STARTING;
        assertTrue(OsgiBundleUtils.isBundleResolved(bundle));

    }

}