package spring.osgi.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Costin Leau
 */
public class OsgiBundleResourceLoaderTest {

    private Bundle bundle;
    private OsgiBundleResourceLoader loader;

    @Before
    public void setUp() throws Exception {
        bundle = mock(Bundle.class);
        loader = new OsgiBundleResourceLoader(bundle, OsgiBundleResourceLoaderTest.class.getClassLoader());
    }

    @After
    public void tearDown() throws Exception {
        loader = null;
        bundle = null;
    }

    @Test
    public void testGetClasspathResource() throws Exception {
        String res = "foo.txt";
        URL expected = new URL("file://" + res);
        when(bundle.getResource(res)).thenReturn(expected);

        Resource resource = loader.getResource("classpath:" + res);
        assertNotNull(resource);
        assertSame(expected, resource.getURL());
    }

    @Test
    public void testGetBundleResource() throws Exception {
        String res = "foo.txt";
        URL url = new URL("file:/" + res);
        when(bundle.findEntries("/", res, false)).thenReturn(new Vector<>(Arrays.asList(url)).elements());

        Resource resource = loader.getResource("osgibundle:/" + res);
        assertNotNull(resource);
        assertSame(url, resource.getURL());
    }

    @Test
    public void testGetRelativeResource() throws Exception {
        String res = "foo.txt";
        URL expected = new URL("file:/" + res);

        Resource resource = loader.getResource("file:/" + res);
        assertNotNull(resource);
        assertEquals(expected, resource.getURL());
    }

    @Test
    public void testGetFallbackResource() throws Exception {
        String res = "foo.txt";
        URL expected = new URL("http:/" + res);

        Resource resource = loader.getResource("http:/" + res);
        assertNotNull(resource);
        assertEquals(expected, resource.getURL());
    }

    @Test
    public void testGetResourceByPath() throws Exception {
        try {
            loader.getResourceByPath(null);
            fail("should have thrown exception");
        } catch (Exception ex) {
            // expected
        }
        String path = "foo";
        Resource res = loader.getResourceByPath(path);
        assertNotNull(res);
        assertSame(OsgiBundleResource.class, res.getClass());
        assertEquals(path, ((OsgiBundleResource) res).getPath());
    }
}
