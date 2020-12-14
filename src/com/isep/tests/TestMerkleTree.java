package com.isep.tests;
import com.isep.HashSHA256;
import com.isep.MerkleTree;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMerkleTree {

    @Test
    public void MerkleTreeStringParams() {
        MerkleTree m = new MerkleTree("TEST", 0);
        assertEquals(1, m.getSize());
        assertEquals(1, m.getNext());
        assertEquals(0, m.getStartRangeLog());
        assertEquals(0, m.getEndRangeLog());
        assertArrayEquals(new HashSHA256("TEST").getHash(), m.getHashSHA256().getHash());
    }

    @Test
    public void MerkleTreeComplexParamsBasic() {
        MerkleTree mLeft = new MerkleTree("TE", 0);
        MerkleTree mRight = new MerkleTree("ST", 1);

        MerkleTree m = new MerkleTree(mLeft, mRight);

        assertEquals(mLeft, m.getMerkleTreeLeft());
        assertEquals(mRight, m.getMerkleTreeRight());
        assertEquals(mLeft.getStartRangeLog(), m.getStartRangeLog());
        assertEquals(mRight.getEndRangeLog(), m.getEndRangeLog());
        assertEquals(mRight.getSize() + mLeft.getSize(), m.getSize());
        assertArrayEquals(new HashSHA256(mLeft.getHashSHA256(), mRight.getHashSHA256()).getHash(), m.getHashSHA256().getHash());
    }

    @Test
    public void MerkleTreeComplexeParamsConflict() {
        MerkleTree mLeft = new MerkleTree("TE", 0);
        MerkleTree mRight = new MerkleTree("ST", 3);

        MerkleTree m = new MerkleTree(mLeft, mRight);
        assertNull(m.getHashSHA256());
    }
}
