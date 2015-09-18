package spring.osgi.io;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Costin Leau
 */
public class OsgiBundleResourcePatternResolverTest {

    OsgiBundleResourcePatternResolver resolver;

    Bundle bundle;


    @Before
    public void setUp() throws Exception {
        bundle = mock(Bundle.class);
        resolver = new OsgiBundleResourcePatternResolver(bundle, OsgiBundleResourcePatternResolverTest.class.getClassLoader());

    }

    @After
    public void tearDown() {
        bundle = null;
        resolver = null;
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResourcePatternResolver(org.osgi.framework.Bundle)}.
     */
    @Test
    public void testOsgiBundleResourcePatternResolverBundle() {
        ResourceLoader res = resolver.getResourceLoader();
        assertTrue(res instanceof OsgiBundleResourceLoader);
        Resource resource = res.getResource("foo");
        assertSame(bundle, ((OsgiBundleResource) resource).getBundle());
        assertEquals(res.getResource("foo"), resolver.getResource("foo"));
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResourcePatternResolver#OsgiBundleResourcePatternResolver(org.springframework.core.io.ResourceLoader)}.
     */
    @Test
    public void testOsgiBundleResourcePatternResolverResourceLoader() {
        ResourceLoader resLoader = new DefaultResourceLoader();
        resolver = new OsgiBundleResourcePatternResolver(resLoader);
        ResourceLoader res = resolver.getResourceLoader();

        assertSame(resLoader, res);
        assertEquals(resLoader.getResource("foo"), resolver.getResource("foo"));
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResourcePatternResolver#getResources(java.lang.String)}.
     */
    @Test
    @Ignore
    public void testGetResourcesString() throws Exception {
        Resource[] res;

        try {
            resolver.getResources("classpath*:**/*");
            fail("should have thrown exception");
        } catch (Exception ex) {
            // expected
        }

        String thisClass = "spring/osgi/io/OsgiBundleResourcePatternResolverTest.class";

        res = resolver.getResources("osgibundle:" + thisClass);
        assertNotNull(res);
        assertEquals(1, res.length);
        assertThat(res[0], instanceOf(UrlResource.class));
    }
}