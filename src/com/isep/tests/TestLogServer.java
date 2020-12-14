package com.isep.tests;

import com.isep.HashSHA256;
import com.isep.LogServer;
import com.isep.MerkleTree;
import org.junit.Test;
import sun.awt.image.ImageWatched;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class TestLogServer {
    @Test
    public void LogServerMerkleTreeParams() {
        MerkleTree m = new MerkleTree("TEST", 0);
        LogServer l = new LogServer(m);

        assertEquals(m, l.getMerkleTree());
        assertEquals(m.getHashSHA256(), l.currentRootHashSHA256());
    }

    @Test
    public void LogServerFileParamsBuildTree() {
        /* Here, we tested BuildTree method.
        * Context : The Log file contains (line 1:test1 line 2:test2)
        * */
        LogServer l = new LogServer("log.txt");

        MerkleTree mLeft = new MerkleTree("test1", 4);
        MerkleTree mRight = new MerkleTree("test2", 5);
        MerkleTree expectedMerkleTree = new MerkleTree(mLeft, mRight);

        assertArrayEquals(expectedMerkleTree.getHashSHA256().getHash(), l.currentRootHashSHA256().getHash());
    }

    @Test
    public void genPath() {
        LogServer l = new LogServer("log.txt");

        MerkleTree mLeft = new MerkleTree("test1", 4);
        MerkleTree mRight = new MerkleTree("test2", 5);
        MerkleTree expectedMerkleTree = new MerkleTree(mLeft, mRight);

        LinkedList<HashSHA256> path = l.genPath(0);

        LinkedList<HashSHA256> expectedPath = new LinkedList<HashSHA256>();
        expectedPath.add(expectedMerkleTree.getMerkleTreeRight().getHashSHA256());

        assertEquals(expectedPath.toArray().length, path.toArray().length);
    }

    @Test()
    public void appendSingle() {
        LogServer l = new LogServer("log.txt");
        l.append("test3");

        MerkleTree mLeft = new MerkleTree("test1", 4);
        MerkleTree mRight = new MerkleTree("test2", 5);
        MerkleTree merkleTreeMerge = new MerkleTree(mLeft, mRight);

        MerkleTree mRight2 = new MerkleTree("test3", 6);
        MerkleTree mAppend = new MerkleTree(merkleTreeMerge, mRight2);

        assertArrayEquals(mAppend.getHashSHA256().getHash(), l.getMerkleTree().getHashSHA256().getHash());
    }
}
