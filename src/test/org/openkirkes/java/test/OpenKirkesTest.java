package org.openkirkes.java.test;

import org.junit.Test;
import org.openkirkes.java.OpenKirkes;

import static org.junit.Assert.assertTrue;

public class OpenKirkesTest {
    @Test
    public void testSomeLibraryMethod() {
        OpenKirkes classUnderTest = new OpenKirkes();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
    }
}
