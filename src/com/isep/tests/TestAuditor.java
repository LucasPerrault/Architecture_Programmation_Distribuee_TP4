package com.isep.tests;

import com.isep.Auditor;
import com.isep.HashSHA256;
import com.isep.LogServer;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuditor {

    @Test()
    public void AuditorConstructor() {
        LogServer l = new LogServer("log.txt");
        Auditor a = new Auditor(l);

        assertArrayEquals(a.getServer().getMerkleTree().getHashSHA256().getHash(), l.getMerkleTree().getHashSHA256().getHash());
    }

    @Test()
    public void buildHashEndNull() {
        LogServer l = new LogServer("log.txt");
        Auditor a = new Auditor(l);

        HashSHA256 h  = a.buildHash("test", 0, 0, new LinkedList<>());
        assertArrayEquals(new HashSHA256("test").getHash(), h.getHash());
    }
}
