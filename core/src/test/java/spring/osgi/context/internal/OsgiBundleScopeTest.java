package spring.osgi.context.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

import static org.junit.Assert.*;


/**
 * Tests for OsgiBundleScope.
 *
 * @author Costin Leau
 */
public class OsgiBundleScopeTest {

    ObjectFactory objFactory;

    OsgiBundleScope scope;


    @Before
    public void setUp() throws Exception {
        scope = new OsgiBundleScope();
        OsgiBundleScope.EXTERNAL_BUNDLE.set(null);
    }

    @After
    public void tearDown() throws Exception {
        objFactory = null;
        scope.destroy();
        scope = null;
    }

    @Test
    public void testLocalBeans() {
        ObjectFactory factory = new ObjectFactory() {

            public Object getObject() throws BeansException {
                return new Object();
            }

        };
        Object foo = scope.get("foo", factory);
        Object foo2 = scope.get("foo", factory);
        assertNotNull(foo);
        assertSame("instance not cached", foo, foo2);

        Object bar = scope.get("bar", factory);
        Object bar2 = scope.get("bar", factory);
        assertNotNull(bar);
        assertSame("instance not cached", bar, bar2);
    }

    @Test
    public void testIsExternalBundleCalling() {
        assertFalse(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
        OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());
        assertTrue(OsgiBundleScope.EXTERNAL_BUNDLE.get() != null);
    }

    @Test
    public void testLocalDestructionCallback() {

        final Object[] callbackCalls = new Object[1];

        scope.registerDestructionCallback("foo", new Runnable() {

            public void run() {
                callbackCalls[0] = Boolean.TRUE;
            }
        });

        scope.destroy();
        assertSame(Boolean.TRUE, callbackCalls[0]);
    }

    @Test
    public void testDestructionCallbackPassedAround() {
        OsgiBundleScope.EXTERNAL_BUNDLE.set(new Object());

        Runnable callback = new Runnable() {

            public void run() {
            }
        };

        scope.registerDestructionCallback("foo", callback);
        assertSame(callback, OsgiBundleScope.EXTERNAL_BUNDLE.get());
    }
}