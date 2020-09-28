package org.openkirkes.java.test;

import org.junit.Test;
import org.openkirkes.java.Library;

import static org.junit.Assert.assertTrue;

public class LibraryTest {
    @Test public void testSomeLibraryMethod() {
        Library classUnderTest = new Library();
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
    }
}
