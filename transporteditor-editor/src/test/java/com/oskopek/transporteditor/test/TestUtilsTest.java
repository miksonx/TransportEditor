package com.oskopek.transporteditor.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestUtilsTest {

    @Test
    public void isThrown() throws Exception {
        Runnable throwISE = () -> {
            throw new IllegalStateException("");
        };
        assertTrue(TestUtils.isThrown(throwISE, IllegalStateException.class));
        assertTrue(TestUtils.isThrown(throwISE, RuntimeException.class));
        assertTrue(TestUtils.isThrown(throwISE, Exception.class));
        assertTrue(TestUtils.isThrown(throwISE, Throwable.class));
        assertFalse(TestUtils.isThrown(throwISE, Error.class));
    }
}
