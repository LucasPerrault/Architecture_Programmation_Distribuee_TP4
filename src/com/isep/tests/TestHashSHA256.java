package com.isep.tests;

import com.isep.HashSHA256;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestHashSHA256 {
    @Test
    public void HashSHA256StringParams() {
        HashSHA256 h = new HashSHA256("TEST");
        assertEquals(32, h.getHashSize());
    }

    @Test
    public void Hash256HashParams() {
        HashSHA256 h1 = new HashSHA256("TE");
        HashSHA256 h2 = new HashSHA256("ST");

        HashSHA256 h = new HashSHA256(h1, h2);

        assertEquals(32, h.getHashSize());
    }
}
