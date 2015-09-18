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

package spring.osgi.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Costin Leau
 */
public class OsgiBundleResourceTest {

    private OsgiBundleResource resource;

    private Bundle bundle;

    private String path;

    @Before
    public void setUp() throws Exception {
        path = OsgiBundleResourceTest.class.getName().replace('.', '/').concat(".class");
        bundle = mock(Bundle.class);
        when(bundle.findEntries(anyString(), anyString(), anyBoolean())).then(new Answer<Enumeration<URL>>() {

            @Override
            public Enumeration<URL> answer(InvocationOnMock invocationOnMock) throws Throwable {
                String path = (String) invocationOnMock.getArguments()[0];
                String filePattern = (String) invocationOnMock.getArguments()[1];
                return OsgiBundleResourceTest.class.getClassLoader().getResources(path + "/" + filePattern);
            }
        });
        resource = new OsgiBundleResource(bundle, path);
    }

    @After
    public void tearDown() {
        path = null;
        bundle = null;
        resource = null;
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#hashCode()}.
     */
    @Test
    public void testHashCode() {
        assertEquals(path.hashCode(), resource.hashCode());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#OsgiBundleResource(org.osgi.framework.Bundle, java.lang.String)}.
     */
    @Test
    public void testOsgiBundleResource() {
        assertSame(bundle, resource.getBundle());
        assertEquals(path, resource.getPath());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getPath()}.
     */
    @Test
    public void testGetPath() {
        assertEquals(path, resource.getPath());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getBundle()}.
     */
    @Test
    public void testGetBundle() {
        assertSame(bundle, resource.getBundle());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getInputStream()}.
     */
    @Test
    public void testGetInputStream() throws Exception {
        InputStream stream = resource.getInputStream();
        assertNotNull(stream);
        stream.close();
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getURL()}.
     */
    @Test
    public void testGetURL() throws Exception {
        assertNotNull(resource.getURL());

        resource = new OsgiBundleResource(bundle, "osgibundle:foo" + path);
        try {
            resource.getURL();
            fail("should have thrown exception");
        } catch (Exception ex) {
            // expected
        }
    }

    @Test
    public void testNonBundleUrlWhichExists() throws Exception {
        File tmp = File.createTempFile("foo", "bar");
        tmp.deleteOnExit();
        resource = new OsgiBundleResource(bundle, "file:" + tmp.toString());
        assertNotNull(resource.getURL());
        assertTrue(resource.exists());
        tmp.delete();
    }

    @Test
    public void testNonBundleUrlWhichDoesNotExist() throws Exception {
        resource = new OsgiBundleResource(bundle, "file:foo123123");
        resource.getURL();
        assertFalse(resource.exists());
    }

    @Test
    public void testFileWithSpecialCharsInTheNameBeingResolved() throws Exception {
        String name = "file:./target/test-classes/test-file";
        FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
        fileLoader.setClassLoader(getClass().getClassLoader());

        Resource fileRes = fileLoader.getResource(name);
        resource = new OsgiBundleResource(bundle, name);

        testFileVsOsgiFileResolution(fileRes, resource);
    }

    @Test
    public void testFileWithEmptyCharsInTheNameBeingResolved() throws Exception {
        String name = "file:./target/test-classes/test file";
        FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
        fileLoader.setClassLoader(getClass().getClassLoader());

        Resource fileRes = fileLoader.getResource(name);
        resource = new OsgiBundleResource(bundle, name);

        testFileVsOsgiFileResolution(fileRes, resource);
    }

    @Test
    @Ignore
    public void testFileWithNormalCharsInTheNameBeingResolved() throws Exception {
        String name = "file:.project";
        FileSystemResourceLoader fileLoader = new FileSystemResourceLoader();
        fileLoader.setClassLoader(getClass().getClassLoader());

        Resource fileRes = fileLoader.getResource(name);

        resource = new OsgiBundleResource(bundle, name);
        testFileVsOsgiFileResolution(fileRes, resource);
    }

    private void testFileVsOsgiFileResolution(Resource fileRes, Resource otherRes) throws Exception {
        assertNotNull(fileRes.getURL());
        assertNotNull(fileRes.getFile());
        assertTrue(fileRes.getFile().exists());

        assertNotNull(otherRes.getURL());
        assertNotNull(otherRes.getFile());
        assertTrue(StringUtils.pathEquals(fileRes.getFile().getAbsolutePath(), otherRes.getFile().getAbsolutePath()));
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getResourceFromBundleSpace(java.lang.String)}.
     */
    @Test
    public void testGetResourceFromBundle() throws Exception {

        String location = "foo";
        URL result = new URL("file:/" + location);

        when(bundle.findEntries("/", "foo", false)).thenReturn(new Vector<>(Arrays.asList(result)).elements());
        assertEquals(result, resource.getResourceFromBundleSpace(location).getURL());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getResourceFromBundleClasspath(java.lang.String)}.
     */
    @Test
    public void testGetResourceFromBundleClasspath() throws Exception {


        String location = "file://foo";
        URL result = new URL(location);

        when(bundle.getResource(location)).thenReturn(result);
        assertSame(result, resource.getResourceFromBundleClasspath(location));
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#isRelativePath(java.lang.String)}.
     */
    @Test
    public void testIsRelativePath() {
        assertTrue(resource.isRelativePath("foo"));
        assertFalse(resource.isRelativePath("/foo"));
        assertFalse(resource.isRelativePath(":foo"));
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#createRelative(java.lang.String)}.
     */
    @Test
    public void testCreateRelativeString() {
        String location = "foo";
        Resource res = resource.createRelative(location);
        assertSame(OsgiBundleResource.class, res.getClass());
        assertEquals("spring/osgi/io/" + location, ((OsgiBundleResource) res).getPath());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getFilename()}.
     */
    @Test
    public void testGetFilename() {
        assertNotNull(resource.getFilename());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#getDescription()}.
     */
    @Test
    public void testGetDescription() {
        assertNotNull(resource.getDescription());
    }

    /**
     * Test method for
     * {@link spring.osgi.io.OsgiBundleResource#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject() {
        assertEquals(resource, new OsgiBundleResource(bundle, path));
        assertEquals(resource, resource);
        assertFalse(resource.equals(new OsgiBundleResource(bundle, "")));
        assertFalse(resource.equals(new OsgiBundleResource(mock(Bundle.class), path)));
    }

    @Test
    public void testDefaultPathWithinContext() throws Exception {
        assertEquals(path, resource.getPathWithinContext());
    }

    @Test
    public void testPathWithinBundleSpace() throws Exception {
        String contextPath = "folder/resource";
        resource = new OsgiBundleResource(bundle, "osgibundle:" + contextPath);
        assertEquals(contextPath, resource.getPathWithinContext());
    }

    @Test
    public void testPathWithinClassSpace() throws Exception {
        String contextPath = "folder/resource";
        resource = new OsgiBundleResource(bundle, "classpath:" + contextPath);
        assertEquals(contextPath, resource.getPathWithinContext());
    }

    @Test
    public void testPathWithinJarSpace() throws Exception {
        String contextPath = "folder/resource";
        resource = new OsgiBundleResource(bundle, "osgibundlejar:" + contextPath);
        assertEquals(contextPath, resource.getPathWithinContext());
    }

    @Test
    public void testPathOutsideContext() throws Exception {
        String contextPath = "folder/resource";
        resource = new OsgiBundleResource(bundle, "file:" + contextPath);
        assertNull(resource.getPathWithinContext());
    }

    @Test
    public void testLastModified() throws Exception {
        assertTrue("last modified should be non zero", resource.lastModified() > 0);
    }

    @Test
    public void testNonExistingFile() throws Exception {
        resource = new OsgiBundleResource(bundle, "file:/some/non.existing.file");
        File file = resource.getFile();
        assertNotNull(file);
        assertFalse(file.exists());
    }
}