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

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import static org.junit.Assert.*;

/**
 * @author Costin Leau
 */
public class OsgiResourceUtilsTest {

    @Test
    public void testNullGetPrefix() throws Exception {
        assertNotNull(OsgiResourceUtils.getPrefix(null));
        assertEquals(OsgiResourceUtils.EMPTY_PREFIX, OsgiResourceUtils.getPrefix(null));
    }

    @Test
    public void testGetPrefix() {
        String prefix = "foo" + OsgiResourceUtils.PREFIX_DELIMITER;
        String suffix = "bar";
        String path = prefix + suffix;
        assertEquals(prefix, OsgiResourceUtils.getPrefix(path));
    }

    @Test
    public void testDoublePrefix() throws Exception {
        String path = "noSuffix";
        assertEquals(OsgiResourceUtils.EMPTY_PREFIX, OsgiResourceUtils.getPrefix(path));
    }

    @Test
    public void testGetSearchTypeUnknown() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_UNKNOWN, OsgiResourceUtils.getSearchType("xxx:path"));
    }

    @Test
    public void testGetSearchTypeUnspecified() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED, OsgiResourceUtils.getSearchType("path"));
    }

    @Test
    public void testGetSearchTypeBundleSpace() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE, OsgiResourceUtils.getSearchType("osgibundle:path"));
    }

    @Test
    public void testGetSearchTypeBundleJar() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR, OsgiResourceUtils.getSearchType("osgibundlejar:path"));
    }

    @Test
    public void testGetSearchTypeBundleClassSpace() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE,
                OsgiResourceUtils.getSearchType(ResourceUtils.CLASSPATH_URL_PREFIX + "path"));
    }

    @Test
    public void testGetSearchTypeBundleClassAllSpace() {
        assertEquals(OsgiResourceUtils.PREFIX_TYPE_CLASS_ALL_SPACE, OsgiResourceUtils.getSearchType("classpath*:path"));
    }

    @Test
    public void testIsClassPathType() {
        assertTrue(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_CLASS_ALL_SPACE));
        assertTrue(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_CLASS_SPACE));
        assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_JAR));
        assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_BUNDLE_SPACE));
        assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_NOT_SPECIFIED));
        assertFalse(OsgiResourceUtils.isClassPathType(OsgiResourceUtils.PREFIX_TYPE_UNKNOWN));
    }

    @Test
    public void testStripPrefixWithNoPrefix() {
        String path = "path";
        assertEquals(path, OsgiResourceUtils.stripPrefix(path));
    }

    @Test
    public void testStripPrefix() throws Exception {
        String prefix = "xxx:";
        String path = "path";
        assertEquals(path, OsgiResourceUtils.stripPrefix(prefix + path));
    }

    @Test
    public void testConvertURLArraytoResourceArray() throws Exception {
        URL[] urls = new URL[]{new URL("file:///"),
                getClass().getResource("/" + getClass().getName().replace('.', '/') + ".class")};
        Resource[] resources = OsgiResourceUtils.convertURLArraytoResourceArray(urls);
        assertNotNull(resources);
        assertEquals(2, resources.length);

        for (int i = 0; i < resources.length; i++) {
            assertTrue(resources[i] instanceof UrlResource);
            assertEquals(urls[i], resources[i].getURL());
        }
    }

    @Test
    public void testConvertNullURLArraytoResourceArray() {
        assertNotNull(OsgiResourceUtils.convertURLArraytoResourceArray(null));
        assertEquals(0, OsgiResourceUtils.convertURLArraytoResourceArray(null).length);
    }

    @Test
    public void testConvertURLEnumerationToResourceArray() throws Exception {
        URL[] urls = new URL[]{new URL("file:///"),
                getClass().getResource("/" + getClass().getName().replace('.', '/') + ".class")};

        Resource[] resources = OsgiResourceUtils.convertURLEnumerationToResourceArray(new Vector<>(Arrays.asList(urls)).elements());

        assertNotNull(resources);
        assertEquals(2, resources.length);

        for (int i = 0; i < resources.length; i++) {
            assertTrue(resources[i] instanceof UrlResource);
            assertEquals(urls[i], resources[i].getURL());
        }
    }

    @Test
    public void testConvertNullURLEnumerationToResourceArray() {
        assertNotNull(OsgiResourceUtils.convertURLEnumerationToResourceArray(null));
        assertEquals(0, OsgiResourceUtils.convertURLEnumerationToResourceArray(null).length);
    }

    @Test
    public void testFindUpperFolderWOAFolder() throws Exception {
        String path = "path";
        assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
    }

    @Test
    public void testFindUpperFolderWOAProperString() throws Exception {
        String path = "p";
        assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
    }

    @Test
    public void testFindUpperFolderWRootFolder() throws Exception {
        String path = "/";
        assertEquals(path, OsgiResourceUtils.findUpperFolder(path));
    }

    @Test
    public void testFindUpperFolderWDoubleFolders() throws Exception {
        String path = "/path1/path2/";
        assertEquals("/path1/", OsgiResourceUtils.findUpperFolder(path));
    }

    @Test
    public void testFindUpperFolderWFileInsideFolder() throws Exception {
        String path = "/path/file";
        assertEquals("/path/", OsgiResourceUtils.findUpperFolder(path));
    }

    @Test
    public void testFindUpperFolderWRelativePath() throws Exception {
        String path = "path/file";
        assertEquals("path/", OsgiResourceUtils.findUpperFolder(path));
    }
}